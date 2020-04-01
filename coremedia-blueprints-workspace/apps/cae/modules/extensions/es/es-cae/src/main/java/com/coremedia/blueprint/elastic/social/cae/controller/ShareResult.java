package com.coremedia.blueprint.elastic.social.cae.controller;


import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;

public class ShareResult extends ContributionResult {

  public ShareResult(Object target) {
    super(target);
  }

  public ShareResult(Object target,
                     CommunityUser user,
                     ElasticSocialService elasticSocialService,
                     boolean feedbackEnabled,
                     ContributionType contributionType) {
    super(target, user, elasticSocialService, feedbackEnabled, contributionType);
  }

  @Override
  protected void load() {
    // numberOfShares???
  }
}
