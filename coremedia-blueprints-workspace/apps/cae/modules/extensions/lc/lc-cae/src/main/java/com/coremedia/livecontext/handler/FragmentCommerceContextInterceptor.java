package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextHelper;
import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.ecommerce.cae.AbstractCommerceContextInterceptor;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextBuilder;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.contract.ContractService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.links.context.Context;
import com.coremedia.livecontext.fragment.links.context.LiveContextContextHelper;
import com.coremedia.livecontext.fragment.links.context.accessors.LiveContextContextAccessor;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.google.common.collect.Sets.intersection;
import static java.util.stream.Collectors.toSet;

/**
 * Suitable for URLs whose second segment denotes the store, e.g. /fragment/10001/...
 */
public class FragmentCommerceContextInterceptor extends AbstractCommerceContextInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(FragmentCommerceContextInterceptor.class);

  private CatalogAliasTranslationService catalogAliasTranslationService;
  private LiveContextContextAccessor fragmentContextAccessor;
  private LiveContextSiteResolver liveContextSiteResolver;

  private boolean contractsProcessingEnabled = true;

  // Names of context entries
  private String contextNameMemberGroup = "wc.preview.memberGroups";
  private String contextNameTimestamp = "wc.preview.timestamp";
  private String contextNamePreviewUserGroup = "wc.preview.usergroup";
  private String contextNameTimezone = "wc.preview.timezone";
  private String contextNameWorkspaceId = "wc.preview.workspaceId";
  private String contextNameUserId = "wc.user.id";
  private String contextNameUserName = "wc.user.loginid";
  private String contextNameContractIds = "wc.preview.contractIds";
  private String contextNameUserGroupIds = "wc.user.membergroupids";

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, HttpServletResponse response, Object handler) {
    setFragmentContext(request);
    return super.preHandle(request, response, handler);
  }

  @Override
  @NonNull
  protected Optional<CommerceConnection> getCommerceConnectionWithConfiguredStoreContext(
          @NonNull Site site, @NonNull HttpServletRequest request) {
    Optional<CommerceConnection> connection = super.getCommerceConnectionWithConfiguredStoreContext(site, request);

    connection.ifPresent(commerceConnection -> updateStoreContext(commerceConnection, request, site.getId()));

    return connection;
  }

  private void updateStoreContext(@NonNull CommerceConnection connection, @NonNull HttpServletRequest request,
                                  @NonNull String siteId) {
    FragmentParameters fragmentParameters = FragmentContextProvider.getFragmentContext(request).getParameters();

    fragmentParameters.getCatalogId().ifPresent(catalogId -> {
      StoreContext originalStoreContext = connection.getStoreContext();

      Optional<CatalogAlias> catalogAlias = catalogAliasTranslationService
              .getCatalogAliasForId(catalogId, originalStoreContext);

      StoreContext updatedStoreContext = connection
              .getStoreContextProvider()
              .buildContext(originalStoreContext)
              .withCatalogId(catalogId)
              .withCatalogAlias(catalogAlias.orElse(null))
              .build();

      connection.setInitialStoreContext(updatedStoreContext);
      CurrentStoreContext.set(updatedStoreContext);
      StoreContextHelper.setStoreContextToRequest(updatedStoreContext, request);
    });

    if (isPreview()) {
      Optional<Context> fragmentContext = LiveContextContextHelper.findContext(request);
      if (fragmentContext.isPresent()) {
        StoreContextProvider storeContextProvider = connection.getStoreContextProvider();
        StoreContext updatedStoreContext = updateStoreContextForPreview(fragmentContext.get(),
                connection.getStoreContext(), storeContextProvider, request);
        connection.setInitialStoreContext(updatedStoreContext);
        CurrentStoreContext.set(updatedStoreContext);
        StoreContextHelper.setStoreContextToRequest(updatedStoreContext, request);
      }
    }
  }

  @Override
  protected void initUserContext(@NonNull CommerceConnection commerceConnection, @NonNull HttpServletRequest request) {
    super.initUserContext(commerceConnection, request);

    UserContext userContext = CurrentUserContext.find().orElse(null);
    Context fragmentContext = LiveContextContextHelper.findContext(request).orElse(null);
    if (userContext == null || fragmentContext == null) {
      return;
    }

    userContext = adjustUserContext(userContext, fragmentContext);
    CurrentUserContext.set(userContext);

    StoreContext storeContext = commerceConnection.getStoreContext();
    StoreContextBuilder storeContextBuilder = commerceConnection.getStoreContextProvider().buildContext(storeContext);

    // Set user segments.
    findStringValue(fragmentContext, contextNameUserGroupIds)
            .ifPresent(storeContextBuilder::withUserSegments);

    if (contractsProcessingEnabled) {
      final UserContext finalUserContext = userContext;
      commerceConnection.getContractService()
              .flatMap(contractService -> findContractIdsForUserContextContractsProcessing(
                      fragmentContext, storeContext, finalUserContext, contractService))
              .ifPresent(storeContextBuilder::withContractIds);
    }

    StoreContext clonedStoreContext = storeContextBuilder.build();
    commerceConnection.setInitialStoreContext(clonedStoreContext);
    CurrentStoreContext.set(clonedStoreContext);
    StoreContextHelper.setStoreContextToRequest(clonedStoreContext, request);
  }

  @NonNull
  private UserContext adjustUserContext(@NonNull UserContext userContext, @NonNull Context fragmentContext) {
    UserContext.Builder userContextBuilder = UserContext.buildCopyOf(userContext);

    findStringValue(fragmentContext, contextNameUserId).ifPresent(userContextBuilder::withUserId);
    findStringValue(fragmentContext, contextNameUserName).ifPresent(userContextBuilder::withUserName);

    return userContextBuilder.build();
  }

  @NonNull
  @Override
  protected Optional<Site> findSite(@NonNull HttpServletRequest request, String normalizedPath) {
    FragmentParameters parameters = FragmentContextProvider.getFragmentContext(request).getParameters();
    return Optional.ofNullable(parameters).flatMap(liveContextSiteResolver::findSiteFor);
  }

  @Override
  public SiteResolver getSiteResolver() {
    return liveContextSiteResolver;
  }

  protected void setFragmentContext(@NonNull HttpServletRequest request) {
    // apply the absolute URL flag for fragment requests
    request.setAttribute(ABSOLUTE_URI_KEY, true);
    fragmentContextAccessor.openAccessToContext(request);
  }

  @NonNull
  private StoreContext updateStoreContextForPreview(@NonNull Context fragmentContext,
                                                    @NonNull StoreContext storeContext,
                                                    @NonNull StoreContextProvider storeContextProvider,
                                                    @NonNull HttpServletRequest request) {
    String newUserSegments = null;
    ZonedDateTime newPreviewDate = null;
    WorkspaceId newWorkspaceId;

    // member group user segments
    Optional<String> memberGroupUserSegments = findStringValue(fragmentContext, contextNameMemberGroup);
    if (memberGroupUserSegments.isPresent()) {
      newUserSegments = memberGroupUserSegments.get();
    }

    // preview mode
    if (isStudioPreviewRequest(request)) {
      // preview date
      newPreviewDate = createPreviewDate(fragmentContext).orElse(null);

      // preview user group segments
      Optional<String> previewUserGroupSegments = findStringValue(fragmentContext, contextNamePreviewUserGroup);
      if (previewUserGroupSegments.isPresent()) {
        newUserSegments = previewUserGroupSegments.get();
      }
    }

    // workspace ID
    newWorkspaceId = findStringValue(fragmentContext, contextNameWorkspaceId).map(WorkspaceId::of).orElse(null);

    // Update store context.
    StoreContextBuilder storeContextBuilder = storeContextProvider.buildContext(storeContext);
    if (newUserSegments != null) {
      storeContextBuilder.withUserSegments(newUserSegments);
    }
    if (newPreviewDate != null) {
      storeContextBuilder.withPreviewDate(newPreviewDate);
    }
    if (newWorkspaceId != null) {
      storeContextBuilder.withWorkspaceId(newWorkspaceId);
    }

    // Update request.
    if (newPreviewDate != null) {
      Calendar calendar = GregorianCalendar.from(newPreviewDate);
      request.setAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE, calendar);
    }

    return storeContextBuilder.build();
  }

  @VisibleForTesting
  boolean isStudioPreviewRequest(@NonNull HttpServletRequest request) {
    return LiveContextPageHandlerBase.isStudioPreviewRequest(request);
  }

  @NonNull
  @VisibleForTesting
  Optional<ZonedDateTime> createPreviewDate(@NonNull Context fragmentContext) {
    Optional<String> timestampText = findStringValue(fragmentContext, contextNameTimestamp);

    ZoneId zoneId = findStringValue(fragmentContext, contextNameTimezone)
            .map(this::parseTimeZone)
            .orElse(null);

    return timestampText.flatMap(text -> parsePreviewDate(text, zoneId));
  }

  /**
   * Obtain a datetime value with a time zone from values in the fragment context.
   *
   * @param timestampText the value of the {@link #setContextNameTimestamp(String)} attribute from the fragment
   *                      context as the timestamp string to be parsed
   * @param zoneId        the value of the {@link #setContextNameTimezone(String)} attribute from the fragment context
   *                      as the time zone string already parsed into a time zone ID
   * @return The time represented by the timestamp and time zone, or nothing if the timestamp cannot be parsed
   */
  @NonNull
  protected Optional<ZonedDateTime> parsePreviewDate(@NonNull String timestampText, @Nullable ZoneId zoneId) {
    ZoneId nonNullZoneId = zoneId != null ? zoneId : ZoneId.systemDefault();

    return parsePreviewTimestamp(timestampText)
            .map(Timestamp::toLocalDateTime)
            .map(localDateTime -> ZonedDateTime.of(localDateTime, nonNullZoneId));
  }

  @NonNull
  private Optional<Timestamp> parsePreviewTimestamp(@NonNull String text) {
    try {
      Timestamp timestamp = Timestamp.valueOf(text);
      return Optional.of(timestamp);
    } catch (IllegalArgumentException e) {
      LOG.warn("Cannot convert timestamp \"{}\", ignore", text, e);
      return Optional.empty();
    }
  }

  @NonNull
  private ZoneId parseTimeZone(@NonNull String text) {
    return TimeZone.getTimeZone(text).toZoneId();
  }

  @NonNull
  private Optional<List<String>> findContractIdsForUserContextContractsProcessing(
          @NonNull Context fragmentContext, @NonNull StoreContext storeContext, @NonNull UserContext userContext,
          @NonNull ContractService contractService) {
    Collection<String> contractIdsFromContext = findContractIdsInFragmentContext(fragmentContext);
    if (contractIdsFromContext.isEmpty()) {
      return Optional.empty();
    }

    // check if user is allowed to execute a call for the passed contracts
    String organizationId = findStringValue(fragmentContext, "user.organization.id").orElse(null);
    Set<String> contractIdsForUser = findContractIdsForUser(contractService, storeContext, userContext,
            organizationId);

    Collection intersection = intersection(contractIdsForUser, Sets.newHashSet(contractIdsFromContext));
    if (intersection.isEmpty()) {
      return Optional.empty();
    }

    //noinspection unchecked
    return Optional.of(new ArrayList<String>(intersection));
  }

  @NonNull
  private Collection<String> findContractIdsInFragmentContext(@NonNull Context fragmentContext) {
    return findStringValue(fragmentContext, contextNameContractIds)
            .filter(contractIdsStr -> !contractIdsStr.isEmpty())
            .map(Splitter.on(' ')::splitToList)
            .orElseGet(Collections::emptyList);
  }

  @NonNull
  private static Set<String> findContractIdsForUser(@NonNull ContractService contractService,
                                                    @NonNull StoreContext storeContext,
                                                    @NonNull UserContext userContext,
                                                    @Nullable String organizationId) {
    Collection<Contract> contractsForUser = contractService.findContractIdsForUser(userContext, storeContext,
            organizationId);

    return contractsForUser.stream()
            .map(CommerceBean::getExternalTechId)
            .collect(toSet());
  }

  @NonNull
  private static Optional<String> findStringValue(@NonNull Context fragmentContext, @NonNull String name) {
    String value = (String) fragmentContext.get(name);
    return Optional.ofNullable(value);
  }

  @Required
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  @Required
  public void setFragmentContextAccessor(LiveContextContextAccessor fragmentContextAccessor) {
    this.fragmentContextAccessor = fragmentContextAccessor;
  }

  @Required
  public void setLiveContextSiteResolver(LiveContextSiteResolver liveContextSiteResolver) {
    this.liveContextSiteResolver = liveContextSiteResolver;
  }

  public void setContractsProcessingEnabled(boolean contractsProcessingEnabled) {
    this.contractsProcessingEnabled = contractsProcessingEnabled;
  }

  public void setContextNameMemberGroup(String contextNameMemberGroup) {
    this.contextNameMemberGroup = contextNameMemberGroup;
  }

  public void setContextNameTimestamp(String contextNameTimestamp) {
    this.contextNameTimestamp = contextNameTimestamp;
  }

  public void setContextNameTimezone(String contextNameTimezone) {
    this.contextNameTimezone = contextNameTimezone;
  }

  public void setContextNameWorkspaceId(String contextNameWorkspaceId) {
    this.contextNameWorkspaceId = contextNameWorkspaceId;
  }

  public void setContextNameUserId(String contextNameUserId) {
    this.contextNameUserId = contextNameUserId;
  }

  public void setContextNameUserName(String contextNameUserName) {
    this.contextNameUserName = contextNameUserName;
  }

  public void setContextNameContractIds(String contextNameContractIds) {
    this.contextNameContractIds = contextNameContractIds;
  }
}
