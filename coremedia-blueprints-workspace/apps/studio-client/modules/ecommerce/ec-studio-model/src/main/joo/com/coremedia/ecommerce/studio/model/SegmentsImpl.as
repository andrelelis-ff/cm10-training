package com.coremedia.ecommerce.studio.model {
[RestResource(uriTemplate="livecontext/segments/{siteId:[^/]+}/{workspaceId:[^/]+}")]
public class SegmentsImpl extends CatalogObjectImpl implements Segments {
  public function SegmentsImpl(uri:String) {
    super(uri);
  }

  public function getSegments():Array {
    return get(CatalogObjectPropertyNames.SEGMENTS);
  }
}
}