package com.coremedia.blueprint.assets.studio;

/**
 * The place to store asset management configuration.
 */
public class AssetManagementConfiguration {
  private String settingsDocument;

  /**
   * Return the name of the asset management settings document (currently storing available channels & regions)
   * @return the name of the settings document
   */
  public String getSettingsDocument() {
    return settingsDocument;
  }

  /**
   * Set the name of the asset management settings document.
   * @param settingsDocument the name of the settings document
   */
  public void setSettingsDocument(String settingsDocument) {
    this.settingsDocument = settingsDocument;
  }
}
