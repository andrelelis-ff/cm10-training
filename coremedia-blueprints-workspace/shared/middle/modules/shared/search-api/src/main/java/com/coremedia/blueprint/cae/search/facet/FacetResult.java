package com.coremedia.blueprint.cae.search.facet;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a search faceting result.
 *
 * @since 1810
 * @cm.template.api
 */
@DefaultAnnotation(NonNull.class)
public class FacetResult {

  private final ImmutableMap<String, Collection<FacetValue>> facets;

  public FacetResult() {
    this(ImmutableMap.of());
  }

  public FacetResult(Map<String, Collection<FacetValue>> facets) {
    this.facets = ImmutableMap.copyOf(facets);
  }

  /**
   * Returns a map of facets to collection of facet values.
   *
   * <p>The keys of the map are symbolic facet names that internally map to index fields.
   *
   * @return map of facet names to facet values
   * @cm.template.api
   */
  public Map<String, Collection<FacetValue>> getFacets() {
    return facets;
  }

  /**
   * Returns whether the given facet has a value where {@link FacetValue#isFilter()} returns true.
   *
   * @param facet facet name
   * @return true if facet has an enabled filter, false otherwise
   * @cm.template.api
   */
  public boolean isFacetWithFilter(@Nullable String facet) {
    return facet != null && getFacets()
      .getOrDefault(facet, Collections.emptyList())
      .stream()
      .anyMatch(FacetValue::isFilter);
  }

  /**
   * Creates a {@link FacetFilterBuilder} from {@link #getFacets()} that can be used to enable or disable
   * filters for the facets from this result.
   *
   * @return {@link FacetFilterBuilder}
   * @cm.template.api
   */
  public FacetFilterBuilder filter() {
    return new FacetFilterBuilder(getFacets());
  }
}
