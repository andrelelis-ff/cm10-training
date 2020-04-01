package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/catalog/{siteId:[^/]+}/{externalId:[^/]+}")]
public class CatalogImpl extends CatalogObjectImpl implements Catalog {
  public function CatalogImpl(uri:String) {
    super(uri);
  }

  public function getRootCategory():Category {
    return get(CatalogObjectPropertyNames.ROOT_CATEGORY);
  }

  public function isDefault():Boolean {
    return get(CatalogObjectPropertyNames.DEFAULT);
  }
}
}