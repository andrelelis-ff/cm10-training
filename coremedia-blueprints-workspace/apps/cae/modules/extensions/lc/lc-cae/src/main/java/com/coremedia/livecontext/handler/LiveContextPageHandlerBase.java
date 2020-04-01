package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.links.UrlPrefixResolver;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.handlers.PreviewHandler;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.link.LinkService;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.objectserver.web.links.UriComponentsHelper;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriComponents;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.coremedia.blueprint.base.links.UriConstants.Links.SCHEME_KEY;

public class LiveContextPageHandlerBase extends PageHandlerBase {

  protected static final String SHOP_NAME_VARIABLE = "shop";

  public static final String URL_PROVIDER_URL_TEMPLATE = "urlTemplate";
  public static final String URL_PROVIDER_QUERY_PARAMS = "queryParams";
  public static final String URL_PROVIDER_SEO_SEGMENT = "seoSegment";
  public static final String URL_PROVIDER_IS_STUDIO_PREVIEW = "isStudioPreview";
  public static final String URL_PROVIDER_IS_INITIAL_STUDIO_REQUEST = "isInitialStudioRequest";
  public static final String HAS_PREVIEW_TOKEN = "hasPreviewToken";
  public static final String URL_PROVIDER_SEARCH_TERM = "searchTerm";
  private static final String REQUEST_PARAMETER_PREVIEW = "preview";
  public static final String P13N_URI_PARAMETER = "p13n_test";

  private ResolveContextStrategy resolveContextStrategy;
  private LiveContextNavigationFactory liveContextNavigationFactory;
  private UrlPrefixResolver urlPrefixResolver;
  private LiveContextSiteResolver siteResolver;
  private SettingsService settingsService;
  private ContentRepository contentRepository;

  // --- construct and configure ------------------------------------

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setResolveContextStrategy(ResolveContextStrategy resolveContextStrategy) {
    this.resolveContextStrategy = resolveContextStrategy;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }

  @Required
  public void setUrlPrefixResolver(UrlPrefixResolver urlPrefixResolver) {
    this.urlPrefixResolver = urlPrefixResolver;
  }

  @Required
  public void setSiteResolver(LiveContextSiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  // --- features ---------------------------------------------------

  protected SettingsService getSettingsService() {
    return settingsService;
  }

  @NonNull
  protected Optional<LiveContextNavigation> getNavigationContext(@NonNull Site site,
                                                                 @NonNull CommerceBean commerceBean) {
    try {
      return resolveContextStrategy.resolveContext(site, commerceBean);
    } catch (Exception ignored) {
      // Do not log, means actually just "not found", does not indicate a problem.
      return Optional.empty();
    }
  }

  protected LiveContextNavigationFactory getLiveContextNavigationFactory() {
    return liveContextNavigationFactory;
  }

  protected LiveContextSiteResolver getSiteResolver() {
    return siteResolver;
  }

  protected UriComponents absoluteUri(UriComponents originalUri, Object bean, @NonNull Site site,
                                      Map<String, Object> linkParameters, @NonNull ServletRequest request) {
    if (!isAbsoluteUrlRequested(request)) {
      return originalUri;
    }

    String siteId = site.getId();
    String absoluteUrlPrefix = urlPrefixResolver.getUrlPrefix(siteId, bean, null);
    if (absoluteUrlPrefix == null) {
      throw new IllegalStateException("Cannot calculate an absolute URL for " + bean);
    } else if (!StringUtils.isBlank(absoluteUrlPrefix)) {
      //explicitly set scheme if it is set in link parameters
      String scheme = null;
      if (linkParameters != null) {
        Object schemeAttribute = linkParameters.get(SCHEME_KEY);
        if (schemeAttribute != null) {
          scheme = (String) schemeAttribute;
        }
      }

      return UriComponentsHelper.prefixUri(absoluteUrlPrefix, scheme, originalUri);
    }

    return UriComponentsHelper.prefixUri(absoluteUrlPrefix, null, originalUri);
  }

  @NonNull
  protected Optional<LinkService> getLinkService() {
    return CurrentStoreContext.get()
            .getConnection()
            .getLinkService();
  }

  /**
   * To evaluate if the newPreviewSession query parameter has to be applied to a commerce url, the evaluator has to know
   * if it's the first request triggered by a studio action (e.g. open in tab) or it's a follow up trigger by an author
   * clicking in the preview.
   * If it's a request triggered by a studio action an author want's to have a cleared session (no logged in user or
   * p13n context). If he tests in studio the preview the author want to stay logged in and use the same p13n context.
   *
   * @param request the current request
   * @return true if the request was triggered by a studio action.
   */
  public static boolean isInitialStudioRequest(@NonNull HttpServletRequest request) {
    return PreviewHandler.isStudioPreviewRequest(request);
  }

  protected boolean isPreview() {
    return contentRepository.isContentManagementServer();
  }

  public static boolean isStudioPreviewRequest(@NonNull HttpServletRequest request) {
    return isInitialStudioRequest(request)
            || isTrue(request.getAttribute(HAS_PREVIEW_TOKEN))
            || isTrue(request.getParameter(REQUEST_PARAMETER_PREVIEW))
            || isTrue(request.getParameter(P13N_URI_PARAMETER));
  }

  private static boolean isTrue(Object attribute) {
    return Boolean.valueOf(attribute + "");
  }

  @NonNull
  protected String getSiteSegment(@NonNull Site site) {
    return getContentLinkBuilder().getVanityName(site.getSiteRootDocument());
  }

  // --- internal ---------------------------------------------------

  @VisibleForTesting
  SecurityContext getSecurityContext() {
    return SecurityContextHolder.getContext();
  }

  private static boolean isAbsoluteUrlRequested(@NonNull ServletRequest request) {
    Object absolute = request.getAttribute(ABSOLUTE_URI_KEY);
    return "true".equals(absolute) || Boolean.TRUE.equals(absolute);
  }
}
