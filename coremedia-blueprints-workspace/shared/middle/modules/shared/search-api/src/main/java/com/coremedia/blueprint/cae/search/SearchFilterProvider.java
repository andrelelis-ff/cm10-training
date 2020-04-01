package com.coremedia.blueprint.cae.search;

import java.util.List;

/**
 * A SearchFilterProvider allows adding custom filters to a search query.
 */
public interface SearchFilterProvider<T> {
  List<T> getFilter(boolean isPreview);
}