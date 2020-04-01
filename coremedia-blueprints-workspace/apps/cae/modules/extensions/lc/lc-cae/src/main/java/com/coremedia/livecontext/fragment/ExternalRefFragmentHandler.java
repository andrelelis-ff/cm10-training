package com.coremedia.livecontext.fragment;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.fragment.resolver.ExternalReferenceResolver;
import com.coremedia.livecontext.fragment.resolver.LinkableAndNavigation;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.coremedia.objectserver.web.HandlerHelper.badRequest;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ExternalRefFragmentHandler extends FragmentHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRefFragmentHandler.class);

  private List<ExternalReferenceResolver> externalReferenceResolvers;
  private boolean usePageRendering = false;

  // --- FragmentHandler --------------------------------------------

  @Override
  public boolean include(FragmentParameters params) {
    return !Strings.isNullOrEmpty(params.getExternalRef());
  }

  @NonNull
  @Override
  public ModelAndView createModelAndView(@NonNull FragmentParameters parameters, @NonNull HttpServletRequest request) {
    String externalRef = parameters.getExternalRef();

    ExternalReferenceResolver resolver = findReferenceResolver(parameters);
    if (resolver == null) {
      LOGGER.warn("Cannot resolve external reference value '{}'", externalRef);
      return HandlerHelper.notFound("ExternalRefFragmentHandler could not find an external reference resolver for '"
              + externalRef + "'");
    }

    // resolve the external reference
    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return HandlerHelper.notFound("No site available from SiteHelper.");
    }

    LinkableAndNavigation linkableAndNavigation = resolver.resolveExternalRef(parameters, site);

    if (linkableAndNavigation == null || linkableAndNavigation.getLinkable() == null) {
      LOGGER.info("No content could be resolved for external reference value '{}'", externalRef);
      return HandlerHelper.notFound(resolver + " could not find content for '" + externalRef + "'");
    }

    Content linkable = linkableAndNavigation.getLinkable();
    Content navigation = linkableAndNavigation.getNavigation();
    if (navigation == null) {
      if (isSubtypeOfCMNavigation(linkable)) {
        navigation = linkable;
      }
    }

    if (navigation == null) {
      LOGGER.warn("No navigation could be resolved for external reference value '{}'", externalRef);
      return HandlerHelper.notFound("No navigation could be resolved for external reference value '"
              + externalRef + "'");
    }

    //check if the content and navigation belong to the selected site
    SitesService sitesService = getSitesService();
    if (!sitesService.isContentInSite(site, navigation)) {
      return badRequest("Resolved context is not part of the given site");
    }
    if (!sitesService.isContentInSite(site, linkable)) {
      return badRequest("The content resolved for the given external reference (" + externalRef
              + ") is not part of the given site");
    }

    ModelAndView modelAndView = doCreateModelAndView(parameters, navigation, linkable,
            UserVariantHelper.getUser(request));

    return modelAndView != null
            ? modelAndView
            : badRequest("Don't know how to handle fragment parameters " + parameters);
  }

  @VisibleForTesting
  static boolean isSubtypeOfCMNavigation(@NonNull Content content) {
    return content.getType().isSubtypeOf(CMNavigation.NAME);
  }

  /**
   * Create the ModelAndView.
   * <p>
   * Invoked if linkable and navigation could be derived from the request
   * and all sanity checks have succeeded.
   * <p>
   * This default implementation covers all current and historical
   * Blueprint usecases.  You can enhance or simplify it according to your
   * project's particular needs.
   * <p>
   * The developer parameter may be considered by particular features to
   * reflect work in progress.
   */
  @Nullable
  protected ModelAndView doCreateModelAndView(@NonNull FragmentParameters parameters, Content navigation,
                                              Content linkable, @Nullable User developer) {
    String placement = parameters.getPlacement();
    String view = parameters.getView();

    // when usePageRendering = true the full html head and body is rendered
    if (isBlank(placement) && usePageRendering) {
      return createModelAndViewForPage(navigation, linkable, view, developer);
    }

    // if the target is a channel and no placement and/or view is given we use a well-known "pagegrid" view that
    // results in a full page grid rendering (but without complete html head and body)
    view = normalizedPageFragmentView(linkable, placement, view);

    // if the target is a deep content (like an article) we render a
    // linkable directly (and not a page or pagegrid or placement)
    if (!isRequestToPlacement(linkable, navigation, placement)) {
      return createModelAndViewForLinkable(navigation, linkable, view, developer);
    }

    //include a page fragment for the given channel
    if (isNotBlank(placement)) {
      return createModelAndViewForPlacement(navigation, view, placement, developer);
    }

    return null;
  }

  // --- internal ---------------------------------------------------

  @VisibleForTesting
  @NonNull
  protected ModelAndView createModelAndViewForLinkable(@NonNull Content channel, @NonNull Content child, String view,
                                                       @Nullable User developer) {
    // The default view is used only for placement requests, that do not request a certain view. For
    // any other requests, the default view is null (as usual).
    if ("default".equals(view)) {
      view = null;
    }

    ContentBeanFactory contentBeanFactory = getContentBeanFactory();
    Navigation navigation = contentBeanFactory.createBeanFor(channel, Navigation.class);
    Linkable linkable = contentBeanFactory.createBeanFor(child, Linkable.class);

    DataViewFactory dataViewFactory = getDataViewFactory();
    if (dataViewFactory != null) {
      linkable = dataViewFactory.loadCached(linkable, null);
    }

    if (!getValidationService().validate(linkable)) {
      return handleInvalidLinkable(linkable);
    }

    Page page = asPage(navigation, linkable, developer);
    ModelAndView modelAndView = HandlerHelper.createModelWithView(linkable, view);
    RequestAttributeConstants.setPage(modelAndView, page);
    NavigationLinkSupport.setNavigation(modelAndView, navigation);

    return modelAndView;
  }

  @NonNull
  private ModelAndView createModelAndViewForPage(Content navigation, Content linkable, String view,
                                                 @Nullable User developer) {
    ContentBeanFactory contentBeanFactory = getContentBeanFactory();

    Navigation navigationBean = contentBeanFactory.createBeanFor(navigation, Navigation.class);

    Linkable linkableBean = contentBeanFactory.createBeanFor(linkable, Linkable.class);
    if (!getValidationService().validate(linkableBean)) {
      return handleInvalidLinkable(linkableBean);
    }

    Page page = asPage(navigationBean, linkableBean, developer);
    return createModelAndView(page, view);
  }

  @NonNull
  private ModelAndView createModelAndViewForPlacement(Content navigation, String view, String placement,
                                                      @Nullable User developer) {
    CMChannel channelBean = getContentBeanFactory().createBeanFor(navigation, CMChannel.class);

    // validation will be done in following method
    return createModelAndViewForPlacementAndView(channelBean, placement, view, developer);
  }

  // --------------- Helper -----------------

  /**
   * Based on the linkable, the navigation and the placement name this method evaluates if the request
   * is meant to be a placement request or not. The livecontext include tag allows any combination
   * of request parameter, hence it is allowed to provide a placement name together with a
   * content id for a linkable and a navigation document. In case they both are different, for example
   * linkable is a requested article and navigation its context, no placement shall be rendered but
   * the whole article. The placement name must then be ignored.
   */
  private boolean isRequestToPlacement(@Nullable Content linkable, Content navigation, @Nullable String placementName) {
    if (isBlank(placementName)) {
      return false;
    }

    //noinspection RedundantIfStatement
    if (linkable != null && !linkable.equals(navigation)) {
      return false;
    }

    return true;
  }

  /**
   * Finds the matching resolver for the given fragment attribute values.
   */
  @Nullable
  private ExternalReferenceResolver findReferenceResolver(FragmentParameters params) {
    for (ExternalReferenceResolver resolver : externalReferenceResolvers) {
      if (resolver.include(params)) {
        return resolver;
      }
    }

    return null;
  }

  // ---------- Config ----------------------------

  @Required
  public void setExternalReferenceResolvers(List<ExternalReferenceResolver> externalReferenceResolvers) {
    this.externalReferenceResolvers = externalReferenceResolvers;
  }

  public void setUsePageRendering(boolean usePageRendering) {
    this.usePageRendering = usePageRendering;
  }
}
