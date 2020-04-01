package com.coremedia.ecommerce.studio.rest.model;

import com.coremedia.livecontext.ecommerce.common.StoreContext;

/**
 * We are using a faked commerce bean here to support the invalidation of the list of available contracts
 * Therefore we implement the the interface "CommerceObject" here and use the Store itself
 * as a delegate since the "Contracts" only provides methods that are available on the store.
 */
public class Contracts extends StoreContextCommerceObject {
  public Contracts(StoreContext context) {
    super(context, "contracts");
  }
}
