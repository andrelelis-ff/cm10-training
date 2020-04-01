package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/facets/{siteId:[^/]+}/{catalogAlias:[^/]+}/{workspaceId:[^/]+}/{categoryId:.+}")]
public class FacetsImpl extends CatalogObjectImpl implements Facets {
  public function FacetsImpl(uri:String) {
    super(uri);
  }

  public function getFacets():* {
    return get(CatalogObjectPropertyNames.FACETS);
  }
}
}