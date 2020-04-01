package com.coremedia.blueprint.elastic.social.cae.user;

import com.coremedia.elastic.social.api.users.CommunityUser;

/**
 * The UserContext allows to set and retrieve the logged in user of the current {@link Thread}.
 */
public final class UserContext {
  private static final ThreadLocal<CommunityUser> USER_THREAD_LOCAL = new ThreadLocal<>();

  private UserContext() {
  }

  /**
   * Returns the logged in user of the current {@link Thread}.
   *
   * @return the current logged in user or null if no user is logged in.
   */
  public static CommunityUser getUser() {
    return USER_THREAD_LOCAL.get();
  }

  /**
   * Sets the user for the current {@link Thread}.
   *
   * @param user the user for the current {@link Thread}
   * @throws IllegalArgumentException when a <code>null</code> value is passed.
   */
  public static void setUser(CommunityUser user) {
    if (user == null) {
      throw new IllegalArgumentException("no user given");
    }
    USER_THREAD_LOCAL.set(user);
  }

  /**
   * Clears the user of the current {@link Thread}.
   */
  public static void clear() {
    USER_THREAD_LOCAL.remove();
  }
}
