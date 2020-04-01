package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/contracts/{siteId:[^/]+}/{workspaceId:[^/]+}")]
public class ContractsImpl extends CatalogObjectImpl implements Contracts {
  public function ContractsImpl(uri:String) {
    super(uri);
  }

  public function getContracts():Array {
    return get(CatalogObjectPropertyNames.CONTRACTS);
  }
}
}