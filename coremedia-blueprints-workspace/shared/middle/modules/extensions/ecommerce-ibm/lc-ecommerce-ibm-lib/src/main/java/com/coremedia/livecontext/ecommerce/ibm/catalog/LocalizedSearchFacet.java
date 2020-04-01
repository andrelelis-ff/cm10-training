package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.livecontext.ecommerce.search.SearchFacet;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;

/**
 * Helper class for localized search facets.
 */
class LocalizedSearchFacet implements SearchFacet {

  private final SearchFacet delegate;
  private final List<SearchFacet> childFacets;
  private String label;

  LocalizedSearchFacet(@NonNull SearchFacet searchFacet, @NonNull String label, @NonNull List<SearchFacet> childFacets) {
    this.delegate = searchFacet;
    this.label = label;
    this.childFacets = childFacets;
  }

  @NonNull
  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public int getCount() {
    return delegate.getCount();
  }

  @Override
  public boolean isSelected() {
    return delegate.isSelected();
  }

  @Override
  @NonNull
  public String getQuery() {
    return delegate.getQuery();
  }

  @Override
  public String getUrl() {
    return delegate.getUrl();
  }

  @Override
  @NonNull
  public Map<String, Object> getExtendedData() {
    return delegate.getExtendedData();
  }

  @Override
  @NonNull
  public List<SearchFacet> getChildFacets() {
    return childFacets;
  }
}
