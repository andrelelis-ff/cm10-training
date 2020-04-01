package com.coremedia.ecommerce.studio.rest;

import org.springframework.http.HttpStatus;

/**
 * Exception to indicate to the REST client that a catalog bean could not be found.
 */
public class CatalogBeanNotFoundRestException extends CatalogRestException {

  private static final long serialVersionUID = 1767180019775402052L;

  public CatalogBeanNotFoundRestException(String message) {
    super(HttpStatus.NOT_FOUND, CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN, message);
  }

  public CatalogBeanNotFoundRestException(HttpStatus status, String message) {
    super(status, CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN, message);
  }
}
