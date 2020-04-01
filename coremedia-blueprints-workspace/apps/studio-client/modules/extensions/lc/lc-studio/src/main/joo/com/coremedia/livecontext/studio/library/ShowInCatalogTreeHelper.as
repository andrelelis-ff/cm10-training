package com.coremedia.livecontext.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ecommerce.studio.augmentation.augmentationService;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeModel;
import com.coremedia.ecommerce.studio.library.ShowInLibraryHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;

import ext.StringUtil;

public class ShowInCatalogTreeHelper extends ShowInLibraryHelper {

  internal static const TREE_MODEL:CatalogTreeModel = new CatalogTreeModel();

  public function ShowInCatalogTreeHelper(entities:Array) {
    super(entities, TREE_MODEL);
  }

  override protected function tryShowInCatalogTree(entity:Object):Boolean {
    var content:Content = entity as Content;
    var catalogObject:CatalogObject = null;
    if(content) {
      catalogObject = augmentationService.getCatalogObject(content);
      if (catalogObject === undefined) {
        return undefined;
      }
    } else if(entity is CatalogObject) {
      catalogObject = CatalogObject(entity);
    }
    return tryShowCatalogObject(catalogObject, entity);
  }

  private function tryShowCatalogObject(catalogObject:CatalogObject, entity:Object):Boolean {
    if(catalogObject) {
      var sitesService:SitesService = editorContext.getSitesService();
      // catalog objects immediately know their site id
      var catalogObjectSiteId:String = catalogObject.getSiteId();
      if (sitesService.getPreferredSiteId() === catalogObjectSiteId) {
        return super.tryShowInCatalogTree(catalogObject)
      }
      entity.siteId = catalogObjectSiteId;
    }
    return false;
  }

  override protected function openDialog(msg:String, buttons:Object, entity:Object, callback:Function):void {
    if(entity is CatalogObject) {
      delete buttons['no'];
      var siteName:String = editorContext.getSitesService().getSiteName(entity.siteId);
      msg = StringUtil.format(RESOURCE_BUNDLE.Catalog_show_in_catalog_tree_fails_for_CatalogObject, siteName);
    }
    super.openDialog(msg, buttons, entity, callback);
  }
}
}