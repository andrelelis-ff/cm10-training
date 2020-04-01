package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/contract/{siteId:[^/]+}/{workspaceId:[^/]+}/{externalId:[^/]+}")]
public class ContractImpl extends CatalogObjectImpl implements Contract {
  public function ContractImpl(uri:String) {
    super(uri);
  }
}
}