package com.coremedia.livecontext.elastic.social.cae;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResult;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;


public class ProductReviewsResult extends ReviewsResult {

  public ProductReviewsResult(Object target) {
    super(target);
  }

  public ProductReviewsResult(Object target,
                              CommunityUser user,
                              ElasticSocialService service,
                              boolean feedbackEnabled,
                              ContributionType contributionType,
                              ElasticSocialConfiguration elasticSocialConfiguration) {
    super(target, user, service, feedbackEnabled, contributionType, elasticSocialConfiguration);
  }
}
