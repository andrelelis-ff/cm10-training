package com.coremedia.livecontext.ecommerce.ibm.catalog;

enum SearchType {
  SEARCH_TYPE_PRODUCTS("2"),
  SEARCH_TYPE_PRODUCT_VARIANTS("102");
  private final String value;

  SearchType(final String searchType) {
    this.value = searchType;
  }

  public String getValue() {
    return value;
  }
}
