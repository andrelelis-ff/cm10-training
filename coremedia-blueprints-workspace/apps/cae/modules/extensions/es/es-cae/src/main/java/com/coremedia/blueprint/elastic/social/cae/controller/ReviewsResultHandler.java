package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.objectserver.web.links.Link;
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
public class ReviewsResultHandler extends AbstractReviewsResultHandler {

  private static final String REVIEWS_PREFIX = "reviews";

  /**
   * URI pattern, for URIs like "/dynamic/fragment/reviews/{segment}/{contextId}/{id}"
   */
  public static final String DYNAMIC_PATTERN_REVIEWS = "/" + PREFIX_DYNAMIC +
          "/" + SEGMENTS_FRAGMENT +
          "/" + REVIEWS_PREFIX +
          "/{" + ROOT_SEGMENT + "}" +
          "/{" + CONTEXT_ID + "}" +
          "/{" + ID + "}";

  @GetMapping(value = DYNAMIC_PATTERN_REVIEWS)
  public ModelAndView getReviews(@PathVariable(CONTEXT_ID) String contextId,
                                 @PathVariable(ID) String targetId,
                                 @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view,
                                 HttpServletRequest request) {
    return handleGetReviews(SiteHelper.getSiteFromRequest(request), contextId, targetId, view);
  }

  @PostMapping(value = DYNAMIC_PATTERN_REVIEWS)
  public ModelAndView createReview(@PathVariable(CONTEXT_ID) String contextId,
                                   @PathVariable(ID) String targetId,
                                   @RequestParam(value = "text", required = false) String text,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "rating", required = false) Integer rating,
                                   HttpServletRequest request) {
    return handleCreateReview(contextId, targetId, text, title, rating, request);
  }

  // ---------------------- building links ---------------------------------------------------------------------
  @Link(type = ReviewsResult.class, view = VIEW_FRAGMENT, uri = DYNAMIC_PATTERN_REVIEWS)
  public UriComponents buildFragmentLink(ReviewsResult reviewsResult,
                                         UriTemplate uriTemplate,
                                         Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    return super.buildFragmentUri(SiteHelper.getSiteFromRequest(request), reviewsResult, uriTemplate, linkParameters);
  }

  @Link(type = ReviewsResult.class, uri = DYNAMIC_PATTERN_REVIEWS)
  public UriComponents buildInfoLink(ReviewsResult reviewsResult, UriTemplate uriTemplate, HttpServletRequest request) {
    return getUriComponentsBuilder(SiteHelper.getSiteFromRequest(request), reviewsResult, uriTemplate).build();
  }

  @Override
  protected ReviewsResult getReviewsResult(Object target, boolean enabled, ContributionType contributionType, ElasticSocialConfiguration elasticSocialConfiguration) {
    return new ReviewsResult(target, getElasticSocialUserHelper().getCurrentUser(), getElasticSocialService(), enabled, contributionType, elasticSocialConfiguration);
  }
}
