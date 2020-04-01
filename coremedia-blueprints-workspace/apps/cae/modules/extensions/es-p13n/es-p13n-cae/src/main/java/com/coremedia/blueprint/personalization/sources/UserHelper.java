package com.coremedia.blueprint.personalization.sources;

import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.elastic.social.api.users.CommunityUser;

/**
 * Provides convenience methods to handle Elastic Social's {@link com.coremedia.elastic.core.api.users.User} Authentication.
 */
final class UserHelper {
  private UserHelper() {
  }

  /**
   * Returns the Elastic Social {@link com.coremedia.elastic.core.api.users.User} currently logged in.
   *
   * @return The Elastic Social {@link com.coremedia.elastic.core.api.users.User} currently logged in.
   */
  public static CommunityUser getLoggedInUser() {
    CommunityUser user = UserContext.getUser();
    return user == null || user.isAnonymous() ? null : user;
  }
}
