package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/workspaces/{siteId:[^/]+}")]
public class WorkspacesImpl extends CatalogObjectImpl implements Workspaces {
  public function WorkspacesImpl(uri:String) {
    super(uri);
  }

  public function getWorkspaces():Array {
    return get(CatalogObjectPropertyNames.WORKSPACES);
  }
}
}