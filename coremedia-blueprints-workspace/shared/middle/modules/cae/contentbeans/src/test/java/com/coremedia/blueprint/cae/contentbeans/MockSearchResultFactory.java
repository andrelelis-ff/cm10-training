package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;

import java.util.List;

class MockSearchResultFactory implements SearchResultFactory {
  private final List limitedSearchHits;

  /**
   * A big number
   * <p>
   * Search results may be large, and this value is probably not suitable for
   * iteration, allocation and the like.  Therefore, we return a really big
   * number that will make unit tests sweat.
   */
  // A really BIG number, beyond int, but away from overflow borders.
  private long totalNumHits = Long.MAX_VALUE / 3;

  MockSearchResultFactory(List limitedSearchHits) {
    this.limitedSearchHits = limitedSearchHits;
  }

  MockSearchResultFactory(List limitedSearchHits, long totalNumHits) {
    this.limitedSearchHits = limitedSearchHits;
    if (totalNumHits >= 0) {
      this.totalNumHits = totalNumHits;
    }
  }

  @Override
  public SearchResultBean createSearchResult(SearchQueryBean searchInput, long cacheForInSeconds) {
    return createSearchResultUncached(searchInput);
  }

  @Override
  public SearchResultBean createSearchResultUncached(SearchQueryBean searchInput) {
    int offset = searchInput.getOffset();
    int limit = searchInput.getLimit();
    return new MockSearchResultBean(offset, offset+limit);
  }

  private class MockSearchResultBean extends SearchResultBean {
    private int from;
    private int to;

    MockSearchResultBean(int from, int to) {
      this.from = from;
      this.to = Math.min(to, limitedSearchHits.size());
    }

    @Override
    public List<?> getHits() {
      return limitedSearchHits.subList(from, to);
    }

    @Override
    public long getNumHits() {
      return totalNumHits;
    }
  }
}
