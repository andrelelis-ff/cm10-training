package com.coremedia.blueprint.analytics.elastic.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 * The bean corresponding to the <code>CMALXPageList</code> document type. It selects its list of
 * {@link CMLinkable}s by firing an analytics query.
 */
public class CMALXPageListImpl extends CMALXPageListBase {

  /**
   * Get the list of {@link CMLinkable}s selected by an analytics query.
   *
   * @return the list of {@link CMLinkable}s selected by an analytics query
   */
  @Override
  public List<CMLinkable> getItemsUnfiltered() {
    int maxLength = getMaxLength();

    final Iterable trackedLinkables = Iterables.filter(getTrackedItemsUnfiltered(), instanceOf(CMLinkable.class));
    @SuppressWarnings("unchecked") // we've just removed the non-linkables!
    List<CMLinkable> result = copyOf(trackedLinkables).subList(0,  Math.min(Iterables.size(trackedLinkables), maxLength));

    // default content
    if (result.isEmpty()) {
      List<CMLinkable> defaultContentLinks = getDefaultContent();
      final Iterable<CMLinkable> filteredDefaultContent = Iterables.filter(defaultContentLinks, instanceOf(CMLinkable.class));
      result = copyOf(filteredDefaultContent).subList(0, Math.min(Iterables.size(filteredDefaultContent), maxLength));
    }
    return result;
  }
}
