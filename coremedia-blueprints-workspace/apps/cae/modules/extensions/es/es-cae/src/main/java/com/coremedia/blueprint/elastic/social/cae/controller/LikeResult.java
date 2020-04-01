package com.coremedia.blueprint.elastic.social.cae.controller;


import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;

/**
 * @cm.template.api
 */
public class LikeResult extends ContributionResult {

  private boolean alreadyLiked;
  private long numberOfLikes;

  public LikeResult(Object target) {
    super(target);
  }

  public LikeResult(Object target,
                    CommunityUser user,
                    ElasticSocialService elasticSocialService,
                    boolean feedbackEnabled,
                    ContributionType contributionType) {
    super(target, user, elasticSocialService, feedbackEnabled, contributionType);
  }

  /**
   * @cm.template.api
   */
  public boolean hasLiked() {
    ensureLoaded();
    return alreadyLiked;
  }

  /**
   * @cm.template.api
   */
  public long getNumberOfLikes() {
    ensureLoaded();
    return numberOfLikes;
  }

  @Override
  protected void load() {
    alreadyLiked = elasticSocialService.hasLiked(user, target);
    numberOfLikes = elasticSocialService.getNumberOfLikes(target);
  }
}
