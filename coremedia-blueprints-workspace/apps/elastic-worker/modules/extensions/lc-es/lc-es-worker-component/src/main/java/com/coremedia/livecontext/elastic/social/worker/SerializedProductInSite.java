package com.coremedia.livecontext.elastic.social.worker;

class SerializedProductInSite {

  private final String productId;
  private final String siteId;

  public SerializedProductInSite(String productId, String siteId) {
    this.productId = productId;
    this.siteId = siteId;
  }

  String getProductId() {
    return productId;
  }

  String getSiteId() {
    return siteId;
  }
}
