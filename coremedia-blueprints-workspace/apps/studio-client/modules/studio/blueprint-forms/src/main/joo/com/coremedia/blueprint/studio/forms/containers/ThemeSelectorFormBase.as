package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.blueprint.base.components.localization.ContentLocalizationUtil;
import com.coremedia.blueprint.base.components.util.ContentLookupUtil;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentPropertyNames;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.fields.ComboBoxLinkPropertyField;
import com.coremedia.cms.editor.sdk.util.ImageUtil;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.ObjectUtils;

import ext.Template;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.ThemeSelector')]
public class ThemeSelectorFormBase extends ComboBoxLinkPropertyField {

  private static const NO_IMAGE_TOOLTIP:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.ThemeSelector', 'ThemeSelector_no_image_tooltip');

  public static const DEFAULT_PATHS:Array = ['/Themes/'];

  public static var DEFAULT_CROPPING:String = ImageUtil.getCroppingOperation(82, 50);

  protected static const DISPLAY_FIELD_NAME:String = "titleUnencoded";
  protected static const TITLE_FIELD_NAME:String = "title";
  protected static const DESCRIPTION_FIELD_NAME:String = "description";
  protected static const THUMBNAIL_URI_FIELD_NAME:String = "thumbnailUri";
  protected static const THUMBNAIL_TOOLTIP_FIELD_NAME:String = "thumbnailTooltip";

  protected static const COMBO_BOX_TEMPLATE:Template = getExtendedComboBoxTpl(TITLE_FIELD_NAME, DESCRIPTION_FIELD_NAME, THUMBNAIL_URI_FIELD_NAME, THUMBNAIL_TOOLTIP_FIELD_NAME, null);
  protected static const DISPLAY_TEMPLATE:Template = getExtendedDisplayTpl(TITLE_FIELD_NAME, DESCRIPTION_FIELD_NAME, THUMBNAIL_URI_FIELD_NAME, THUMBNAIL_TOOLTIP_FIELD_NAME, null);

  public function ThemeSelectorFormBase(config:ThemeSelectorForm = null) {
    super(config);
  }

  internal static function createAvailableThemesValueExpression(config:ThemeSelectorForm):ValueExpression {
    return ValueExpressionFactory.createFromFunction(computeAvailableLayouts, config);
  }

  private static function computeAvailableLayouts(config:ThemeSelectorForm):Array {
    var paths:Array = config.themesFolderPaths;
    var themeFolders:Array = [];
    for each(var path:String in paths) {
      var baseFolder:Content = SESSION.getConnection().getContentRepository().getChild(path);
      if (baseFolder === undefined) {
        themeFolders = undefined;
      }
      else if (baseFolder.isFolder()) {
        var subFolders:Array = baseFolder.getSubFolders();
        if (subFolders === undefined) {
          themeFolders = undefined;
        }
        for each(var folder:Content in subFolders) {
          if (folder === undefined) {
            themeFolders = undefined;
          } else if (folder.getPath() === undefined) {
            themeFolders = undefined;
          } else if (themeFolders) {
            themeFolders.push(folder.getPath());
          }
        }
      }
    }
    if (themeFolders === undefined) {
      return undefined;
    }
    //concat null to allow resetting
    return [null].concat(ContentLookupUtil.findContentsOfTypeInPaths(themeFolders, ["CMTheme"], config.bindTo.getValue()));
  }

  /**
   * Localization of the theme name. The display name is looked
   * up in the ThemeSelector_properties resource bundle.
   * The method returns undefined if the return value cannot be determined yet.
   * The value of the layout property (if set) takes precedence over
   * the content name for the purposes of localization.
   *
   * @param content the content identifying the theme
   * @return the formatted display name
   */
  public static function localizeText(content:Content):String {
    return ContentLocalizationUtil.localize(content, 'ThemeSelector_default_text', ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.blueprint.studio.ThemeSelector').content, getName);
  }

  private static function getName(content:Content):String {
    return content === null ? "" : content.getName();
  }

  /**
   * Localization of the theme description. The description is looked
   * up in the ThemeSelector_properties resource bundle.
   * The method returns undefined if the return value cannot be determined yet.
   *
   * @param content the content identifying the theme
   * @return the formatted description
   */
  public static function localizeDescription(content:Content):String {
    return ContentLocalizationUtil.localize(content, 'ThemeSelector_default_description', ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.blueprint.studio.ThemeSelector').content, null, getContentDescription);
  }

  private static function getContentDescription(content:Content):Object {
    return content
            ? ObjectUtils.getPropertyAt(content, [ContentPropertyNames.PROPERTIES, 'description'])
            : null;
  }

  public static function getThumbnailUri(content:Content):String {
    return content === null ? "" : editorContext.getThumbnailUri(content, DEFAULT_CROPPING);
  }

  public static function getThumbnailTooltip(content:Content):String {
    return getThumbnailUri(content) ? getName(content) : NO_IMAGE_TOOLTIP;
  }
}
}
