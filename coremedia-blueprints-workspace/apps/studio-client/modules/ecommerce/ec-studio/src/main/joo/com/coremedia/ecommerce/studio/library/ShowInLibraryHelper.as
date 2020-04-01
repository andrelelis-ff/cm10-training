package com.coremedia.ecommerce.studio.library {
import com.coremedia.cap.undoc.content.Content;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundChildTreeModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preferences.PreferenceWindow;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtilInternal;
import com.coremedia.ecommerce.studio.catalogHelper;
import com.coremedia.ecommerce.studio.components.preferences.CatalogPreferencesBase;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.EventUtil;

import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
[ResourceBundle('com.coremedia.cms.editor.Editor')]
public class ShowInLibraryHelper {

  protected static const RESOURCE_BUNDLE:Object = ResourceManager.getInstance().getResourceBundle(null, 'com.coremedia.ecommerce.studio.ECommerceStudioPlugin').content;
  private static const HIDE_EVENT_NAME:String = 'close';

  private var entities:Array;
  private var catalogTreeModel:CompoundChildTreeModel;

  public function ShowInLibraryHelper(entities:Array, catalogTreeModel:CompoundChildTreeModel) {
    this.entities = entities;
    this.catalogTreeModel = catalogTreeModel;
  }

  public function showItems(treeModelId:String):void{
    if(CollectionViewConstants.TREE_MODEL_ID === treeModelId) {
      // clicked on path (see DocumentPath.exml)
      showInContentRepositoryTree();
    } else {
      // opened via tab menu item
      showInCatalogTree();
    }
  }

  public function showInContentRepositoryTree():void {
    // bind 'this' so that function value expression is happy
    var self:ShowInLibraryHelper = this;
    // try to open in content repository tree first
    entities.forEach(function (entity:Object):void {
      if(!tryShowInContentRepositoryTree(entity)) {
        var ve:ValueExpression = ValueExpressionFactory.createFromFunction(function (entity:Object):Boolean {
          return self.tryShowInCatalogTree(entity);
        }, entity);
        ve.loadValue(function():void {
          var canShowInCatalogTree:Boolean = ve.getValue();
          if(!canShowInCatalogTree) {
            adjustSettings(entity, showInContentRepositoryTree, RESOURCE_BUNDLE.Catalog_show_in_content_tree_fails_for_Content);
          }
        });
      }
    });
  }

  public function showInCatalogTree():void {
    // bind 'this' so that function value expression is happy
    var self:ShowInLibraryHelper = this;
    entities.forEach(function (entity:Object):void {
      // try to open in catalog tree first
      var ve:ValueExpression = ValueExpressionFactory.createFromFunction(function (entity:Object):Boolean {
        return self.tryShowInCatalogTree(entity);
      }, entity);
      ve.loadValue(function():void {
        var canShowInCatalogTree:Boolean = ve.getValue();
        if(!canShowInCatalogTree && !tryShowInContentRepositoryTree(entity)) {
          adjustSettings(entity, showInCatalogTree, RESOURCE_BUNDLE.Catalog_show_in_catalog_tree_fails_for_Content);
        }
      });
    });
  }

  protected function tryShowInContentRepositoryTree(entity:Object):Boolean {
    if(entity is Content && getShowAsContentVE().getValue()) {
      showInRepositoryMode(entity, CollectionViewConstants.TREE_MODEL_ID);
      return true;
    }
    return false;
  }

  internal static function getShowAsContentVE():ValueExpression {
    return ValueExpressionFactory.create(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY, editorContext.getPreferences());
  }

  protected function tryShowInCatalogTree(entity:Object):Boolean {
    var idPathFromModel:Array = catalogTreeModel.getIdPathFromModel(entity);
    if (idPathFromModel === undefined) {
      return undefined;
    }
    if(null !== idPathFromModel) {
      showInRepositoryMode(entity, catalogTreeModel.getTreeId());
      return true;
    }
    return false;
  }

  private static function showInRepositoryMode(entity:*, treeModelId:String):void {
    // ignoring type of entity (show in repository doesn't really care if it's of type Content)
    editorContext.getCollectionViewManager().showInRepository(entity, null, treeModelId);
  }

  protected function adjustSettings(entity:Object, callback:Function, msg:String):void {
    var buttons:Object = {
      no: RESOURCE_BUNDLE.Catalog_show_preferences_button_text,
      cancel: ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'dialog_defaultCancelButton_text'),
      yes: RESOURCE_BUNDLE.Catalog_show_switch_site_button_text
    };
    openDialog(msg, buttons, entity, callback);
  }

  protected function openDialog(msg:String, buttons:Object, entity:Object, callback:Function):void {
    MessageBoxUtilInternal.show(RESOURCE_BUNDLE.Catalog_show_in_tree_fails_title, msg, null, buttons, getButtonCallback(entity.siteId, callback));
  }

  protected function switchSite(siteId:String, callback:Function):void {
    //switch site
    editorContext.getSitesService().getPreferredSiteIdExpression().setValue(siteId);
    // make sure that the new catalog is available
    catalogHelper.openCatalog();

    EventUtil.invokeLater(callback);
  }

  private function getButtonCallback(siteId:String, callback:Function):Function {
    return function (btn:String):void {
      if (btn === 'cancel') {
        //just cancel
      }
      else if(btn === 'yes') {
        switchSite(siteId, callback);
      }
      else {
        //show preferences
        var prefWindow:PreferenceWindow = Ext.create(PreferenceWindow, {selectedTabItemId: 'contentCatalogPreferences'});
        prefWindow.show();
        //open the content in library if the user enable the show as content contentCatalogPreferences
        prefWindow.on(HIDE_EVENT_NAME, callback);
      }
    }
  }

}
}
