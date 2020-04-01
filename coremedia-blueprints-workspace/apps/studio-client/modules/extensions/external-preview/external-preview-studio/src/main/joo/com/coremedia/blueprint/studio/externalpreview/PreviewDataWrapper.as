package com.coremedia.blueprint.studio.externalpreview {
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;

import ext.JSON;

/**
 * Encapsulates all data that should be passed to the preview controller
 * so that the external preview HTML page can process it.
 */
public class PreviewDataWrapper {
  private var previewContent:Array = [];
  private var activeContent:Content;

  public function addContent(cnt:Content):void {
    this.previewContent.push(new PreviewDataItem(cnt));
  }

  public function setActiveContent(cnt:Content):void {
    this.activeContent = cnt;
  }

  public function isEmpty():Boolean {
    return previewContent.length === 0;
  }

  public function asRequestParameters():Object {
    var data:Array = [];
    for(var i:int = 0; i<previewContent.length; i++) {
      var item:PreviewDataItem = previewContent[i] as PreviewDataItem;
      item.setActive(false);
      item.setActive(item.getContentId() === IdHelper.parseContentId(activeContent));
      data.push(item.asJSON());
    }

    var dataString:String = JSON.encode(data);
    return {
      token : ExternalPreviewPluginBase.getPreviewToken(),
      method: 'update',
      previewUrl: ExternalPreviewStudioPluginBase.REST_URL,
      data: dataString
    }
  }
}
}