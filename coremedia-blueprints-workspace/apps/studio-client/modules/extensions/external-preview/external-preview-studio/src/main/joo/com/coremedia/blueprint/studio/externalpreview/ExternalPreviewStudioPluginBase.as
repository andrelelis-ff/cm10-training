package com.coremedia.blueprint.studio.externalpreview {

import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;

/**
 * Updates the external preview resources that updates the preview CAE with
 * the current active tabs.
 */
public class ExternalPreviewStudioPluginBase extends StudioPlugin {

  public static var REST_URL:String;
  public static var PREVIEW_URL:String;
  public static var CONTENT_PREVIEW_URL_PREFIX:String;

  public function ExternalPreviewStudioPluginBase(config:ExternalPreviewStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    loadSettings();
  }

  /**
   * Retrieves the server settings for the external preview.
   */
  private function loadSettings():void {
    var settingsBean:RemoteBean = beanFactory.getRemoteBean('externalpreview/config');
    settingsBean.load(function ():void {
      var settings:Object = settingsBean.toObject();
      REST_URL = settings.restUrl;
      PREVIEW_URL = settings.previewUrl;
      CONTENT_PREVIEW_URL_PREFIX = settings.urlPrefix;
    });
  }
}
}
