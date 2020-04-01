package com.coremedia.ecommerce.studio.rest;

/**
 * Error codes for the catalog REST service.
 */
public interface CatalogRestErrorCodes {

  /**
   * Error code for: The process name was not specified when trying to start a workflow process.
   */
  String COULD_NOT_FIND_CATALOG_BEAN = "LC-01000";

  String CATALOG_UNAVAILABLE = "LC-01001";

  String CATALOG_INTERNAL_ERROR = "LC-01002";

  String UNAUTHORIZED = "LC-01003";

  String COULD_NOT_FIND_CATALOG = "LC-01004";

  String ROOT_CATEGORY_NOT_AUGMENTED = "LC-01005";

  String COULD_NOT_FIND_STORE_BEAN = "LC-01006";
}
