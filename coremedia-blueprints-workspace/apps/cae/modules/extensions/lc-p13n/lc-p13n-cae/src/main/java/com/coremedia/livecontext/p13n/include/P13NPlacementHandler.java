package com.coremedia.livecontext.p13n.include;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.contentbeans.LiveContextExternalProduct;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_PLACEMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;
import static com.coremedia.blueprint.personalization.include.P13NUriConstants.Segments.SEGMENT_P13N;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

/**
 * Handle dynamic/personalized personalized content via esi/client include.
 */
@Link
@RequestMapping
public class P13NPlacementHandler extends PageHandlerBase {

  private static final String UNRESOLVABLE_PLACEMENT_VIEW_NAME = "unresolvablePlacement";
  private static final String PLACEMENT_NAME_MAV_KEY = "placementName";

  private static final String ID_VARIABLE = "id";
  private static final String PAGEGRID_VARIABLE = "pageGridName";
  private static final String PLACEMENT_VARIABLE = "placementName";

  /**
   * URI pattern, for URIs like "/dynamic/placement/p13n/coolsegment/id"
   */
  public static final String DYNAMIC_PLACEMENT_URI_PATTERN = '/' + PREFIX_DYNAMIC +
          '/' + SEGMENTS_PLACEMENT +
          '/' + SEGMENT_P13N +
          "/{" + SEGMENT_ROOT + '}' +
          "/{" + ID_VARIABLE + ":" + PATTERN_NUMBER + "}" +
          "/{" + PAGEGRID_VARIABLE + '}' +
          "/{" + PLACEMENT_VARIABLE + '}';

  private ValidationService validationService;

  @GetMapping(value = DYNAMIC_PLACEMENT_URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ROOT) String context,
                                    @PathVariable(ID_VARIABLE) CMObject cmObject,
                                    @PathVariable(PAGEGRID_VARIABLE) String pageGridName,
                                    @PathVariable(PLACEMENT_VARIABLE) String placementName,
                                    @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                    HttpServletRequest request) {
    Navigation navigation = getNavigation(context);

    if (cmObject instanceof CMChannel && navigation != null) {
      request.setAttribute(ABSOLUTE_URI_KEY, true);
      return createModelAndViewForPlacementAndView((CMChannel) cmObject, pageGridName, placementName, view);
    }
    return HandlerHelper.notFound();
  }

  @Link(type = {ContentBeanBackedPageGridPlacement.class}, view = VIEW_FRAGMENT, uri = DYNAMIC_PLACEMENT_URI_PATTERN)
  public UriComponents buildLink(ContentBeanBackedPageGridPlacement placement, UriTemplate uriPattern, Map<String, Object> linkParameters) {
    Navigation context = getContextHelper().currentSiteContext();
    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);
    return result.buildAndExpand(
            ImmutableMap.of(
                    SEGMENT_ROOT, getPathSegments(context).get(0),
                    ID_VARIABLE, placement.getNavigation().getContentId(),
                    PAGEGRID_VARIABLE, placement.getStructPropertyName(),
                    PLACEMENT_VARIABLE, placement.getName()
            ));
  }

  @Required
  public void setValidationService(ValidationService validationService) {
    this.validationService = validationService;
  }

  @NonNull
  private ModelAndView createModelAndViewForPlacementAndView(@NonNull CMChannel channel,
                                                             @NonNull String pageGridName,
                                                             @NonNull String placementName,
                                                             @Nullable String view) {
    //noinspection unchecked
    if (!validationService.validate(channel)) {
      String segment = channel.getSegment();
      LOG.debug("Trying to render a placement from invalid content, returning {} ({}).", SC_NO_CONTENT, segment);
      return notFound("invalid content: " + segment);
    }

    ContentBeanBackedPageGridPlacement placement = null;
    if (channel instanceof CMExternalChannel && pageGridName.contains("pdp")) {
      CMExternalChannel augmentedCategory = (CMExternalChannel) channel;
      placement = (ContentBeanBackedPageGridPlacement) augmentedCategory.getPdpPagegrid().getPlacementForName(placementName);
    } else if (channel instanceof LiveContextExternalProduct && pageGridName.contains("pdp")) {
      LiveContextExternalProduct augmentedProduct = (LiveContextExternalProduct) channel;
      PageGrid pageGrid = augmentedProduct.getPageGrid();
      placement = (ContentBeanBackedPageGridPlacement) pageGrid.getPlacementForName(placementName);
    }

    if (placement == null) {
      PageGrid pageGrid = channel.getPageGrid();
      placement = (ContentBeanBackedPageGridPlacement) pageGrid.getPlacementForName(placementName);
    }

    if (placement == null) {
      return createPlacementUnresolvableError(channel, placementName);
    }

    CMNavigation context = placement.getNavigation();

    Page page = asPage(context, context, null);
    ModelAndView modelAndView = HandlerHelper.createModelWithView(placement, view);
    RequestAttributeConstants.setPage(modelAndView, page);
    NavigationLinkSupport.setNavigation(modelAndView, context);

    return modelAndView;
  }

  @NonNull
  private static ModelAndView createPlacementUnresolvableError(@NonNull CMChannel cmChannel, @NonNull String placementName) {
    LOG.error("No placement named {} found for {}.", placementName, cmChannel.getContent().getPath());
    ModelAndView modelAndView = notFound("No placement found for name '" + placementName + "'");
    modelAndView.setViewName(UNRESOLVABLE_PLACEMENT_VIEW_NAME);
    NavigationLinkSupport.setNavigation(modelAndView, cmChannel);
    modelAndView.addObject(PLACEMENT_NAME_MAV_KEY, placementName);
    return modelAndView;
  }
}
