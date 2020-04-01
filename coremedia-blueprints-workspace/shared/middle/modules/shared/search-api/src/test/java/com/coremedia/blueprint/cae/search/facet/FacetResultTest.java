package com.coremedia.blueprint.cae.search.facet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacetResultTest {

  @Test
  void isFacetWithFilter() {
    FacetResult result = new FacetResult(ImmutableMap.of(
      "noValues", ImmutableList.of(),
      "noFilter", ImmutableList.of(new FacetValue("noFilter", "1", 1)),
      "both", ImmutableList.of(new FacetValue("noFilter", "1", 1), new FacetValue("filter", "2", 2, "2", true)),
      "filter", ImmutableList.of(new FacetValue("filter", "2", 2, "2", true))
    ));

    assertFalse(result.isFacetWithFilter(null));
    assertFalse(result.isFacetWithFilter("unknown"));
    assertFalse(result.isFacetWithFilter("noValues"));
    assertTrue(result.isFacetWithFilter("both"));
    assertTrue(result.isFacetWithFilter("filter"));
  }
}
