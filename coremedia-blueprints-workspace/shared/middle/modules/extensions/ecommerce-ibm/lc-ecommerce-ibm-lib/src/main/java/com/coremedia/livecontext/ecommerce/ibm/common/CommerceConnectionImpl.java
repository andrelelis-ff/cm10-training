package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;

import java.util.Optional;

/**
 * An IBM specific connection class. Manages the IBM vendor specific properties.
 */
public class CommerceConnectionImpl extends BaseCommerceConnection {

  public CommerceConnectionImpl() {
    setVendor(IbmCommerceIdProvider.IBM);
  }

  @Override
  public String getVendorVersion() {
    return Optional.ofNullable(getStoreContext())
            .map(StoreContextHelper::getWcsVersion)
            .map(WcsVersion::toVersionString)
            .orElseGet(super::getVendorVersion);
  }
}
