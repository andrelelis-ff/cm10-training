package com.coremedia.livecontext.studio.action {
import com.coremedia.cms.editor.sdk.quickcreate.OpenQuickCreateAction;
import com.coremedia.cms.editor.sdk.quickcreate.QuickCreateDialog;
import com.coremedia.cms.editor.sdk.quickcreate.processing.ProcessingData;

import ext.Component;
import ext.Ext;
import ext.window.Window;

import net.jangaroo.net.URI;
import net.jangaroo.net.URIUtils;

public class OpenCreateExternalPageDialogActionBase extends OpenQuickCreateAction {

  public static const EXTERNAL_ID_PROPERTY:String = 'externalId';
  public static const EXTERNAL_URI_PATH_PROPERTY:String = 'externalUriPath';
  internal static const KNOWN_NON_SEO_PARAMS:Array = ['storeId', 'catalogId', 'langId'];

  private var data:Object;

  public function OpenCreateExternalPageDialogActionBase(config:OpenQuickCreateAction = null) {
    super(config);
    data = undefined;
  }

  protected override function getDialogConfig(trigger:Component):Window {
    //create the dialog
    var dialogConfig:QuickCreateDialog = QuickCreateDialog(Ext.apply({}, Ext.apply({}, initialConfig)));
    dialogConfig.model = new ProcessingData();
    dialogConfig.model.set(EXTERNAL_ID_PROPERTY, data.pageId);
    dialogConfig.model.set(ProcessingData.NAME_PROPERTY, data.pageId);
    var previewUrl:String = data.shopUrl;
    if (previewUrl){
      var uri:URI = URIUtils.parse(previewUrl);
      if (!isSeoUrl(previewUrl)) {
        var path:String = uri.path.split('/').pop();
        var query:String = replaceKnownQueryParameters(uri.query);
        dialogConfig.model.set(EXTERNAL_URI_PATH_PROPERTY, path + '?' + query);
      }
    }

    return dialogConfig;
  }

  internal function setData(data:Array) {
    if(Ext.isEmpty(data)) {
      this.data = undefined;
    } else {
      this.data = data[0];
    }
  }

  override protected function calculateDisabled():Boolean {
    if(this.data === undefined) {
      return undefined;
    }

    return !data.shopUrl || !data.pageId;
  }

  private static function replaceKnownQueryParameters(queryStr:String):String {
    var queryParamMap:Object = Ext.urlDecode(queryStr);
    var result:Array = [];
    for (var key:String in queryParamMap) {
      if (KNOWN_NON_SEO_PARAMS.indexOf(key) > -1) {
        result.push(key + '={' + key + '}');
      } else {
        result.push(key + '=' + queryParamMap[key]);
      }
    }
    return result.join('&');
  }

  private static function isSeoUrl(url:String):Boolean {
    return KNOWN_NON_SEO_PARAMS.every(function (s:String):Boolean {
      return url.indexOf(s) < 0;
    });
  }

}
}
