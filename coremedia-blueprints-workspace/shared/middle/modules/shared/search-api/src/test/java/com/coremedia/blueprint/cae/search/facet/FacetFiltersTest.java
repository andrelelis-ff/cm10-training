package com.coremedia.blueprint.cae.search.facet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FacetFiltersTest {

  @Test
  void formatEmpty() {
    assertEquals("", FacetFilters.format(ImmutableMap.of()));
  }

  @Test
  void format() {
    assertEquals("foo:2;bar:1,2", FacetFilters.format(ImmutableMap.of(
      "none", ImmutableList.of(),
      "foo", ImmutableList.of(new FacetValue("foo", "2", 33)),
      "bar", ImmutableList.of(new FacetValue("bar", "1", 11), new FacetValue("bar", "2", 22))
    )));
  }

  @Test
  void formatSpecialCharacters() {
    String facet = "a:b;c\\d,e";
    assertEquals("a\\:b\\;c\\\\d\\,e:foo\\\\\\:\\;\\,&,3\\,4", FacetFilters.format(ImmutableMap.of(
      facet, ImmutableList.of(new FacetValue(facet, "foo\\:;,&", 33), new FacetValue(facet, "3,4", 34))
    )));
  }

  @Test
  void parseEmpty() {
    assertEquals(ImmutableMap.of(), FacetFilters.parse(null));
    assertEquals(ImmutableMap.of(), FacetFilters.parse(""));
  }

  @Test
  void parse() {
    assertEquals(ImmutableMap.of("foo", ImmutableList.of("2"), "bar", ImmutableList.of("1", "2"))
      , FacetFilters.parse("none:;foo:2;bar:1,2,"));
  }

  @Test
  void parseSpecialCharacters() {
    assertEquals(ImmutableMap.of("a:b;c\\d,e", ImmutableList.of("foo\\:;,&:2", "3,4"), "bar", ImmutableList.of("1", "2"))
      , FacetFilters.parse("a\\:b\\;c\\\\d\\,e:foo\\\\\\:\\;\\,&:2,3\\,4;bar:1,2,"));
  }

  @Test
  void split() {
    assertEquals(ImmutableList.of("a\\:b\\;c\\\\d\\,e:foo\\\\\\:\\;\\,&:2,3\\,4", "bar:1,2,"),
      FacetFilters.split("a\\:b\\;c\\\\d\\,e:foo\\\\\\:\\;\\,&:2,3\\,4;bar:1,2,", ';'));
  }

  @Test
  void splitLimit() {
    assertEquals(ImmutableList.of("a:b:c"), FacetFilters.split("a:b:c", ':', 0));
    assertEquals(ImmutableList.of("a:b:c"), FacetFilters.split("a:b:c", ':', 1));
    assertEquals(ImmutableList.of("a", "b:c"), FacetFilters.split("a:b:c", ':', 2));
    assertEquals(ImmutableList.of("a", "b", "c"), FacetFilters.split("a:b:c", ':', 3));
    assertEquals(ImmutableList.of("a", "b", "c"), FacetFilters.split("a:b:c", ':', 4));
  }
}
