package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;

/**
 * @cm.template.api
 */
public class RatingResult extends ContributionResult {

  private int rating;
  private long numberOfRatings;
  private double averageRating;

  public RatingResult(Object target) {
    super(target);
  }

  public RatingResult(Object target, CommunityUser user, ElasticSocialService service, boolean feedbackEnabled, ContributionType contributionType) {
    super(target, user, service, feedbackEnabled, contributionType);
  }


  public int getRating() {
    return rating;
  }

  /**
   * @cm.template.api
   */
  public double getAverageRating() {
    return averageRating;
  }

  /**
   * @cm.template.api
   */
  public long getNumberOfRatings() {
    return numberOfRatings;
  }

  @Override
  protected void load() {
    rating = elasticSocialService.getRating(user, target);
    averageRating = elasticSocialService.getAverageRating(target);
    numberOfRatings = elasticSocialService.getNumberOfRatings(target);
  }
}
