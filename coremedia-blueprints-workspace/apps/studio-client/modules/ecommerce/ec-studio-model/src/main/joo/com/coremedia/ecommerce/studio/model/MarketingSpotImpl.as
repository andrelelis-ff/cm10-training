package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/marketingspot/{siteId:[^/]+}/{workspaceId:[^/]+}/{externalId:[^/]+}")]
public class MarketingSpotImpl extends CatalogObjectImpl implements MarketingSpot {
  public function MarketingSpotImpl(uri:String) {
    super(uri);
  }

  public function getMarketing():Marketing {
    return get(CatalogObjectPropertyNames.MARKETING);
  }
}
}