package com.coremedia.livecontext.p13n.studio {
import com.coremedia.blueprint.personalization.editorplugin.plugin.AddSiteSpecificPathPlugin;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ecommerce.studio.model.Contracts;
import com.coremedia.ecommerce.studio.model.Segments;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.ValueExpression;

public class LivecontextP13NStudioPluginBase extends StudioPlugin {

  internal static const USER_SEGMENTS:String = 'usersegments';
  internal static const USER_CONTRACTS:String = 'usercontracts';

  public function LivecontextP13NStudioPluginBase(config:LivecontextP13NStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);
    //add site formatter for the catalog object entity
    AddSiteSpecificPathPlugin.addSitePathFormatter(formatSitePathFromCatalogObject);
  }

  private function formatSitePathFromCatalogObject(path:String, entityExpression:ValueExpression, callback:Function):void {
    entityExpression.loadValue(function(entity:Object):void {
      if (entity is CatalogObject) {
        entityExpression.extendBy(CatalogObjectPropertyNames.STORE).loadValue(function(store:Store):void {
          //value should be the store
          var sitesService:SitesService = editorContext.getSitesService();
          var site:Site = sitesService.getSite(store.getSiteId());
          var selectedSitePath:String = site ? site.getSiteRootFolder().getPath() + '/' + path : null;
          callback.call(null, selectedSitePath);
        });
      }
    });
  }

  internal static function getSegments(store:Store):Array {
    var segments:Segments = store.getSegments();
    return segments && segments.getSegments();
  }

  internal static function getContracts(store:Store):Array {
    var contracts:Contracts = store.getContracts();
    return contracts && contracts.getContracts();
  }

}
}
