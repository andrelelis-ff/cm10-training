package com.coremedia.catalog.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.catalog.studio.CatalogStudioPluginBase;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ecommerce.studio.library.ShowInLibraryHelper;
import com.coremedia.ui.util.EventUtil;

public class ShowInCatalogTreeHelper extends ShowInLibraryHelper {
  internal static const TREE_MODEL:RepositoryCatalogTreeModel = new RepositoryCatalogTreeModel();

  public function ShowInCatalogTreeHelper(entities:Array) {
    super(entities, TREE_MODEL);
  }

  override protected function tryShowInCatalogTree(entity:Object):Boolean {
    var content:Content = entity as Content;
    if (content) {
      var sitesService:SitesService = editorContext.getSitesService();
      var siteId:String = sitesService.getSiteIdFor(content);
      if (siteId === undefined) {
        return undefined;
      }
      entity.siteId = siteId;
      if (sitesService.getPreferredSiteId() === siteId) {
        return super.tryShowInCatalogTree(entity);
      }
    }
    return false;
  }

  override protected function switchSite(siteId:String, callback:Function):void {
    var site:Site = editorContext.getSitesService().getSite(siteId);
    CatalogStudioPluginBase.switchSite(site, function ():void {
      EventUtil.invokeLater(callback);
    });
  }

}
}