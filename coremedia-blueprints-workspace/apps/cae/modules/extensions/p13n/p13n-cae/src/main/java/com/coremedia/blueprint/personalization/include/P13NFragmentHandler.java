package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMDynamicList;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch;
import com.coremedia.blueprint.personalization.contentbeans.CMSelectionRules;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
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

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;

/**
 * Handle dynamic/personalized personalized content via esi/client include.
 * see also {@link P13NIncludePredicate}
 */
@Link
@RequestMapping
public class P13NFragmentHandler extends PageHandlerBase {

  private static final String URI_PREFIX = "p13n";
  private static final String ID_VARIABLE = "id";

  /**
   * URI pattern, for URIs like "/dynamic/fragment/p13n/perfectchef/id"
   */
  public static final String DYNAMIC_URI_PATTERN = '/' + PREFIX_DYNAMIC +
          '/' + SEGMENTS_FRAGMENT +
          '/' + URI_PREFIX +
          "/{" + SEGMENT_ROOT + '}' +
          "/{" + ID_VARIABLE + ":" + PATTERN_NUMBER + "}";

  private ContentRepository contentRepository;


  @GetMapping(value = DYNAMIC_URI_PATTERN)
  public ModelAndView handleFragmentRequest(@PathVariable(SEGMENT_ROOT) String context,
                                            @PathVariable(ID_VARIABLE) int contentId,
                                            @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                            HttpServletRequest request) {
    Content content = contentRepository.getContent(IdHelper.formatContentId(contentId));
    ContentBean contentBean = getContentBeanFactory().createBeanFor(content, ContentBean.class);
    Navigation navigation = getNavigation(context);

    if (contentBean != null && navigation != null
            && (contentBean instanceof CMP13NSearch || contentBean instanceof CMSelectionRules)) {
      // add navigationContext as navigationContext request param
      ModelAndView modelWithView = HandlerHelper.createModelWithView(contentBean, view);
      NavigationLinkSupport.setNavigation(modelWithView, navigation);

      // need to add compose and add page to request here in order to get settings for image transformation later on...
      Page page = asPage(navigation, navigation, UserVariantHelper.getUser(request));
      addPageModel(modelWithView, page);

      return modelWithView;
    }
    return HandlerHelper.notFound();
  }

  @Link(type = {CMSelectionRules.class, CMP13NSearch.class}, view = VIEW_FRAGMENT, uri = DYNAMIC_URI_PATTERN)
  public UriComponents buildFragmentLink(CMDynamicList dynamicList, UriTemplate uriPattern, Map<String, Object> linkParameters) {
    return buildLinkInternal(dynamicList, uriPattern, linkParameters);
  }

  private UriComponents buildLinkInternal(CMLinkable cmLinkable, UriTemplate uriPattern, Map<String, Object> linkParameters) {
    Navigation context = getContextHelper().currentSiteContext();
    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);
    return result.buildAndExpand(
            ImmutableMap.of(
                    SEGMENT_ROOT, getPathSegments(context).get(0),
                    ID_VARIABLE, cmLinkable.getContentId()
            ));
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public ContentRepository getContentRepository() {
    return contentRepository;
  }

}
