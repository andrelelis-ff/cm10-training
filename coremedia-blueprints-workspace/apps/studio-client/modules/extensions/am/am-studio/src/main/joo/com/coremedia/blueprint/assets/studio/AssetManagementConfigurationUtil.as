package com.coremedia.blueprint.assets.studio {

import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.StudioConfigurationUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

/**
 * Utility class for easy access to asset management configuration.
 */
public class AssetManagementConfigurationUtil {
  private static var configuredRightsChannelsValueExpression:ValueExpression;
  private static var configuredRightsRegionsValueExpression:ValueExpression;

  /**
   * Return a value expression evaluating to the list of channels configured in the asset management settings document.
   * @return a value expression evaluating to the list of channels
   */
  public static function getConfiguredRightsChannelsValueExpression():ValueExpression {
    if (!configuredRightsChannelsValueExpression) {
      configuredRightsChannelsValueExpression = ValueExpressionFactory.createFromFunction(
              StudioConfigurationUtil.getConfiguration,
              getSettingsDocumentName(),
              AssetConstants.PROPERTY_ASSET_METADATA_CHANNELS);
    }
    return configuredRightsChannelsValueExpression;
  }

  /**
   * Return a value expression evaluating to the list of regions configured in the asset management settings document.
   * @return a value expression evaluating to the list of regions
   */
  public static function getConfiguredRightsRegionsValueExpression():ValueExpression {
    if (!configuredRightsRegionsValueExpression) {
      configuredRightsRegionsValueExpression = ValueExpressionFactory.createFromFunction(
              StudioConfigurationUtil.getConfiguration,
              getSettingsDocumentName(),
              AssetConstants.PROPERTY_ASSET_METADATA_REGIONS);
    }
    return configuredRightsRegionsValueExpression;
  }

  /**
   * Return the name of the settings document for asset management.
   * @return the name of the settings document
   */
  public static function getSettingsDocumentName():String {
    var assetManagementConfig:Object = getAssetManagementConfiguration();
    return assetManagementConfig['settingsDocument'];
  }

  /**
   * Return the asset management configuration object.
   * See com.coremedia.blueprint.assets.studio.AssetManagementConfiguration.java for details.
   *
   * @return the configuration object
   */
  public static function getAssetManagementConfiguration():Object {
    return editorContext.getConfiguration()['assetManagement'];
  }

  public function AssetManagementConfigurationUtil() {
    throw new Error("Utility class AssetManagementConfigurationUtil must not be instantiated");
  }
}
}