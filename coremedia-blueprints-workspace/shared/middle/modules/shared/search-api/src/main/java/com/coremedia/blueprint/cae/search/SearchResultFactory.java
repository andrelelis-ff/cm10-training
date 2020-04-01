package com.coremedia.blueprint.cae.search;

/**
 * A SearchResultFactory generates {@link SearchResultBean}s for {@link SearchQueryBean}s.
 */
public interface SearchResultFactory {

  /**
   * Queries a search index with the query criteria provided. Caches the result for the given time.
   * @param searchInput the query criteria
   * @param cacheForInSeconds the time to cache the results for in seconds or 0 for no caching
   * @return the result of the query
   */
  SearchResultBean createSearchResult(SearchQueryBean searchInput, long cacheForInSeconds);

  /**
   * Queries a search index with the query criteria provided.
   * @param searchInput the query criteria
   * @return the result of the query
   */
  SearchResultBean createSearchResultUncached(SearchQueryBean searchInput);

}
