package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;

import static com.coremedia.elastic.social.api.ContributionType.ANONYMOUS;
import static com.coremedia.elastic.social.api.ContributionType.DISABLED;
import static com.coremedia.elastic.social.api.ContributionType.READONLY;
import static com.coremedia.elastic.social.api.ContributionType.REGISTERED;

/**
 * @cm.template.api
 */
public abstract class ContributionResult {
  protected ElasticSocialService elasticSocialService;
  protected boolean feedbackEnabled = false;
  protected Object target;
  protected CommunityUser user;
  protected ContributionType contributionType = DISABLED;
  private boolean loaded = false;

  protected ContributionResult(Object target) {
    this.target = target;
  }

  public ContributionResult(Object target, CommunityUser user, ElasticSocialService service, boolean feedbackEnabled, ContributionType contributionType) {
    this.elasticSocialService = service;
    this.feedbackEnabled = feedbackEnabled;
    this.target = target;
    this.user = user;
    this.contributionType = contributionType;
  }

  protected synchronized void ensureLoaded() {
    if (elasticSocialService == null) {
      throw new IllegalStateException("ElasticSocialService is not defined, cannot load feedback");
    }
    if (!loaded) {
      if (isEnabled()) {
        load();
      }
      loaded = true;
    }
  }

  protected abstract void load();

  /**
   * @cm.template.api
   */
  public Object getTarget() {
    return target;
  }

  public CommunityUser getUser() {
    return user;
  }

  /**
   * @cm.template.api
   */
  public boolean isEnabled() {
    return feedbackEnabled && contributionType != DISABLED;
  }

  /**
   * @cm.template.api
   */
  public boolean isReadOnly() {
    return isEnabled() && contributionType == READONLY;
  }

  protected ElasticSocialService getElasticSocialService() {
    return elasticSocialService;
  }

  public ContributionType getContributionType() {
    return contributionType == null ? DISABLED : contributionType;
  }

  public boolean isAnonymousContributingEnabled() {
    return getContributionType() == ANONYMOUS;
  }

  /**
   * @cm.template.api
   */
  public boolean isWritingContributionsEnabled() {
    return getContributionType() == REGISTERED || getContributionType() == ANONYMOUS;
  }

  /**
   * @cm.template.api
   */
  public boolean isWritingContributionsAllowed() {
    return isEnabled() && isWritingContributionsEnabled() && isWritingAllowedForUser();
  }

  public boolean isWritingAllowedForUser() {
    return isEnabled() && getContributionType() != READONLY
            && (isAnonymousContributingEnabled() || (null != user && !user.isAnonymous()));
  }
}
