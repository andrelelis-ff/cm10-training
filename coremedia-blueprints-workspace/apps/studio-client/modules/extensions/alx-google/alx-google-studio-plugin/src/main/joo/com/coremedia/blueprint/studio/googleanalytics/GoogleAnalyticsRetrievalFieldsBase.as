package com.coremedia.blueprint.studio.googleanalytics {
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentProperties;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

public class GoogleAnalyticsRetrievalFieldsBase extends PropertyFieldGroup {

  private static const GOOGLE_ANALYTICS:String = 'googleAnalytics';
  private static const P12_FILE:String = 'p12File';
  private static const LOCAL_SETTINGS:String = 'localSettings';
  private static const CM_DOWNLOAD:String = "CMDownload";

  private var p12FileVE:ValueExpression;
  private var localSettings:RemoteBean;

  public function GoogleAnalyticsRetrievalFieldsBase(config:GoogleAnalyticsRetrievalFields = null) {
    super(config);
    updateP12FileFromStruct();
    getP12FileVE().addChangeListener(updateStruct);
    bindTo.addChangeListener(updateP12FileFromStruct);
  }

  private function updateStruct():void {
    var value:Array = getP12FileVE().getValue();
    if (value && value.length > 0) {
      applyToStruct(bindTo.getValue(), CM_DOWNLOAD, P12_FILE, value[0]);
    } else {
      removeLinkFromStruct(bindTo.getValue(), P12_FILE);
    }
  }

  private static function removeLinkFromStruct(content:Content, structPropertyName:String):void {
    var struct:Struct = content.getProperties().get(LOCAL_SETTINGS);
    if (struct) {
      var googleAnalytics:Struct = getStruct(struct, GOOGLE_ANALYTICS);
      if (googleAnalytics) {
        googleAnalytics.getType().removeProperty(structPropertyName);
      }
    }
  }

  private static function getStruct(struct:Struct, key:String):Struct {
    return struct.get(key);
  }


  protected function getP12FileVE():ValueExpression {
    if (!p12FileVE) {
      p12FileVE = ValueExpressionFactory.createFromValue([]);
    }
    return p12FileVE;
  }

  private function updateP12FileFromStruct():void {
    var c:Content = bindTo.getValue();
    c.load(function ():void {
      var props:ContentProperties = c.getProperties();
      var init:Boolean = false;
      if (!localSettings) {
        init = true;
      }
      localSettings = props.get(LOCAL_SETTINGS) as RemoteBean;
      if (init) {
        localSettings.addPropertyChangeListener(GOOGLE_ANALYTICS, updateP12FileFromLocalSettings);
      }
      localSettings.load(function ():void {
        updateP12FileFromLocalSettings();
      });
    });
  }

  private function updateP12FileFromLocalSettings():void {
    var googleAnalytics:Struct = getStruct(localSettings as Struct, GOOGLE_ANALYTICS);
    if (googleAnalytics) {
      var p12File:Struct = googleAnalytics.get(P12_FILE);
      if (!p12File) {
        getP12FileVE().setValue([]);
      } else {
        getP12FileVE().setValue([p12File]);
      }
    }
  }

  private function applyToStruct(content:Content, contentType:String, structPropertyName:String, link:Content):void {
    var struct:Struct = content.getProperties().get(LOCAL_SETTINGS);

    //the substruct can be created on the fly but isn't loaded, so we trigger an invalidate in this case
    var googleAnalytics:Struct = getStruct(struct, GOOGLE_ANALYTICS);
    if(!googleAnalytics) {
      struct.getType().addStructProperty(GOOGLE_ANALYTICS);
      content.invalidate(function():void {
        applyToStruct(content, contentType, structPropertyName, link);
      });
      return;
    }

    var capType:CapType = SESSION.getConnection().getContentRepository().getContentType(contentType);
    googleAnalytics.getType().addLinkProperty(structPropertyName, capType, link);

    // apply the link again: in case the substruct had to be created previously,
    // we need to notify the component about the missed initialization
    getP12FileVE().setValue([link]);
  }

  override protected function onDestroy():void {
    super.onDestroy();
    localSettings.removePropertyChangeListener(GOOGLE_ANALYTICS, updateP12FileFromLocalSettings);
    getP12FileVE().removeChangeListener(updateStruct);
    bindTo.removeChangeListener(updateP12FileFromStruct);
  }
}
}