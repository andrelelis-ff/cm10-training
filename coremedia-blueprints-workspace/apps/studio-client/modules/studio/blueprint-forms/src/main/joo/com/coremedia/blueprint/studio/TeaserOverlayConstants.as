package com.coremedia.blueprint.studio {
import com.coremedia.cap.content.ContentPropertyNames;

/**
 * Constants for Teaser Overlays.
 */
public class TeaserOverlayConstants {

  /**
   * Property path from content to teaser overlay configuration.
   */
  public static const DEFAULT_SETTINGS_PATH:String = ContentPropertyNames.PROPERTIES + ".localSettings.teaserOverlay";

  /**
   * Array of paths to look the style descriptors up.
   *
   * Paths not starting with / are relative to the site folder of the teaser content the overlay is used for.
   * Paths starting with / are absolute (relative from root)
   */
  public static const DEFAULT_STYLE_DESCRIPTOR_FOLDER_PATHS:Array = [
    "Options/Settings/Teaser Styles/",
    "/Settings/Options/Settings/Teaser Styles/"
  ];

  /**
   * The name of the style descriptor content that is used as a default.
   */
  public static const DEFAULT_STYLE_NAME:String = "Default";
}
}