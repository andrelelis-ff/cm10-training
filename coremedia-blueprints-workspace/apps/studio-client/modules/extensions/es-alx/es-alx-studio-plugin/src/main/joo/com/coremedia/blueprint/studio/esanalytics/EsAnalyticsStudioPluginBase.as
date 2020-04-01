package com.coremedia.blueprint.studio.esanalytics {

import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.error.RemoteError;

public class EsAnalyticsStudioPluginBase extends StudioPlugin {

  /**
   * Value expression holding the analytics settings document (if it exists)
   */
  public static const SETTINGS:ValueExpression = ValueExpressionFactory.create("analyticsSettings");

  private static const ALX_SETTINGS_DOCUMENT:String = '/Settings/Options/Settings/AnalyticsSettings';

  public function EsAnalyticsStudioPluginBase(config:EsAnalyticsStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);
    fetchAnalyticsSettings();
  }

  internal static function fetchAnalyticsSettings():void {
    SESSION.getConnection().getContentRepository().getChild(ALX_SETTINGS_DOCUMENT, receiveAnalyticsSettings);
  }

  internal static function receiveAnalyticsSettings(content:Content, absPath:String, error:RemoteError):void{
    if(content) {
      SETTINGS.setValue(content);
    } else if(error.status === 403) {
      trace("[WARN] analytics settings not readable for current user");
      error.setHandled(true);
    } else {
      // inform user that alx settings are missing
      trace("[WARN] analytics settings are missing: create or import that document");
    }
  }

}
}