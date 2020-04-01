package com.coremedia.blueprint.lc.test;

import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface TestConfig {

  CommerceConnection getCommerceConnection();

  StoreContext getStoreContext(@NonNull CommerceConnection connection);

  StoreContext getGermanStoreContext(@NonNull CommerceConnection connection);

  String getCatalogName();
}
