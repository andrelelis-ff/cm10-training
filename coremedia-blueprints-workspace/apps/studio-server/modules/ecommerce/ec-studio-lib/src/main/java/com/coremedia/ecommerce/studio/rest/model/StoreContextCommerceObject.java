package com.coremedia.ecommerce.studio.rest.model;

import com.coremedia.livecontext.ecommerce.common.CommerceObject;
import com.coremedia.livecontext.ecommerce.common.StoreContext;

public abstract class StoreContextCommerceObject implements CommerceObject {
  private StoreContext context;
  private String idPrefix;

  protected StoreContextCommerceObject(StoreContext context, String idPrefix) {
    this.context = context;
    this.idPrefix = idPrefix;
  }

  public String getId() {
    return idPrefix + "-" + context.getStoreName();
  }

  public StoreContext getContext() {
    return context;
  }
}
