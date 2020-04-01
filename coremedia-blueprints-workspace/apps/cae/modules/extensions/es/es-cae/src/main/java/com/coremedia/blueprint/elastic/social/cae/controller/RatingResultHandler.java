package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;

@Link
@RequestMapping
public class RatingResultHandler extends ElasticContentHandler<RatingResult> {

  private static final String RATING_PREFIX = "rating";
  public static final String RATING_PARAMETER = "rating";

  /**
   * URI pattern, for URIs like "/dynamic/fragment/rating/{segment}/{contextId}/{id}"
   */
  public static final String DYNAMIC_PATTERN_RATING = "/" + PREFIX_DYNAMIC +
          "/" + SEGMENTS_FRAGMENT +
          "/" + RATING_PREFIX +
          "/{" + ROOT_SEGMENT + "}" +
          "/{" + CONTEXT_ID + "}" +
          "/{" + ID + "}";

  @GetMapping(value = DYNAMIC_PATTERN_RATING)
  public ModelAndView getRating(@PathVariable(CONTEXT_ID) String contextId,
                                @PathVariable(ID) String id,
                                @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                HttpServletRequest request) {

    Navigation navigation = getNavigation(contextId);

    Object contributionTarget = fetchContributionTarget(request, id);
    if (contributionTarget == null) {
      return HandlerHelper.notFound();
    }

    Object[] beans = getBeansForSettings(contributionTarget, navigation).toArray();
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    RatingResult commentsResult = new RatingResult(contributionTarget, getElasticSocialUserHelper().getCurrentUser(),
            getElasticSocialService(), elasticSocialConfiguration.isFeedbackEnabled(), elasticSocialConfiguration.getRatingType());

    ModelAndView modelWithView = HandlerHelper.createModelWithView(commentsResult, view);
    NavigationLinkSupport.setNavigation(modelWithView, navigation);

    return modelWithView;
  }

  @PostMapping(value = DYNAMIC_PATTERN_RATING)
  public ModelAndView createRating(@PathVariable(CONTEXT_ID) String contextId,
                                   @PathVariable(ID) String targetId,
                                   @RequestParam(value = RATING_PARAMETER) int rating,
                                   @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                   HttpServletRequest request) {

    Navigation navigation = getNavigation(contextId);

    Object contributionTarget = fetchContributionTarget(request, targetId);
    if (contributionTarget == null) {
      return HandlerHelper.notFound();
    }

    // workaround to prevent creating anonymous users when no comment can be written because of validation errors etc.
    CommunityUser author = getElasticSocialUserHelper().getCurrentUser();
    HandlerInfo result = createResult(request, navigation, author, contributionTarget);
    if (result.isSuccess()) {
      getElasticSocialService().updateRating(author, contributionTarget, navigation.getContext(), rating);
    }

    return HandlerHelper.createModelWithView(result, view);
  }

  @Override
  protected void validateEnabled(HandlerInfo handlerInfo, CommunityUser user, Navigation navigation, @Nullable User developer, Object... beans) {
    ElasticSocialConfiguration elasticSocialConfiguration = getElasticSocialConfiguration(beans);
    // user == null was not allowed in previous versions, removed because user filter handling not fix
    if (!elasticSocialConfiguration.isRatingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.RATING_FORM_ERROR_NOT_ENABLED);
    } else if ((user == null || user.isAnonymous()) && !elasticSocialConfiguration.isAnonymousRatingEnabled()) {
      addErrorMessage(handlerInfo, null, navigation, developer, ContributionMessageKeys.RATING_FORM_NOT_LOGGED_IN);
    }
  }


  // ---------------------- building links ---------------------------------------------------------------------
  @Link(type = RatingResult.class, view = VIEW_FRAGMENT, uri = DYNAMIC_PATTERN_RATING)
  public UriComponents buildFragmentLink(RatingResult RatingResult,
                                         UriTemplate uriTemplate,
                                         Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    return buildFragmentUri(SiteHelper.getSiteFromRequest(request), RatingResult, uriTemplate, linkParameters);
  }

  @Link(type = RatingResult.class, uri = DYNAMIC_PATTERN_RATING)
  public UriComponents buildInfoLink(RatingResult RatingResult, UriTemplate uriTemplate, HttpServletRequest request) {
    return getUriComponentsBuilder(SiteHelper.getSiteFromRequest(request), RatingResult, uriTemplate).build();
  }
}
