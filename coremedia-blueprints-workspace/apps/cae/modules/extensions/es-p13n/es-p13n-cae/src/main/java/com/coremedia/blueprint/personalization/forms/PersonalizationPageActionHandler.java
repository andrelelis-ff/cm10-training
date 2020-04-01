package com.coremedia.blueprint.personalization.forms;

import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.personalization.elastic.InterestsService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static com.coremedia.objectserver.web.HandlerHelper.badRequest;
import static java.util.Collections.singletonList;

/**
 * Controller/linkschemes for personalization related actions that are performed on pages.
 */
@RequestMapping
@Link
public class PersonalizationPageActionHandler extends PageHandlerBase {


  public static final String URI_BASE = '/' + PREFIX_SERVICE +
          "/userdetails" +
          "/{" + SEGMENT_ROOT + "}" +
          "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}";

  public static final String EXPLICITINTEREST_URI = URI_BASE + "/interest/explicit";


  private InterestsService interestsService;

  @Required
  public void setInterestsService(InterestsService interestsService) {
    this.interestsService = interestsService;
  }

  /**
   * Handles update of explicit interests as ajax request and return an updated form as HTML snippet
   *
   * @param form The form containing the interests
   */
  @PostMapping(value = EXPLICITINTEREST_URI)
  public ModelAndView handlePostExplicitInterestsAjax(
          @PathVariable(SEGMENT_ROOT) String context,
          @PathVariable(SEGMENT_ID) ContentBean contentBean,
          @ModelAttribute() PersonalizationForm form,
          BindingResult bindingResult,
          HttpServletRequest request) {

    if (!(contentBean instanceof CMAction)) {
      return badRequest();
    }

    // compute page
    Navigation navigation = getNavigation(singletonList(context));
    Page page = asPage(navigation, (CMAction) contentBean, UserVariantHelper.getUser(request));

    // perform update
    PersonalizationForm updatedForm = (PersonalizationForm)
            interestsService.updateExplicitInterests(null, form, bindingResult);

    // see PersonalizationForm.explicitInterests.jsp
    ModelAndView result = HandlerHelper.createModelWithView(updatedForm, "ajax");
    addPageModel(result, page);
    return result;
  }

  /**
   * Handles GET request on explicit interests via AJAX and provides a {@link PersonalizationForm} as an HTML snippet
   */
  @GetMapping(value = EXPLICITINTEREST_URI)
  public ModelAndView handleGetExplicitInterestsAjax(@PathVariable(SEGMENT_ROOT) String context,
                                                     @PathVariable(SEGMENT_ID) ContentBean contentBean,
                                                     HttpServletRequest request) {

    if (!(contentBean instanceof CMAction)) {
      return badRequest();
    }
    CMAction action = (CMAction) contentBean;

    // compute page
    Navigation navigation = getNavigation(singletonList(context));
    Page page = asPage(navigation, action, UserVariantHelper.getUser(request));

    final PersonalizationForm form = interestsService.getExplicitInterests(page);

    // see PersonalizationForm.explicitInterests.jsp
    final ModelAndView result = HandlerHelper.createModelWithView(form, "ajax");
    addPageModel(result, page);
    return result;

  }


  /**
   * Builds a link for  {@link #handleGetExplicitInterestsAjax} and {@link #handlePostExplicitInterestsAjax}
   */
  @Link(type = PersonalizationForm.class, view = "ajax", parameter = "page", uri = EXPLICITINTEREST_URI)
  public URI buildExplicitInterestsAjaxLink(PersonalizationForm form, Map<String, Object> linkParameters, UriTemplate uriTemplate, HttpServletRequest request) {
    Page page = (Page) linkParameters.get("page");
    List<String> segmentNames = getPathSegments(page.getNavigation());
    //todo page.getContentId() may be null for non-CMS content beans rendered as main content on a page
    Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put(SEGMENT_ID, page.getContentId())
            .put(SEGMENT_ROOT, segmentNames.get(0))
            .build();
    return uriTemplate.expand(parameters);
  }
}
