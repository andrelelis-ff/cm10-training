package com.coremedia.livecontext.ecommerce.ibm.catalog;

import java.util.Map;

class CategoryDelegateLoader extends AbstractDelegateLoader {

  private final CategoryImpl category;

  public CategoryDelegateLoader(CategoryImpl category) {
    this.category = category;
  }

  @Override
  Map<String, Object> getDelegateFromCache() {
    return category.getDelegateFromCache();
  }
}
