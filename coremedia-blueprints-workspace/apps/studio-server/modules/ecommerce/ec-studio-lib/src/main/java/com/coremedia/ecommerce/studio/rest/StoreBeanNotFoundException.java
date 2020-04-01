package com.coremedia.ecommerce.studio.rest;

import org.springframework.http.HttpStatus;

import static com.coremedia.ecommerce.studio.rest.CatalogRestErrorCodes.COULD_NOT_FIND_STORE_BEAN;

public class StoreBeanNotFoundException extends CatalogRestException {

  private static final long serialVersionUID = 1680285844326419304L;

  public StoreBeanNotFoundException(HttpStatus status, String message) {
    super(status, COULD_NOT_FIND_STORE_BEAN, message);
  }
}
