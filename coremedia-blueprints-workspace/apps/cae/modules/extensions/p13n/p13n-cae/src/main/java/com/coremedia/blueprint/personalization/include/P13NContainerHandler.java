package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.DynamizableCMTeasableContainer;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
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
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_CONTAINER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;
import static com.coremedia.blueprint.personalization.include.P13NUriConstants.Segments.SEGMENT_P13N;
import static com.coremedia.objectserver.web.HandlerHelper.notFound;

/**
 * Handle dynamic/personalized personalized content via esi/client include.
 * see also {@link P13NIncludePredicate}
 */
@Link
@RequestMapping
public class P13NContainerHandler extends PageHandlerBase {

  private static final String UNRESOLVABLE_PROPERTY_NAME = "unresolvableProperty";

  private static final String ID_VARIABLE = "id";
  private static final String PROPERTY_PATH_VARIABLE = "propertyPath";

  /**
   * URI pattern, for URIs like "/dynamic/container/p13n/coolsegment/id"
   */
  public static final String DYNAMIC_CONTAINER_URI_PATTERN = '/' + PREFIX_DYNAMIC +
          '/' + SEGMENTS_CONTAINER +
          '/' + SEGMENT_P13N +
          "/{" + SEGMENT_ROOT + '}' +
          "/{" + ID_VARIABLE + ":" + PATTERN_NUMBER + "}" +
          "/{" + PROPERTY_PATH_VARIABLE + '}';

  private ContentRepository contentRepository;

  @GetMapping(value = DYNAMIC_CONTAINER_URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ROOT) String context,
                                            @PathVariable(ID_VARIABLE) int contentId,
                                            @PathVariable(PROPERTY_PATH_VARIABLE) String propertyPath,
                                            @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                            HttpServletRequest request) {
    Content content = contentRepository.getContent(IdHelper.formatContentId(contentId));
    ContentBean contentBean = getContentBeanFactory().createBeanFor(content, ContentBean.class);
    Navigation navigation = getNavigation(context);

    if (!(contentBean instanceof CMTeasable) || navigation == null) {
      return HandlerHelper.notFound();
    }

    request.setAttribute(ABSOLUTE_URI_KEY, true);
    Page page = asPage(navigation, navigation, UserVariantHelper.getUser(request));
    ModelAndView modelAndView = createModelAndView((CMTeasable) contentBean, propertyPath, navigation, view);
    RequestAttributeConstants.setPage(modelAndView, page);
    return modelAndView;
  }

  @Link(type = {DynamizableCMTeasableContainer.class}, view = VIEW_FRAGMENT, uri = DYNAMIC_CONTAINER_URI_PATTERN)
  public UriComponents buildLink(DynamizableCMTeasableContainer container, UriTemplate uriPattern, Map<String, Object> linkParameters) {
    Navigation context = getContextHelper().currentSiteContext();
    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);
    return result.buildAndExpand(
            ImmutableMap.of(
                    SEGMENT_ROOT, getPathSegments(context).get(0),
                    ID_VARIABLE, container.getTeasable().getContentId(),
                    PROPERTY_PATH_VARIABLE, container.getPropertyPath() != null ? container.getPropertyPath() : "items"
            ));
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public ContentRepository getContentRepository() {
    return contentRepository;
  }


  @NonNull
  private ModelAndView createModelAndView(@NonNull CMTeasable cmTeasable,
                                          @NonNull String propertyPath,
                                          @NonNull Navigation context,
                                          @Nullable String view) {
    DynamizableCMTeasableContainer container = new DynamizableCMTeasableContainer(cmTeasable, propertyPath);
    try {
      container.resolveItems();
    } catch (Exception e) {
      return createPropertyPathUnresolvableError(cmTeasable, propertyPath);
    }

    ModelAndView modelAndView = HandlerHelper.createModelWithView(container, view);
    NavigationLinkSupport.setNavigation(modelAndView, context);

    return modelAndView;
  }

  @NonNull
  private static ModelAndView createPropertyPathUnresolvableError(@NonNull CMTeasable cmTeasable, @NonNull String propertyPath) {

    LOG.warn("No propertyPath named {} found for {}.", propertyPath, cmTeasable.getContent().getPath());

    ModelAndView modelAndView = notFound("No property found for name '" + propertyPath + "'");
    modelAndView.setViewName(UNRESOLVABLE_PROPERTY_NAME);
    if (cmTeasable instanceof Navigation) {
      NavigationLinkSupport.setNavigation(modelAndView, (Navigation) cmTeasable);
    }
    modelAndView.addObject(PROPERTY_PATH_VARIABLE, propertyPath);
    return modelAndView;
  }
}
