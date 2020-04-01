package com.coremedia.livecontext.ecommerce.ibm.catalog;

/**
 * This enumeration is used to transform human readable string from studio
 * to IBM specific four default sort property fields.
 */
enum OrderByType {

  ORDER_BY_TYPE_BRAND_ASC("1"),
  ORDER_BY_TYPE_CATEGORY_ASC("2"),
  ORDER_BY_TYPE_PRICE_ASC("3"),
  ORDER_BY_TYPE_PRICE_DSC("4");

  private final String value;

  OrderByType(final String orderByType) {
    this.value = orderByType;
  }

  public String getValue() {
    return value;
  }
}
