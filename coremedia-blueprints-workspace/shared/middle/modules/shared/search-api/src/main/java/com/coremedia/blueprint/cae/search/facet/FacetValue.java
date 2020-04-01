package com.coremedia.blueprint.cae.search.facet;

import com.coremedia.blueprint.cae.search.ValueAndCount;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A single value of a search facet.
 *
 * <p>In addition to the actual {@link #getValue() value} and {@link #getCount() count}, values can have a
 * different {@link #getLabel() label} that is more appropriate for displaying it to a user.
 *
 * <p>Method {@link #isFilter()} returns true for facet values that are currently set as search filters.
 *
 * @since 1810
 * @cm.template.api
 */
@DefaultAnnotation(NonNull.class)
public class FacetValue extends ValueAndCount {

  private final String facet;
  private final String label;
  private final boolean filter;

  /**
   * Creates a new FacetValue that is not a {@link #isFilter() filter} and which uses the value for
   * {@link #getLabel()}.
   *
   * @param facet facet name
   * @param value facet value
   * @param count facet count
   */
  public FacetValue(String facet, String value, long count) {
    this(facet, value, count, value, false);
  }

  /**
   * Creates a new FacetValue.
   *
   * @param facet facet name
   * @param value facet value
   * @param count facet count
   * @param label facet label, can be the same as {@code value}
   * @param filter true if the value is set as search filter
   */
  public FacetValue(String facet, String value, long count, String label, boolean filter) {
    super(value, count);
    this.facet = facet;
    this.label = label;
    this.filter = filter;
  }

  /**
   * Returns the name of the facet.
   *
   * @return facet name
   * @cm.template.api
   */
  public String getFacet() {
    return facet;
  }

  /**
   * Returns a label that can be displayed to users.
   *
   * <p>The method can return an empty string if no label could be computed for
   * a valid value. In such a case, frontend code could still handle localization.
   *
   * @return facet label
   * @cm.template.api
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns if this facet value is currently set as search filter.
   *
   * @return true if this facet value is a search filter
   * @cm.template.api
   */
  public boolean isFilter() {
    return filter;
  }

  @Override
  public String toString() {
    return "FacetValue[" +
           "value=" + getValue() +
           ", count=" + getCount() +
           ", facet=" + facet +
           ", label='" + label + '\'' +
           ", filter=" + filter +
           ']';
  }
}
