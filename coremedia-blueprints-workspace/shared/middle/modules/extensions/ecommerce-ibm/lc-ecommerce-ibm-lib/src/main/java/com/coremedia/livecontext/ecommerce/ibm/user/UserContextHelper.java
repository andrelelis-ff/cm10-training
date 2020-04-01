package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Helper class to build an "IBM WCS conform" user context.
 * You do not have to know the exact keys if you use the helper method.
 * Use this class as static import.
 */
public class UserContextHelper {

  private UserContextHelper() {
  }

  @Nullable
  public static String getForUserName(@Nullable UserContext context) {
    if (context == null) {
      return null;
    }

    Object value = context.getUserName();
    if (value == null) {
      return null;
    }

    return (String) value;
  }

  @Nullable
  public static Integer getForUserId(@Nullable UserContext context) {
    if (context == null) {
      return null;
    }

    Object value = context.getUserId();
    if (value == null) {
      return null;
    }

    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException ignored) {
      //ignore
    }

    return null;
  }

  public static boolean isAnonymousId(@Nullable Integer userId) {
    return userId == null || userId < 0;
  }
}
