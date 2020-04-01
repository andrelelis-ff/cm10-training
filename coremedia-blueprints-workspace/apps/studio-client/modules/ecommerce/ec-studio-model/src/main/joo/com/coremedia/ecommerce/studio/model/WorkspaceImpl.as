package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/workspace/{siteId:[^/]+}/{externalId:[^/]+}")]
public class WorkspaceImpl extends CatalogObjectImpl implements Workspace {
  public function WorkspaceImpl(uri:String) {
    super(uri);
  }
}
}