package com.coremedia.ecommerce.studio.rest.model;

import com.coremedia.livecontext.ecommerce.common.StoreContext;

/**
 * We are using a faked commerce bean here to support the invalidation of the list of available segments
 * Therefore we implement the the interface "CommerceObject" here and use the Store itself
 * as a delegate since the "Workspaces" only provides methods that are available on the store.
 */
public class Workspaces extends StoreContextCommerceObject {
  public Workspaces(StoreContext context) {
    super(context, "workspaces");
  }
}
