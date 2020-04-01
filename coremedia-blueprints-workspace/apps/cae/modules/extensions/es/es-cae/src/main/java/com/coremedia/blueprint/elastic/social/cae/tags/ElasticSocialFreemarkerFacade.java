package com.coremedia.blueprint.elastic.social.cae.tags;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.blueprint.elastic.social.cae.controller.CommentsResult;
import com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResult;
import com.coremedia.blueprint.elastic.social.cae.guid.GuidCookieHandler;
import com.coremedia.blueprint.elastic.social.cae.user.ElasticSocialUserHelper;
import com.coremedia.elastic.core.api.tenant.TenantService;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.users.CommunityUser;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A Facade for utility functions used by FreeMarker templates.
 */
@Named
public class ElasticSocialFreemarkerFacade {

  @Inject
  private ElasticSocialService elasticSocialService;

  @Inject
  private TenantService tenantService;

  @Inject
  private ElasticSocialUserHelper elasticSocialUserHelper;

  @Inject
  private ElasticSocialPlugin elasticSocialPlugin;

  public boolean isLoginAction(Object bean) {
    return ElasticSocialFunctions.isLoginAction(bean);
  }

  /**
   * Checks if the current user of the web page is a logged-in user or it is an anonymous user.
   * @return <code>true</code> if the current user is not logged in otherwise <code>false</code>
   */
  public boolean isAnonymousUser() {
    return ElasticSocialFunctions.isAnonymousUser();
  }

  public CommunityUser getCurrentUser() {
    return elasticSocialUserHelper.getCurrentOrAnonymousUser();
  }

  /**
   * Checks if the user choose not to publish its user name, profile image, and other personal information with
   * its contributions.
   * @param communityUser the user to be checked
   * @return <code>true</code> if the user wants to remain anonymous otherwise <code>false</code>
   */
  public boolean isAnonymous(CommunityUser communityUser) {
    return ElasticSocialFunctions.isAnonymous(communityUser);
  }

  public boolean hasComplaintForCurrentUser(String id, String collection) {
    return ElasticSocialFunctions.hasComplaintForCurrentUser(id, collection);
  }

  public String getCurrentGuid() {
    return GuidCookieHandler.getCurrentGuid();
  }

  public CommentsResult getCommentsResult(Object target) {
    return new CommentsResult(target);
  }

  public ReviewsResult getReviewsResult(Object target) {
    return new ReviewsResult(target);
  }

  public long getNumberOfComments(Object target) {
    return elasticSocialService.getNumberOfComments(target);
  }

  public boolean hasUserWrittenReview(Object target) {
    Review review = elasticSocialService.getReview(elasticSocialUserHelper.getCurrentUser(), target);
    return review != null;
  }

  public boolean hasUserRated(Object target) {
    int rating = elasticSocialService.getRating(elasticSocialUserHelper.getCurrentUser(), target);
    return rating > 0;
  }

  public String getCurrentTenant() {
    return tenantService.getCurrent();
  }

  public ElasticSocialConfiguration getElasticSocialConfiguration(Page page) {
    return elasticSocialPlugin.getElasticSocialConfiguration(page);
  }
}
