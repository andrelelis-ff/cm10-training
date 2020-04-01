package com.coremedia.blueprint.cae.search.facet;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder to create a specification of enabled filters for facet values that can be passed to
 * {@link com.coremedia.blueprint.cae.search.SearchQueryBean#setFacetFilters(String)}
 *
 * @since 1810
 * @cm.template.api
 */
@DefaultAnnotation(NonNull.class)
public class FacetFilterBuilder {

  private final Map<String, LinkedHashSet<FacetValue>> facets = new LinkedHashMap<>();

  /**
   * Creates a new builder without any enabled filters.
   */
  public FacetFilterBuilder() {
    this(ImmutableMap.of());
  }

  /**
   * Creates a new builder from all given facet values that are enabled filters as returned by
   * {@link FacetValue#isFilter()}.
   *
   * @param facets map of facets to collection of facet values.
   */
  public FacetFilterBuilder(Map<String, Collection<FacetValue>> facets) {
    for (Map.Entry<String, Collection<FacetValue>> entry : facets.entrySet()) {
      String facet = entry.getKey();
      Collection<FacetValue> facetValues = entry.getValue();
      LinkedHashSet<FacetValue> filterValues = facetValues.stream()
        .filter(FacetValue::isFilter)
        .collect(Collectors.toCollection(LinkedHashSet::new));
      this.facets.put(facet, filterValues);
    }
  }

  /**
   * Builds and returns a filter string for the enabled facet value filters.
   *
   * @return filter string for {@link com.coremedia.blueprint.cae.search.SearchQueryBean#setFacetFilters(String)}
   * @cm.template.api
   */
  public String build() {
    return FacetFilters.format(facets);
  }

  /**
   * Clears all enabled filters.
   *
   * @return this
   * @cm.template.api
   */
  public FacetFilterBuilder clear() {
    facets.clear();
    return this;
  }

  /**
   * Clears all filters for values of the given facet.
   *
   * @param facet facet
   * @return this
   * @cm.template.api
   */
  public FacetFilterBuilder clear(String facet) {
    facets.remove(facet);
    return this;
  }

  /**
   * Toggles the enabledness of the filter for the given facet values.
   *
   * @param facetValue facet value to toggle filter for
   * @param moreFacetValues more facet values to toggle filters for
   * @return this
   * @cm.template.api
   */
  public FacetFilterBuilder toggle(FacetValue facetValue, FacetValue... moreFacetValues) {
    change(facetValue, moreFacetValues, true, true);
    return this;
  }

  /**
   * Enables the filter for the given facet values.
   *
   * @param facetValue facet value to enable filter for
   * @param moreFacetValues more facet values to enable filters for
   * @return this
   * @cm.template.api
   */
  public FacetFilterBuilder enable(FacetValue facetValue, FacetValue... moreFacetValues) {
    change(facetValue, moreFacetValues, false, true);
    return this;
  }

  /**
   * Disables the filter for the given facet values.
   *
   * @param facetValue facet value to disable filter for
   * @param moreFacetValues more facet values to disable filters for
   * @return this
   * @cm.template.api
   */
  public FacetFilterBuilder disable(FacetValue facetValue, FacetValue... moreFacetValues) {
    change(facetValue, moreFacetValues, true, false);
    return this;
  }

  private void change(FacetValue facetValue, FacetValue[] moreFacetValues, boolean remove, boolean add) {
    change(facetValue, remove, add);
    for (FacetValue moreFacetValue : moreFacetValues) {
      change(moreFacetValue, remove, add);
    }
  }

  private void change(FacetValue facetValue, boolean remove, boolean add) {
    LinkedHashSet<FacetValue> values = facets.computeIfAbsent(facetValue.getFacet(), s -> new LinkedHashSet<>());
    if (remove && values.remove(facetValue)) {
      return;
    }
    if (add) {
      values.add(facetValue);
    }
  }

}
