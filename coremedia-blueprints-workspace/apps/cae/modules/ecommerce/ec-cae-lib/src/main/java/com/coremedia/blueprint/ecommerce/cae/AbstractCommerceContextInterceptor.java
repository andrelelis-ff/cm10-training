package com.coremedia.blueprint.ecommerce.cae;

import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextHelper;
import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.common.preview.PreviewDateFormatter;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextBuilder;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Initializes the StoreContextProvider according to the current request.
 */
public abstract class AbstractCommerceContextInterceptor extends HandlerInterceptorAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCommerceContextInterceptor.class);

  public static final String QUERY_PARAMETER_WORKSPACE_ID = "workspaceId";

  private static final String DYNAMIC_FRAGMENT = "/" + UriConstants.Segments.PREFIX_DYNAMIC + "/";

  private static final String STORE_CONTEXT_INITIALIZED = AbstractCommerceContextInterceptor.class.getName()
          + "#storeContext.initialized";

  private SiteResolver siteResolver;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  private boolean preview;
  private boolean initUserContext = false;

  // --- configure --------------------------------------------------

  /**
   * Default: false
   */
  public void setInitUserContext(boolean initUserContext) {
    this.initUserContext = initUserContext;
  }

  @Value("${cae.is.preview}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  @Required
  public void setSiteResolver(SiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  @Required
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  // --- HandlerInterceptor -----------------------------------------

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String normalizedPath = normalizePath(request.getPathInfo());
    Site site = findSite(request, normalizedPath).orElse(null);

    // If site is null, we cannot help it here.  Silently do nothing.
    // It is up to the request handler to return 404.
    if (site == null) {
      return true;
    }

    // Initialize just once.
    // We're not testing against `Commerce.get()` in
    // case we're running behind `CommerceConnectionFilter`.
    if (request.getAttribute(STORE_CONTEXT_INITIALIZED) != null) {
      return true;
    }

    SiteHelper.setSiteToRequest(site, request);
    try {
      prepareCommerceConnection(site, request);
    } catch (RuntimeException e) {
      // cleanup thread locals in case of exceptions (otherwise #afterCompletion is does this job)
      CurrentUserContext.remove();
      CurrentStoreContext.remove();
      throw e;
    }

    return true;
  }

  private void prepareCommerceConnection(@NonNull Site site, @NonNull HttpServletRequest request) {
    try {
      CommerceConnection commerceConnection = getCommerceConnectionWithConfiguredStoreContext(site, request)
              .orElse(null);
      if (commerceConnection == null) {
        return;
      }

      CurrentStoreContext.set(commerceConnection.getStoreContext());
      request.setAttribute(STORE_CONTEXT_INITIALIZED, true);

      if (initUserContext) {
        initUserContext(commerceConnection, request);
      }
    } catch (CommerceException e) {
      LOG.debug("No commerce connection found for site '{}'.", site.getName(), e);
    }
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
          throws Exception {
    super.afterCompletion(request, response, handler, ex);

    CurrentStoreContext.remove();
    CurrentUserContext.remove();
  }

// --- abstract ---------------------------------------------------

  /**
   * Calculate a site from the request.
   *
   * @param request        the request
   * @param normalizedPath is the URL path w/o a dynamic fragment prefix
   * @return a site, or nothing
   */
  @NonNull
  protected abstract Optional<Site> findSite(HttpServletRequest request, String normalizedPath);

  // --- hook points and utils for extending classes ----------------

  public SiteResolver getSiteResolver() {
    return siteResolver;
  }

  protected boolean isPreview() {
    return preview;
  }

  // --- basics, suitable for most extending classes ----------------

  @NonNull
  protected Optional<CommerceConnection> getCommerceConnectionWithConfiguredStoreContext(
          @NonNull Site site, @NonNull HttpServletRequest request) {
    Optional<CommerceConnection> connection = commerceConnectionInitializer.findConnectionForSite(site);

    // The commerce connection is supposed to be prototype-scoped (i.e.
    // a new instance is created every time the bean is requested).
    // Thus, fiddling with it here should be fine (although it would be
    // better to avoid that).

    if (!connection.isPresent()) {
      LOG.debug("Site '{}' has no commerce connection.", site.getName());
      return Optional.empty();
    }

    if (preview) {
      updateStoreContextForPreview(request, connection.get());
    }

    return connection;
  }

  private static void updateStoreContextForPreview(@NonNull HttpServletRequest request,
                                                   @NonNull CommerceConnection connection) {
    StoreContextProvider storeContextProvider = connection.getStoreContextProvider();
    StoreContext originalStoreContext = connection.getStoreContext();

    StoreContextBuilder storeContextBuilder = storeContextProvider.buildContext(originalStoreContext);

    StoreContext clonedStoreContext = prepareStoreContextForPreview(request, storeContextBuilder)
            .build();

    connection.setInitialStoreContext(clonedStoreContext);
    CurrentStoreContext.set(clonedStoreContext);
    StoreContextHelper.setStoreContextToRequest(clonedStoreContext, request);
  }

  @NonNull
  @SuppressWarnings("AssignmentToMethodParameter")
  private static StoreContextBuilder prepareStoreContextForPreview(@NonNull HttpServletRequest request,
                                                                   @NonNull StoreContextBuilder storeContextBuilder) {
    WorkspaceId workspaceId = findWorkspaceId(request).orElse(null);
    storeContextBuilder = storeContextBuilder.withWorkspaceId(workspaceId);

    ZonedDateTime previewDate = findPreviewDate(request).orElse(null);
    storeContextBuilder = storeContextBuilder.withPreviewDate(previewDate);

    return storeContextBuilder;
  }

  @NonNull
  private static Optional<WorkspaceId> findWorkspaceId(@NonNull HttpServletRequest request) {
    String workspaceIdStr = request.getParameter(QUERY_PARAMETER_WORKSPACE_ID);
    return Optional.ofNullable(workspaceIdStr)
            .map(WorkspaceId::of);
  }

  @NonNull
  private static Optional<ZonedDateTime> findPreviewDate(@NonNull HttpServletRequest request) {
    String previewDateText = request.getParameter(ValidityPeriodValidator.REQUEST_PARAMETER_PREVIEW_DATE);
    return Optional.ofNullable(previewDateText)
            .flatMap(PreviewDateFormatter::parse);
  }

  /**
   * Sets the user context to the user context provider.
   * You will need this if you want to do a call for a user.
   */
  protected void initUserContext(@NonNull CommerceConnection commerceConnection, @NonNull HttpServletRequest request) {
    try {
      UserContext userContext = commerceConnection.getUserContextProvider().createContext(request);
      CurrentUserContext.set(userContext);
    } catch (CommerceException e) {
      LOG.warn("Error creating commerce user context: {}", e.getMessage(), e);
    }
  }

  /**
   * Cut off a possible dynamic prefix
   */
  @Nullable
  @VisibleForTesting
  static String normalizePath(@Nullable String urlPath) {
    return urlPath != null
            && urlPath.startsWith(DYNAMIC_FRAGMENT) ? urlPath.substring(DYNAMIC_FRAGMENT.length() - 1) : urlPath;
  }
}
