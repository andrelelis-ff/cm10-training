package com.coremedia.blueprint.studio.externalpreview.dialog {

import com.coremedia.blueprint.studio.externalpreview.ExternalPreviewStudioPluginBase;
import com.coremedia.cms.editor.sdk.components.StudioDialog;

import ext.Ext;
import ext.StringUtil;

/**
 * Base class for the external preview help dialog.
 */
[ResourceBundle('com.coremedia.blueprint.studio.externalpreview.ExternalPreviewStudioPlugin')]
public class ExternalPreviewWindowBaseBase extends StudioDialog {
  internal static const WINDOW_FEATURES:String = "menubar=yes,resizable=yes,scrollbars=yes,status=yes,location=yes";

  public function ExternalPreviewWindowBaseBase(config:ExternalPreviewWindow = null) {
    super(config);
  }

  /**
   * Creates a mail with the preview URL link.
   */
  protected function mailPreviewLink():void {
    //create the subject
    var subject:String = resourceManager.getString('com.coremedia.blueprint.studio.externalpreview.ExternalPreviewStudioPlugin', 'ExternalPreview_mail_subject');
    var body:String = StringUtil.format(resourceManager.getString('com.coremedia.blueprint.studio.externalpreview.ExternalPreviewStudioPlugin', 'ExternalPreview_mail_body'),
            ExternalPreviewStudioPluginBase.PREVIEW_URL);

    //encode whole url
    var params:String = Ext.urlEncode({subject:subject, body:body});
    var url:String = Ext.urlAppend("mailto:", params);

    //regular javascript mail to link.
    window.open(url, 'emailWindow');
  }

  /**
   * Formats the given string to format/cut the link url.
   * @param text
   * @return
   */
  protected static function formatText(text:String):String {
    if(text.length > 60) {
      text = text.substring(0,59) + "...";
    }
    return text;
  }

  protected function getHyperlink():String {
      return '<a href="' + ExternalPreviewStudioPluginBase.PREVIEW_URL + '" target="_blank">' +
              ExternalPreviewStudioPluginBase.PREVIEW_URL + '</a>';
  }

  protected function closeDialog():void {
    hide();
  }

  override public function close():void {
    hide();
  }
}
}