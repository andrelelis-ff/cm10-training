package com.coremedia.ecommerce.studio.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Facets representation for JSON.
 */
public class FacetsRepresentation extends CommerceBeanRepresentation {

  private Map<String, List<Facet>> facets = Collections.emptyMap();

  public Map<String, List<Facet>> getFacets() {
    return facets;
  }

  public void setFacets(Map<String, List<Facet>> facets) {
    this.facets = facets;
  }
}
