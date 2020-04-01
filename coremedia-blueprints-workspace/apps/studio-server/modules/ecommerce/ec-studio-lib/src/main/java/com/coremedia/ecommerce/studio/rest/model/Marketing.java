package com.coremedia.ecommerce.studio.rest.model;

import com.coremedia.livecontext.ecommerce.common.StoreContext;

/**
 * We are using a faked commerce bean here since the "Marketing" level
 * in the Studio library is only used there and is not supported by the commerce API.
 * Therefore we implement the the interface "CommerceObject" here and use the Store itself
 * as a delegate since the "Marketing" only provides methods that are available on the store.
 */
public class Marketing extends StoreContextCommerceObject {
  public Marketing(StoreContext context) {
    super(context, "marketing");
  }
}
