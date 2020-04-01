package com.coremedia.livecontext.ecommerce.ibm.common;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractIbmService {

  private IbmCommerceIdProvider commerceIdProvider;

  @Autowired
  public void setCommerceIdProvider(IbmCommerceIdProvider commerceIdProvider) {
    this.commerceIdProvider = commerceIdProvider;
  }

  protected IbmCommerceIdProvider getCommerceIdProvider() {
    return commerceIdProvider;
  }
}
