package com.coremedia.blueprint.studio.externalpreview {
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preview.PreviewURI;

import net.jangaroo.net.URIUtils;

public class PreviewDataItem {
  private var content:Content;
  private var active:Boolean = false;
  private var preview:Boolean = true;

  public function PreviewDataItem(content:Content) {
    this.content = content;
    this.preview = !isExcludedDocumentTypeWithoutPreview(content);
  }


  /**
   * Checks if the given content is in the list of excluded previewable documents.
   * @param content The content to check.
   * @return True, if the given document does not support a preview.
   */
  private static function isExcludedDocumentTypeWithoutPreview(content:Content):Boolean {
    var exclusions:Array = editorContext.getDocumentTypesWithoutPreview();
    return exclusions.length > 0 && exclusions.indexOf(content.getType().getName()) >= 0;
  }

  public function setActive(b:Boolean):void {
    active = b;
  }

  public function asJSON():Object {
    var name:String = content.getName();
    var previewUrl:String = content.getPreviewUrl();
    previewUrl = appendPreviewUrlTransformerParameters(previewUrl);

    if (previewUrl.indexOf("//") !== 0 && !URIUtils.parse(previewUrl).isAbsolute) {
      previewUrl = ExternalPreviewStudioPluginBase.CONTENT_PREVIEW_URL_PREFIX + content.getPreviewUrl();
    }
    if(previewUrl.indexOf("//") === 0) {
      previewUrl = window.location.protocol + previewUrl;
    }
    
    return {
      active:active,
      modificationDate:content.get('modificationDate'),
      name:name,
      preview:this.preview,
      id: IdHelper.parseContentId(content),
      previewUrl: previewUrl,
      lifecycleStatus: content.getLifecycleStatus()
    }
  }

  /**
   * Uses a dummy PreviewURI to collect all parameters that are appended through
   * PreviewURLTransformer implementations.
   * @param previewUrl the default preview url which already contains the content id
   */
  private function appendPreviewUrlTransformerParameters(previewUrl:String):String {
    var dummyUri:PreviewURI = new PreviewURI(previewUrl, content, [], function ():void {});
    var transformers:Array = (editorContext as EditorContextImpl).getPreviewUrlTransformers();
    for each(var transformer:Function in transformers) {
      transformer.call(null, dummyUri, function ():void {
      });
    }
    //the id is already there and we don't want special view
    var params:Object = dummyUri.getParameters();
    params.id = undefined;
    params.view = undefined;

    for (var property:String in params) {
      if (params[property]) {
        previewUrl += "&" + property + "=" + params[property];
      }
    }
    return previewUrl;
  }

  public function getContentId():int {
    return IdHelper.parseContentId(content);
  }
}
}
