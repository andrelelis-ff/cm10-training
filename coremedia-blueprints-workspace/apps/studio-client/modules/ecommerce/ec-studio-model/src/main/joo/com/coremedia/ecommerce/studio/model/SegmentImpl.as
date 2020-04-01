package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/segment/{siteId:[^/]+}/{workspaceId:[^/]+}/{externalId:[^/]+}")]
public class SegmentImpl extends CatalogObjectImpl implements Segment {
  public function SegmentImpl(uri:String) {
    super(uri);
  }
}
}