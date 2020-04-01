package com.coremedia.blueprint.cae.search.facet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacetFilterBuilderTest {

  @Test
  void buildEmpty() {
    assertEquals("", new FacetFilterBuilder(ImmutableMap.of()).build());
  }

  @Test
  void buildNoFilter() {
    FacetValue value1 = new FacetValue("A", "1", 1);
    FacetValue value2 = new FacetValue("A", "2", 2, "2", false);
    FacetValue value3 = new FacetValue("B", "3", 3);
    assertEquals("", new FacetFilterBuilder(
      ImmutableMap.of("A", ImmutableList.of(value1, value2), "B", ImmutableList.of(value3))
    ).build());
  }

  @Test
  void buildFilter() {
    FacetValue facetValue = new FacetValue("facet", "v", 1, "v", true);
    assertEquals("facet:v", new FacetFilterBuilder(ImmutableMap.of("facet", ImmutableList.of(facetValue))).build());
  }

  @Test
  void buildMixed() {
    FacetValue filter = new FacetValue("facet", "v", 1, "v", true);
    FacetValue noFilter = new FacetValue("B", "3", 3);
    assertEquals("facet:v", new FacetFilterBuilder(ImmutableMap.of(
      "facet", ImmutableList.of(filter),
      "B", ImmutableList.of(noFilter)
    )).build());
  }

  @Test
  void build() {
    FacetFilterBuilder builder = new FacetFilterBuilder();
    assertEquals("", builder.build());

    FacetValue facet1 = new FacetValue("facet", "1", 2);
    FacetValue facet2 = new FacetValue("facet", "2", 1);
    FacetValue barFoo = new FacetValue("bar", "foo", 3);

    builder.enable(facet2, facet1, barFoo);
    assertEquals("facet:2,1;bar:foo", builder.build());

    // enable enabled, nop
    builder.enable(facet2);
    assertEquals("facet:2,1;bar:foo", builder.build());

    builder.toggle(facet1);
    assertEquals("facet:2;bar:foo", builder.build());

    // disable disabled, nop
    builder.disable(facet1);
    assertEquals("facet:2;bar:foo", builder.build());

    builder.toggle(facet1,barFoo);
    assertEquals("facet:2,1", builder.build());

    builder.disable(facet1, barFoo);
    assertEquals("facet:2", builder.build());

    builder.enable(barFoo);
    builder.clear("facet");
    assertEquals("bar:foo", builder.build());

    builder.toggle(facet1, barFoo, facet2);
    assertEquals("facet:1,2", builder.build());

    builder.clear();
    assertEquals("", builder.build());
  }
}
