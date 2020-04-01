package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.user.User;
import com.coremedia.common.util.Predicate;
import com.coremedia.livecontext.fragment.pagegrid.PageGridPlacementResolver;
import com.coremedia.objectserver.web.HandlerHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static com.coremedia.blueprint.cae.view.DynamicIncludeHelper.createDynamicIncludeRootDelegateModelAndView;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

/**
 * Common base class for fragment handler. Each handler implements the Predicate interface
 * to decide if the concrete instance is responsible for the call, depending on the parameters in the FragmentParameters
 * object.
 */
public abstract class FragmentHandler extends PageHandlerBase implements Predicate<FragmentParameters> {

  private static final String DEFAULT_PAGEGRID_VIEW = "asFragment";
  static final String PLACEMENT_NAME_MAV_KEY = "placementName";
  static final String UNRESOLVABLE_PLACEMENT_VIEW_NAME = "unresolvablePlacement";

  protected PageGridPlacementResolver pageGridPlacementResolver;
  protected ValidationService<Linkable> validationService;

  /**
   * Creates the ModelAndView depending on the parameters passed.
   * <p>
   * The method may return null to indicate "not found".
   * However, it is recommended to return a {@link HandlerHelper#notFound}
   * object with a specific message instead.
   *
   * @param params  The FragmentParameters instance that contains all matrix parameters
   * @param request The servlet request
   * @return The ModelAndView to create for the given parameters.
   */
  @Nullable
  abstract ModelAndView createModelAndView(@NonNull FragmentParameters params, @NonNull HttpServletRequest request);

  @NonNull
  protected ModelAndView createModelAndView(@NonNull Navigation navigation, @Nullable String view,
                                            @Nullable User developer) {
    return createModelAndView(asPage(navigation, navigation, developer), view);
  }

  @NonNull
  protected ModelAndView createFragmentModelAndView(@NonNull Navigation navigation, @Nullable String view,
                                                    @NonNull CMChannel rootChannel, @Nullable User developer) {
    CMContext context = navigation.getContext();

    if (view == null) {
      view = "asFragment";
    }

    if (context == null) {
      LOG.info("Could not find a content based context for category '{}'. Will use the root channel instead.",
              navigation.getTitle());

      Page page = asPage(rootChannel, rootChannel, developer);
      return createModelAndView(page, view);
    }

    Page page = asPage(context, context, navigation.getCodeResourcesTreeRelation(), developer);
    return createModelAndView(page, view);
  }

  @NonNull
  protected ModelAndView createFragmentModelAndViewForPlacementAndView(@NonNull Navigation navigation,
                                                                       @NonNull String placement,
                                                                       @Nullable String view,
                                                                       @NonNull CMChannel rootChannel,
                                                                       @Nullable User developer) {
    CMContext context = navigation.getContext();
    if (!(context instanceof CMChannel)) {
      LOG.info("Could not find a content based context for category '{}'. Will use the root channel instead.",
              navigation.getTitle());

      return createModelAndViewForPlacementAndView(rootChannel, placement, view, developer);
    }

    return createModelAndViewForPlacementAndView((CMChannel) context, placement, view, developer);
  }

  @NonNull
  protected ModelAndView createModelAndViewForPlacementAndView(@NonNull CMChannel channel,
                                                               @NonNull String placementName, @Nullable String view,
                                                               @Nullable User developer) {
    //noinspection unchecked
    if (!validationService.validate(channel)) {
      return handleInvalidLinkable(channel);
    }

    PageGridPlacement placement = pageGridPlacementResolver.resolvePageGridPlacement(channel, placementName);
    if (placement == null) {
      return createPlacementUnresolvableError(channel, placementName);
    }

    CMNavigation context = channel;
    // Take the context  of the placement for building the page . In most cases, this is the given channel.
    // For PDPs a specific navigation can be defined which differs from the given channel.
    if (placement instanceof ContentBeanBackedPageGridPlacement) {
      ContentBeanBackedPageGridPlacement contentBeanBackedPageGridPlacement =
              (ContentBeanBackedPageGridPlacement) placement;
      CMNavigation navigation = contentBeanBackedPageGridPlacement.getNavigation();
      if (navigation != null) {
        context = navigation;
      }
    }

    Page page = asPage(context, context, developer);
    // In case the placement contains a p13n content and dynamic include feature is enabled,
    // we need to wrap the placement into a DynamicInclude object to bypass the loop protection in DynamicIncludeRenderNodeDecoratorProvider.
    ModelAndView modelAndView = createDynamicIncludeRootDelegateModelAndView(placement, view);
    RequestAttributeConstants.setPage(modelAndView, page);
    NavigationLinkSupport.setNavigation(modelAndView, channel);

    return modelAndView;
  }

  @NonNull
   static ModelAndView createPlacementUnresolvableError(@NonNull CMLinkable cmLinkable, @NonNull String placementName) {
    LOG.error("No placement named {} found for {}.", placementName, cmLinkable.getContent().getPath());

    ModelAndView modelAndView = notFound("No placement found for name '" + placementName + "'");
    modelAndView.setViewName(UNRESOLVABLE_PLACEMENT_VIEW_NAME);
    if (cmLinkable instanceof Navigation) {
      NavigationLinkSupport.setNavigation(modelAndView, (Navigation) cmLinkable);
    }
    modelAndView.addObject(PLACEMENT_NAME_MAV_KEY, placementName);
    return modelAndView;
  }

  @NonNull
  protected ModelAndView handleInvalidLinkable(@NonNull Linkable linkable) {
    String segment = linkable.getSegment();
    LOG.debug("Trying to render invalid content, returning {} ({}).", SC_NO_CONTENT, segment);
    return notFound("invalid content: " + segment);
  }

  /**
   * If the target is a channel and no placement and/or view is given we use a well-known "pagegrid" view that
   * results in a full page grid rendering (but without complete html head and body)
   */
  public static String normalizedPageFragmentView(@NonNull Content linkable, @Nullable String placement,
                                                  @Nullable String view) {
    // A channel without a given placement or view is always rendered as full pagegrid (view "pagegrid")
    if (linkable.getType().isSubtypeOf(CMChannel.NAME) && placement == null && view == null) {
      return DEFAULT_PAGEGRID_VIEW;
    }
    return view;
  }

  //-------------- Config --------------------

  @Required
  public void setPageGridPlacementResolver(PageGridPlacementResolver pageGridPlacementResolver) {
    this.pageGridPlacementResolver = pageGridPlacementResolver;
  }

  public ValidationService<Linkable> getValidationService() {
    return validationService;
  }

  @Required
  public void setValidationService(ValidationService<Linkable> validationService) {
    this.validationService = validationService;
  }
}
