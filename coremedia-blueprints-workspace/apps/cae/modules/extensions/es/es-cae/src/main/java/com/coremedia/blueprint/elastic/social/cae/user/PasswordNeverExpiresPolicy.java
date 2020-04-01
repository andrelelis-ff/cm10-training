package com.coremedia.blueprint.elastic.social.cae.user;

import com.coremedia.elastic.social.api.users.CommunityUser;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link com.coremedia.blueprint.elastic.social.cae.user.PasswordExpiryPolicy password expiry policy}
 * that never answers <code>true</code>.
 */
public class PasswordNeverExpiresPolicy implements PasswordExpiryPolicy {
  @Override
  public boolean isExpiredFor(@NonNull CommunityUser user) {
    return false;
  }
}
