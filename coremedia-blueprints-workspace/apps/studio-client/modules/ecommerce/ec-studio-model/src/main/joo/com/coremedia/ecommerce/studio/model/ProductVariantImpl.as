package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/sku/{siteId:[^/]+}/{catalogAlias:[^/]+}/{workspaceId:[^/]+}/{externalId:.+}")]
public class ProductVariantImpl extends ProductImpl implements ProductVariant {
  public function ProductVariantImpl(uri:String) {
    super(uri);
  }

  public function getParent():Product {
    return get(CatalogObjectPropertyNames.PARENT);
  }

  public function getDefiningAttributes():Array {
    return get(ProductPropertyNames.DEFINING_ATTRIBUTES);
  }

}
}