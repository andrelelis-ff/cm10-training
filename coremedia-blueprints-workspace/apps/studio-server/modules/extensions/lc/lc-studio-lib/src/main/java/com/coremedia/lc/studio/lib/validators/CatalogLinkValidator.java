package com.coremedia.lc.studio.lib.validators;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.NoCommerceConnectionAvailable;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.InvalidCatalogException;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.InvalidIdException;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.workspace.Workspace;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.rest.cap.validation.ContentTypeValidatorBase;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;
import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper.format;
import static com.coremedia.rest.validation.Severity.ERROR;
import static com.coremedia.rest.validation.Severity.WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Checks if catalog object can be loaded from catalog link property.
 * see also CatalogLink.as and CatalogLinkPropertyField.as
 */
public class CatalogLinkValidator extends ContentTypeValidatorBase {

  private static final Logger LOG = LoggerFactory.getLogger(CatalogLinkValidator.class);

  private static final String CODE_ISSUE_ID_EMPTY = "EmptyExternalId";
  private static final String CODE_ISSUE_ID_INVALID = "InvalidId";
  private static final String CODE_ISSUE_ID_VALID_ONLY_IN_A_WORKSPACE = "ValidInAWorkspace";
  private static final String CODE_ISSUE_CATALOG_ERROR = "catalogError";
  private static final String CODE_ISSUE_CONTEXT_INVALID = "InvalidStoreContext";
  private static final String CODE_ISSUE_CONTEXT_NOT_FOUND = "StoreContextNotFound";
  private static final String CODE_ISSUE_CATALOG_NOT_FOUND = "CatalogNotFoundError";

  private SitesService sitesService;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  private String propertyName;

  private boolean isOptional = false;

  public CatalogLinkValidator(CommerceConnectionInitializer commerceConnectionInitializer,
                              SitesService sitesService,
                              String propertyName) {
    this.sitesService = sitesService;
    this.commerceConnectionInitializer = commerceConnectionInitializer;
    this.propertyName = propertyName;
  }

  /**
   * A convinient method to add an issue. The given code will be prefixed with
   * the name of the document type and '_'
   *
   * @param issues    the given issues
   * @param severity  the severity of this issue
   * @param code      a code identifying the type of issue. This will be prefixed.
   * @param arguments optional argument describing the issue, for example indicating a illegally linked object
   */
  protected void addIssue(Issues issues, Severity severity, String code, Object... arguments) {
    issues.addIssue(severity, getPropertyName(), getContentType() + '_' + code, arguments);
  }

  protected void emptyPropertyValue(@NonNull Content content, @NonNull Issues issues) {
    addIssue(issues, ERROR, CODE_ISSUE_ID_EMPTY);
  }

  protected void invalidStoreContext(Issues issues, Object... arguments) {
    addIssue(issues, WARN, CODE_ISSUE_CONTEXT_INVALID, arguments);
  }

  protected void storeContextNotFound(Issues issues, Object... arguments) {
    addIssue(issues, WARN, CODE_ISSUE_CONTEXT_NOT_FOUND, arguments);
  }

  protected void invalidExternalId(Issues issues, Object... arguments) {
    addIssue(issues, WARN, CODE_ISSUE_ID_INVALID, arguments);
  }

  protected void validOnlyInWorkspace(Issues issues, Object... arguments) {
    addIssue(issues, WARN, CODE_ISSUE_ID_VALID_ONLY_IN_A_WORKSPACE, arguments);
  }

  protected void catalogNotFound(Issues issues, Object... arguments) {
    addIssue(issues, WARN, CODE_ISSUE_CATALOG_NOT_FOUND, arguments);
  }

  protected void catalogNotAvailable(Issues issues, String propertyValue) {
    issues.addIssue(WARN, propertyName, CODE_ISSUE_CATALOG_ERROR, propertyValue);
  }

  @Override
  public void validate(Content content, Issues issues) {
    if (content == null || !content.isInProduction()) {
      return;
    }

    String commerceBeanId = content.getString(propertyName);

    if (isBlank(commerceBeanId)) {
      if (!isOptional) {
        emptyPropertyValue(content, issues);
      }
      return;
    }

    Site site = getSite(content);

    if (site == null) {
      LOG.debug("The content {} belongs to no site; nothing to do.", content);
      return;
    }

    CommerceConnection commerceConnection;
    try {
      commerceConnection = getCommerceConnection(site);
    } catch (CommerceException e) {
      LOG.debug("StoreContext not found for content: {}", content.getPath(), e);
      storeContextNotFound(issues, commerceBeanId);
      return;
    }

    StoreContext storeContext = commerceConnection.getStoreContext();

    Optional<CommerceId> commerceIdOptional = CommerceIdParserHelper.parseCommerceId(commerceBeanId);
    if (!commerceIdOptional.isPresent()) {
      invalidExternalId(issues, commerceBeanId, storeContext.getStoreName());
      return;
    }

    CommerceId commerceId = commerceIdOptional.get();

    try {
      CommerceBeanFactory commerceBeanFactory = commerceConnection.getCommerceBeanFactory();

      // Clear workspace ID before validating.
      StoreContext storeContextWithoutWorkspaceId = commerceConnection
              .getStoreContextProvider()
              .buildContext(storeContext)
              .withWorkspaceId(null)
              .build();

      boolean commerceBeanWithoutWorkspaceExists = hasCommerceBean(commerceBeanFactory, commerceId,
              storeContextWithoutWorkspaceId);
      if (commerceBeanWithoutWorkspaceExists) {
        // catalog bean is found in the main catalog
        return;
      }

      Optional<String> externalIdOptional = commerceId.getExternalId();
      if (!externalIdOptional.isPresent()) {
        invalidExternalId(issues, commerceBeanId, storeContext.getStoreName());
        return;
      }

      String externalId = externalIdOptional.get();

      // No commerce bean found that belongs to no workspace.
      // Search all workspaces for one with a commerce bean.
      Workspace workspace = findWorkspaceWithExistingCommerceBean(commerceConnection, storeContext,
              commerceBeanFactory, commerceId);
      if (workspace != null) {
        validOnlyInWorkspace(issues, externalId, storeContext.getStoreName(), workspace.getName());
        return;
      }

      // commerce bean not found even in workspaces
      LOG.debug("id: {} not found in the store {}", commerceBeanId, storeContext.getStoreName());
      invalidExternalId(issues, externalId, storeContext.getStoreName());
    } catch (InvalidContextException e) {
      LOG.debug("StoreContext not found for content: {}", content.getPath(), e);
      invalidStoreContext(issues, commerceBeanId);
    } catch (InvalidIdException e) {
      LOG.debug("Invalid catalog id: {}", commerceBeanId, e);
      String storeName = storeContext.getStoreName();
      invalidExternalId(issues, commerceBeanId, storeName);
    } catch (InvalidCatalogException e) {
      LOG.debug("Invalid catalog: {}", commerceBeanId, e);
      CatalogAlias catalogAlias = storeContext.getCatalogAlias();
      catalogNotFound(issues, catalogAlias.toString(), commerceBeanId);
    } catch (CommerceException e) {
      LOG.debug("Catalog could not be accessed: {}", commerceBeanId, e);
      catalogNotAvailable(issues, commerceBeanId);
    }
  }

  @NonNull
  private CommerceConnection getCommerceConnection(@NonNull Site site) {
    return commerceConnectionInitializer.findConnectionForSite(site)
            .orElseThrow(() -> new NoCommerceConnectionAvailable(
                    String.format("No commerce connection available for site '%s'.", site.getName())));
  }

  /**
   * Return the first workspace for which a commerce bean exists.
   */
  @Nullable
  private static Workspace findWorkspaceWithExistingCommerceBean(@NonNull CommerceConnection commerceConnection,
                                                                 StoreContext storeContext,
                                                                 CommerceBeanFactory commerceBeanFactory,
                                                                 CommerceId commerceId) {
    StoreContextProvider storeContextProvider = commerceConnection.getStoreContextProvider();

    StoreContext storeContextToObtainAllWorkspacesFrom = storeContextProvider.buildContext(storeContext).build();
    List<Workspace> allWorkspaces = getWorkspaces(commerceConnection, storeContextToObtainAllWorkspacesFrom);

    for (Workspace workspace : allWorkspaces) {
      WorkspaceId workspaceId = getWorkspaceId(workspace);
      StoreContext storeContextWithWorkspaceId = storeContextProvider
              .buildContext(storeContextToObtainAllWorkspacesFrom)
              .withWorkspaceId(workspaceId)
              .build();

      boolean commerceBeanWithWorkspaceExists = hasCommerceBean(commerceBeanFactory, commerceId,
              storeContextWithWorkspaceId);
      if (commerceBeanWithWorkspaceExists) {
        return workspace;
      }
    }

    return null;
  }

  @NonNull
  private static List<Workspace> getWorkspaces(@NonNull CommerceConnection commerceConnection,
                                               @NonNull StoreContext storeContextClone) {
    return commerceConnection.getWorkspaceService()
            .map(workspaceService -> workspaceService.findAllWorkspaces(storeContextClone))
            .orElseGet(Collections::emptyList);
  }

  @NonNull
  private static WorkspaceId getWorkspaceId(@NonNull Workspace workspace) {
    return Optional.ofNullable(workspace.getExternalTechId())
            .map(WorkspaceId::of)
            .orElse(WORKSPACE_ID_NONE);
  }

  @Nullable
  protected Site getSite(@NonNull Content content) {
    ContentSiteAspect contentSiteAspect = sitesService.getContentSiteAspect(content);
    return contentSiteAspect.getSite();
  }

  private static boolean hasCommerceBean(@NonNull CommerceBeanFactory commerceBeanFactory,
                                         @NonNull CommerceId commerceId,
                                         @NonNull StoreContext storeContext) {
    try {
      CommerceBean commerceBean = commerceBeanFactory.loadBeanFor(commerceId, storeContext);
      return commerceBean != null;
    } catch (NotFoundException e) {
      LOG.trace("Exception creating commerce bean for {} with store context {}", format(commerceId), storeContext, e);
      return false;
    }
  }

  /**
   * Set to true if the validation is only be done if a link is set.
   *
   * @param optional flag
   */
  public void setOptional(boolean optional) {
    isOptional = optional;
  }

  protected String getPropertyName() {
    return propertyName;
  }
}
