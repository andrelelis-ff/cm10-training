package com.coremedia.ecommerce.studio.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Base class for all e-commerce JSON representations
 */
public abstract class AbstractCatalogRepresentation {

  private String id;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
