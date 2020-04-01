package com.coremedia.livecontext.p13n.include;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_NAVI;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;
import static com.coremedia.blueprint.personalization.include.P13NUriConstants.Segments.SEGMENT_P13N;

/**
 * Handle dynamic/personalized personalized navigation via esi/client include.
 */
@Link
@RequestMapping
public class P13NNavigationHandler extends PageHandlerBase {

  private static final String ID_VARIABLE = "id";

  /**
   * URI pattern, for URIs like "/dynamic/navigation/p13n/coolsegment/id"
   */
  public static final String DYNAMIC_NAVIGATION_URI_PATTERN = '/' + PREFIX_DYNAMIC +
          '/' + SEGMENTS_NAVI +
          '/' + SEGMENT_P13N +
          "/{" + SEGMENT_ROOT + '}' +
          "/{" + ID_VARIABLE + ":" + PATTERN_NUMBER + "}";

  @GetMapping(value = DYNAMIC_NAVIGATION_URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ROOT) String context,
                                    @PathVariable(ID_VARIABLE) CMLinkable linkable,
                                    @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                    HttpServletRequest request) {

    Optional<Navigation> navigation = findNavigation(context, linkable);
    if (!navigation.isPresent()){
      return HandlerHelper.notFound();
    }

    User developer = UserVariantHelper.getUser(request);

    request.setAttribute(ABSOLUTE_URI_KEY, true);
    return createModelAndView(navigation.get(), linkable != null ? linkable : navigation.get(), view, developer);
  }

  @Link(type = {Page.class}, view = VIEW_FRAGMENT, uri = DYNAMIC_NAVIGATION_URI_PATTERN)
  public UriComponents buildLink(Page page, UriTemplate uriPattern, Map<String, Object> linkParameters) {

    Navigation context = getContextHelper().currentSiteContext();
    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);
    return result.buildAndExpand(
            ImmutableMap.of(
                    SEGMENT_ROOT, getPathSegments(context).get(0),
                    ID_VARIABLE, page.getContentId()
            ));
  }

  @NonNull
  private ModelAndView createModelAndView(@NonNull Navigation navigation,
                                          @NonNull Linkable linkable,
                                          @Nullable String view,
                                          @Nullable User developer) {

    Page page = asPage(navigation, linkable, developer);
    ModelAndView modelAndView = createModelAndView(page, view);
    RequestAttributeConstants.setPage(modelAndView, page);
    NavigationLinkSupport.setNavigation(modelAndView, navigation);
    return modelAndView;
  }

  Optional<Navigation> findNavigation(String pathSegment, CMLinkable linkable) {
    Navigation navigation = getNavigation(pathSegment);
    if (navigation == null) {
      return Optional.empty();
    }

    if (linkable != null) {
      navigation = getContextHelper().findAndSelectContextFor(navigation.getContext(), linkable);
    }
    return Optional.of(navigation);
  }

}
