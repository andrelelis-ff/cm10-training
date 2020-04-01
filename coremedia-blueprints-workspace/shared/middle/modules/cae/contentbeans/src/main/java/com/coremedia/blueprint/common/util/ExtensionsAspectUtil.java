package com.coremedia.blueprint.common.util;

import org.apache.commons.lang3.StringUtils;

public final class ExtensionsAspectUtil {
  private static final String FEATURE_IS_NOT_CONFIGURED = "This is the default configuration";
  public static final String EXTERNAL_ACCOUNT = ".external.account";

  // static utility class
  private ExtensionsAspectUtil() {}

  public static boolean isFeatureConfigured(String toCheck) {
    return StringUtils.isNotBlank(toCheck) && !toCheck.startsWith(FEATURE_IS_NOT_CONFIGURED);
  }
}
