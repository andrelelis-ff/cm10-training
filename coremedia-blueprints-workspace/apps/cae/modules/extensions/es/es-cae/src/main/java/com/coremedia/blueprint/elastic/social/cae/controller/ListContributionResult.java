package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

/**
 * @cm.template.api
 */
public abstract class ListContributionResult<T> extends ContributionResult {

  private List<T> contributions;
  private List<T> rootContributions;

  public ListContributionResult(Object target) {
    super(target);
  }

  public ListContributionResult(Object target,
                                CommunityUser user,
                                ElasticSocialService service,
                                boolean feedbackEnabled,
                                ContributionType contributionType) {
    super(target, user, service, feedbackEnabled, contributionType);
  }

  public List<T> getContributions() {
    ensureLoaded();
    return contributions == null ? Collections.<T>emptyList() : ImmutableList.copyOf(contributions);
  }

  /**
   * @cm.template.api
   */
  public List<T> getRootContributions() {
    ensureLoaded();
    if (rootContributions == null) {
      rootContributions = findRootContributions();
    }
    return rootContributions == null ? Collections.<T>emptyList() : ImmutableList.copyOf(rootContributions);
  }

  /**
   * @cm.template.api
   */
  public int getNumberOfContributions() {
    ensureLoaded();
    return contributions == null ? 0 : contributions.size();
  }

  protected void setContributions(List<T> contributions) {
    this.contributions = contributions;
  }

  protected abstract List<T> findRootContributions();
}
