package com.coremedia.blueprint.assets.studio.actions {

import com.coremedia.blueprint.assets.studio.AssetConstants;
import com.coremedia.cms.editor.sdk.quickcreate.QuickCreateDialog;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.common.descriptors.StringPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentCreateResult;
import com.coremedia.cap.content.ContentProperties;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.actions.*;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.quickcreate.processing.ProcessingData;
import com.coremedia.ui.data.Blob;
import com.coremedia.ui.data.Calendar;
import com.coremedia.ui.data.RemoteBeanUtil;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.ComponentManager;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.studio.BlueprintDocumentTypes')]
[ResourceBundle('com.coremedia.blueprint.assets.studio.AMStudioPlugin')]
public class CreateContentFromAssetActionBase extends ContentAction {
  private var assetContentType:String;
  private var targetContentType:String;
  private var sourceRenditionProperty:String;
  private var targetRenditionProperty:String;
  private var targetAssetLinkProperty:String;
  private var targetCopyrightProperty:String;
  private var targetValidToProperty:String;
  private var sourceThumbnailProperty:String;
  private var targetThumbnailContentType:String;
  private var targetThumbnailProperty:String;
  private var targetLinkedThumbnailProperty:String;
  private var targetThumbnailAssetLinkProperty:String;

  public function CreateContentFromAssetActionBase(config:CreateContentFromAssetAction = null) {
    assetContentType = config.assetContentType;
    targetContentType = config.targetContentType;
    sourceRenditionProperty = config.sourceRenditionProperty;
    targetRenditionProperty = config.targetRenditionProperty;
    targetAssetLinkProperty = config.targetAssetLinkProperty;
    targetCopyrightProperty = config.targetCopyrightProperty;
    targetValidToProperty = config.targetValidToProperty;
    targetThumbnailProperty = config.targetThumbnailProperty;
    targetThumbnailContentType = config.targetThumbnailContentType;
    sourceThumbnailProperty = config.sourceThumbnailProperty;
    targetLinkedThumbnailProperty = config.targetLinkedThumbnailProperty;
    targetThumbnailAssetLinkProperty = config.targetThumbnailAssetLinkProperty;

    super(CreateContentFromAssetAction(ActionConfigUtil.extendConfiguration(
            ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.blueprint.assets.studio.AMStudioPlugin').content,
            config,
            'create' + targetContentType + 'From' + assetContentType,
            {handler: createContentsFromAssets})));
  }


  override protected function isHiddenFor(contents:Array):Boolean {
    if (!contents || contents.length === 0) {
      return true;
    }

    return contents.some(function(content:Content):Boolean {
      return !content.getState().readable || !content.isDocument() ||
             !content.getType() ||
             !content.getType().isSubtypeOf(assetContentType);
    });
  }

  override protected function isDisabledFor(contents:Array):Boolean {
    if (!contents || contents.length === 0) {
      return true;
    }

    return contents.some(function(content:Content):Boolean{
      var assetProperties:ContentProperties = content.getProperties();
      if (!assetProperties) {
        return true;
      }
      return assetProperties.get(sourceRenditionProperty) === null;
    });
  }

  private function createContentsFromAssets():void {
    var contents:Array = getContents();

    if (isDisabledFor(contents)) {
      return;
    }

    createSingleContent(contents, 0);
  }

  private function createSingleContent(contents:Array, i:int):void {
    if (i >= contents.length){
      return;
    }

    var asset:Content = contents[i];
    var quickCreateConfig:QuickCreateDialog = QuickCreateDialog({});
    quickCreateConfig.bindTo = getValueExpression();
    quickCreateConfig.openInTab = false;
    quickCreateConfig.contentType = targetContentType;
    quickCreateConfig.onSuccess = function (createdContent:Content, data:ProcessingData, callback:Function):void {
      var metadataStruct:Struct = asset.getProperties().get(AssetConstants.PROPERTY_ASSET_METADATA);
      RemoteBeanUtil.loadAll(function ():void {
        createdContent.getProperties().set(targetAssetLinkProperty, [asset]);
        createdContent.getProperties().set(targetRenditionProperty, asset.getProperties().get(sourceRenditionProperty));
        if (targetCopyrightProperty) {
          var copyright:String = metadataStruct.get(AssetConstants.PROPERTY_ASSET_METADATA_COPYRIGHT);
          if (copyright) {
            var descriptor:StringPropertyDescriptor = createdContent.getType().getDescriptor(targetCopyrightProperty) as StringPropertyDescriptor;
            if (descriptor) {
              createdContent.getProperties().set(targetCopyrightProperty, copyright.substr(0, descriptor.length));
            }
          }
        }
        if (targetValidToProperty) {
          var expirationDate:Calendar = metadataStruct.get(AssetConstants.PROPERTY_ASSET_METADATA_EXPIRATIONDATE);
          if (expirationDate) {
            var validToDescriptor:CapPropertyDescriptor = createdContent.getType().getDescriptor(targetValidToProperty);
            if (validToDescriptor) {
              createdContent.getProperties().set(targetValidToProperty, expirationDate);
            }
          }
        }
        if (targetThumbnailProperty && sourceThumbnailProperty) {
          var thumbnail:Blob = asset.getProperties().get(sourceThumbnailProperty);
          if (thumbnail) {
            var contentType:ContentType = SESSION.getConnection().getContentRepository().getContentType(targetThumbnailContentType);
            var thumbnailProperties:Object = {};
            thumbnailProperties[targetThumbnailProperty] = thumbnail;
            thumbnailProperties[targetThumbnailAssetLinkProperty] = [asset];
            contentType.createWithProperties(
                    createdContent.getParent(),
                    createdContent.getName() + ' Thumbnail',
                    thumbnailProperties,
                    function (result:ContentCreateResult):void {
                      var documentsToOpen:Array = [];
                      if (result.createdContent) {
                        createdContent.getProperties().set(targetLinkedThumbnailProperty, [result.createdContent]);
                        documentsToOpen.push(result.createdContent);
                      }
                      documentsToOpen.push(createdContent);
                      editorContext.getContentTabManager().openDocuments(documentsToOpen);
                      callback();
                      createSingleContent(contents, i+1);
                    });
          } else {
            editorContext.getContentTabManager().openDocument(createdContent);
            callback();
            createSingleContent(contents, i+1);
          }
        } else {
          editorContext.getContentTabManager().openDocument(createdContent);
          callback();
          createSingleContent(contents, i+1);
        }

      }, createdContent, metadataStruct);
    };
    quickCreateConfig.defaultNameExpression = ValueExpressionFactory.createFromFunction(function ():String {
      return asset.getName() + ' ' +
              (ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.BlueprintDocumentTypes', targetContentType + '_text') || targetContentType);
    });

    var dialog:QuickCreateDialog = QuickCreateDialog(ComponentManager.create(quickCreateConfig));
    dialog.show();
  }
}
}
