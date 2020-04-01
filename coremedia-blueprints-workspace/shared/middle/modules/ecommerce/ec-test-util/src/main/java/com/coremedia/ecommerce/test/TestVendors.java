package com.coremedia.ecommerce.test;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.Vendor;

import edu.umd.cs.findbugs.annotations.NonNull;

public class TestVendors {

  @NonNull
  public static BaseCommerceIdProvider getIdProvider(@NonNull String vendor) {
    return new BaseCommerceIdProvider(Vendor.of(vendor));
  }

}
