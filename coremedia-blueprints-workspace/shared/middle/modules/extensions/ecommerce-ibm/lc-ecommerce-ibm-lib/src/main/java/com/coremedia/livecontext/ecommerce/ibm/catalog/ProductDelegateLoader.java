package com.coremedia.livecontext.ecommerce.ibm.catalog;

import java.util.Map;

class ProductDelegateLoader extends AbstractDelegateLoader {

  private final ProductBase product;

  ProductDelegateLoader(ProductBase product) {
    this.product = product;
  }

  @Override
  Map<String, Object> getDelegateFromCache() {
    return product.getDelegateFromCache();
  }
}
