package com.coremedia.blueprint.elastic.social.cae.user;

import com.coremedia.elastic.social.api.users.CommunityUser;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface PasswordExpiryPolicy {
  boolean isExpiredFor(@NonNull CommunityUser user);
}
