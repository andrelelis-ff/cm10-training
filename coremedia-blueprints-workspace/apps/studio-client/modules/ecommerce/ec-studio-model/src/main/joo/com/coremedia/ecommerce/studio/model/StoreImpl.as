package com.coremedia.ecommerce.studio.model {
import com.coremedia.ecommerce.studio.CatalogModel;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.coremedia.ui.util.AsyncComputation;

[RestResource(uriTemplate="livecontext/store/{siteId:[^/]+}/{workspaceId:[^/]+}")]
public class StoreImpl extends CatalogObjectImpl implements Store {
  private var siteId:String;
  private var workspaceId:String;
  private const resolvedUrls:Object = {};
  private const URL_INVALIDATION_INTERVAL:int = 5000;

  public function StoreImpl(uri:String, vars:Object) {
    siteId = vars['siteId'];
    workspaceId = vars['workspaceId'];
    super(uri);
  }

  public function getChildrenByName():Object {
    return get(CatalogObjectPropertyNames.CHILDREN_BY_NAME);
  }

  public function getStoreId():String {
    return get(CatalogObjectPropertyNames.STORE_ID);
  }

  override public function getSiteId():String {
    return siteId;
  }

  public function getTopLevel():Array {
    return get("topLevel");
  }

  public function getMarketing():Marketing {
    return get("marketing");
  }

  public function isMarketingEnabled():Boolean {
    return get("marketingEnabled");
  }

  public function getSegments():Segments {
    return get(CatalogObjectPropertyNames.SEGMENTS);
  }

  public function getContracts():Contracts {
    return get(CatalogObjectPropertyNames.CONTRACTS);
  }

  public function getWorkspaces():Workspaces {
    return get(CatalogObjectPropertyNames.WORKSPACES);
  }

  public function getCatalogs():Array{
    return get(CatalogObjectPropertyNames.CATALOGS);
  }

  public function getDefaultCatalog():Catalog {
    return get(CatalogObjectPropertyNames.DEFAULT_CATALOG)
  }

  public function isMultiCatalog():Boolean {
    return get(CatalogObjectPropertyNames.MULTI_CATALOG);
  }

  public function getCurrentWorkspace():Workspace {
    if (!workspaceId || workspaceId === CatalogModel.NO_WS) {
      return undefined;
    }
    if (!getWorkspaces()) {
      return undefined;
    }
    if (!getWorkspaces().getWorkspaces()) {
      return undefined;
    }

    var workspaces:Array = getWorkspaces().getWorkspaces();

    var filtered:Array = workspaces.filter(function (workspace:Workspace):Boolean {
      return workspaceId === workspace.getExternalTechId();
    });

    return filtered.length > 0 ? filtered[0] : null;
  }

  public function getRootCategory():Category {
    return get(CatalogObjectPropertyNames.ROOT_CATEGORY);
  }

  public function getVendorUrl():String {
    return get(CatalogObjectPropertyNames.VENDOR_URL);
  }

  public function getVendorVersion():String {
    return get(CatalogObjectPropertyNames.VENDOR_VERSION);
  }

  public function getTimeZoneId():String {
    return get("timeZoneId");
  }

  public function getVendorName():String {
    return get(CatalogObjectPropertyNames.VENDOR_NAME);
  }

  override public function getStore():Store {
    return this;
  }

  public function resolveShopUrlForPbe(shopUrl:String):RemoteBean {
    var resolvedUrl:Object = resolvedUrls[shopUrl];
    if (undefined === resolvedUrl) {
      var asyncUrlComputation = new AsyncComputation(requestShopUrl);
      resolvedUrl = {at: new Date(), async: asyncUrlComputation};
    }

    if (undefined === resolvedUrls[shopUrl] || resolvedUrl.at < new Date().getTime() - URL_INVALIDATION_INTERVAL) {
      resolvedUrls[shopUrl] = resolvedUrl;
      resolvedUrl.at = new Date();
      resolvedUrl.async.trigger(0, false, shopUrl);
    }
    return resolvedUrl.async.getValue();
  }

  private function requestShopUrl(callback:Function, shopUrl:String):void {
    var urlResolveUri:String = this.getUriPath() + "/urlService";
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(urlResolveUri, "POST", true, true);
    remoteServiceMethod.request({shopUrl: shopUrl},
            function (response:RemoteServiceMethodResponse):void {
              callback(response.getResponseJSON());
            });
  }
}
}
