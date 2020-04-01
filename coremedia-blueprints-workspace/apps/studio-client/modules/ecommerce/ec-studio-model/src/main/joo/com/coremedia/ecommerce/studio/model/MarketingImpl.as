package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/marketing/{siteId:[^/]+}/{workspaceId:[^/]+}")]
public class MarketingImpl extends CatalogObjectImpl implements Marketing {
  public function MarketingImpl(uri:String) {
    super(uri);
  }


  public function getChildrenByName():Object {
    return get(CatalogObjectPropertyNames.CHILDREN_BY_NAME);
  }

  public function getMarketingSpots():Array {
    return get(CatalogObjectPropertyNames.MARKETING_SPOTS);
  }
}
}