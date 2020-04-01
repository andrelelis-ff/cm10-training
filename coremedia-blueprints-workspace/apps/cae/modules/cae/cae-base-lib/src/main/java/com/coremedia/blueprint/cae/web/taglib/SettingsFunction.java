package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.base.settings.SettingsService;

/**
 * Static SettingsService utilities used in JSP Taglibs.
 * For Freemarker use {@link BlueprintFreemarkerFacade} instead.
 */
public final class SettingsFunction {

  /**
   * Hide Utility Class Constructor
   */
  private SettingsFunction() {
  }

  /**
   * @return a setting for an Object with a given Key, or null if no setting is found
   */
  public static Object setting(SettingsService settingsService, Object self, String key) {
    return setting(settingsService, self, key, null);
  }


  /**
   * @return a setting for an Object with a given Key, or the defaultValue if no setting is found
   */
  public static Object setting(SettingsService settingsService, Object self, String key, Object defaultValue) {
    return settingsService.settingWithDefault(key, Object.class, defaultValue, self);
  }
}
