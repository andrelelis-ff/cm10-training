package com.coremedia.blueprint.lc.test;

import edu.umd.cs.findbugs.annotations.Nullable;

public class HoverflyTestHelper {

  private HoverflyTestHelper() {
  }

  /**
   * Return `true` if Hoverfly tapes <em>should</em> be used.
   */
  public static boolean useTapes() {
    return !ignoreTapes();
  }

  /**
   * Return `true` if Hoverfly tapes should <em>not</em> be used.
   */
  private static boolean ignoreTapes() {
    return "true".equals(getIgnoreTapes());
  }

  /**
   * Return the system property-configured flag, if the hoverfly extension and its tapes are disabled.
   */
  @Nullable
  private static Object getIgnoreTapes() {
    return System.getProperties().get("hoverfly.ignoreTapes");
  }
}
