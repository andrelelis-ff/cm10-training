package com.coremedia.livecontext.studio.action {
import com.coremedia.cms.editor.sdk.quickcreate.QuickCreateDialog;
import com.coremedia.cms.editor.sdk.actions.ActionConfigUtil;
import com.coremedia.cms.editor.sdk.quickcreate.processing.ProcessingData;
import com.coremedia.ecommerce.studio.augmentation.augmentationService;
import com.coremedia.ecommerce.studio.model.CatalogObject;

import ext.ComponentManager;
import ext.Ext;
import ext.window.Window;

import mx.resources.ResourceManager;

/**
 * This action is intended to be used from within EXML, only.
 *
 */
[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class CreateCatalogObjectDocumentActionBase extends LiveContextCatalogObjectAction {

  public static const EXTERNAL_ID_PROPERTY:String = 'externalId';

  private var contentType:String;
  private var catalogObjectType:Class;
  private var inheritEditors:Boolean;

  /**
   * @param config the configuration object
   */
  public function CreateCatalogObjectDocumentActionBase(config:CreateCatalogObjectDocumentAction = null) {
    super(CreateCatalogObjectDocumentAction(ActionConfigUtil.extendConfiguration(ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.livecontext.studio.LivecontextStudioPlugin').content, config, config.actionName, {handler: myHandler})));
    contentType = config.contentType;
    catalogObjectType = config.catalogObjectType;
    inheritEditors = config.inheritEditors;
  }

  override protected function isDisabledFor(catalogObjects:Array):Boolean {
    //the action should be enabled only if there is only one catalog object and it is a correct configured type
    if (catalogObjects.length !== 1) {
      return true;
    }
    var catalogObject:CatalogObject = catalogObjects[0];
    if (!(isCorrectType(catalogObject))) {
      return true;
    }
    //check if the catalog object has already an associated content
    if (augmentationService.getContent(catalogObject)) {
      return true;
    }

    return super.isDisabledFor(catalogObjects);
  }

  override protected function isHiddenFor(catalogObjects:Array):Boolean {
    return super.isHiddenFor(catalogObjects) || isDisabledFor(catalogObjects);
  }

  protected function isCorrectType(catalogObject:CatalogObject):Boolean {
    return Ext.getClassName(catalogObject) === Ext.getClassName(catalogObjectType);
  }

  protected function myHandler():void {
    var catalogObject:CatalogObject = getCatalogObjects()[0];
    if (isCorrectType(catalogObject)) {
      //create the dialog
      var dialogConfig:QuickCreateDialog = QuickCreateDialog({});
      dialogConfig.contentType = getContentType();
      dialogConfig.model = new ProcessingData();
      dialogConfig.model.set(EXTERNAL_ID_PROPERTY, catalogObject.getId());
      dialogConfig.model.set(ProcessingData.NAME_PROPERTY, catalogObject.getName());
      dialogConfig.inheritEditors = inheritEditors;

      var dialog:Window = ComponentManager.create(dialogConfig, 'window') as Window;
      dialog.show();
    }
  }

  protected function getContentType():String {
    return contentType;
  }
}
}
