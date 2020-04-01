package com.coremedia.blueprint.studio.rest.intercept;

/**
 * Configuration for the Theme Upload Interceptor
 */
public class ThemeConfiguration {
  private String developerGroups;

  public ThemeConfiguration(String developerGroups) {
    super();
    this.developerGroups = developerGroups;
  }

  /**
   * Useful to enable a feature (namely the theme upload) only for specific
   * developer groups.
   */
  public String getDeveloperGroups() {  // NOSONAR used via reflection
    return developerGroups;
  }
}
