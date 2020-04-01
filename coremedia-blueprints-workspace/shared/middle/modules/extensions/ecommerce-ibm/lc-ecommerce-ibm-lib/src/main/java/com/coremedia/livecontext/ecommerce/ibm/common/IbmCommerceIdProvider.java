package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceIdProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdBuilder;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.Vendor;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public class IbmCommerceIdProvider extends BaseCommerceIdProvider {

  static final Vendor IBM = Vendor.of("ibm");

  public IbmCommerceIdProvider() {
    super(IBM);
  }

  public static CommerceIdBuilder commerceId(CommerceBeanType beanType) {
    return BaseCommerceIdProvider.commerceId(IBM, beanType);
  }
}
