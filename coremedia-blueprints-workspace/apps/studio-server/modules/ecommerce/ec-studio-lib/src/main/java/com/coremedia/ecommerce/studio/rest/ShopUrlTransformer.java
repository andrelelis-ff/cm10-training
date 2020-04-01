package com.coremedia.ecommerce.studio.rest;

import com.google.common.base.Function;

import edu.umd.cs.findbugs.annotations.Nullable;

class ShopUrlTransformer implements Function<PbeShopUrlTargetResolver, Object> {
  private final String shopUrlStr;
  private final String siteId;

  ShopUrlTransformer(String shopUrlStr, String siteId) {
    this.shopUrlStr = shopUrlStr;
    this.siteId = siteId;
  }

  @Nullable
  @Override
  public Object apply(@Nullable PbeShopUrlTargetResolver input) {
    return input == null ? null : input.resolveUrl(shopUrlStr, siteId);
  }
}
