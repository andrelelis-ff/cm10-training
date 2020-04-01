package com.coremedia.ecommerce.studio.rest;

import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * TODO: Description
 */
public class CatalogSearchResultRepresentation {
  private final List<? extends CommerceBean> hits;
  private final long total;


  @JsonCreator
  public CatalogSearchResultRepresentation(@JsonProperty("hits") List<? extends CommerceBean> hits, @JsonProperty("total")long total) {
    this.hits = hits;
    this.total = total;
  }

  @JsonSerialize
  public List<? extends CommerceBean> getHits() {
    return hits;
  }

  @JsonSerialize
  public long getTotal() {
    return total;
  }
}
