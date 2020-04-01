package com.coremedia.blueprint.cae.search.solr;

import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.SegmentResolver;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;

import static com.coremedia.blueprint.cae.search.SearchConstants.FIELDS.CONTEXTS;
import static com.coremedia.blueprint.cae.search.SearchConstants.FIELDS.SEGMENT;

/**
 * {@link com.coremedia.blueprint.cae.search.SegmentResolver} implementation based on an Apache Solr index
 * created with the Blueprint CAE Feeder.
 *
 * @since 7.5.13
 */
public class SolrSegmentResolver implements SegmentResolver {

  private int cacheForSeconds = 10;
  private SearchResultFactory searchResultFactory;

  /**
   * Sets the {@link com.coremedia.blueprint.cae.search.SearchResultFactory}.
   *
   * @param searchResultFactory the {@link com.coremedia.blueprint.cae.search.SearchResultFactory}
   */
  @Required
  public void setSearchResultFactory(@NonNull SearchResultFactory searchResultFactory) {
    Objects.requireNonNull(searchResultFactory);
    this.searchResultFactory = searchResultFactory;
  }

  /**
   * Sets the time in seconds to cache the result.
   *
   * <p>The default is 10 seconds.
   *
   * @param cacheForSeconds time to cache the result or 0 for no caching
   */
  public void setCacheForSeconds(int cacheForSeconds) {
    this.cacheForSeconds = cacheForSeconds;
  }

  @Override
  @Nullable
  public <T> T resolveSegment(int contextId, @NonNull String segment, @NonNull Class<T> resultClass) {
    SearchQueryBean queryBean = new SearchQueryBean();
    queryBean.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT);
    queryBean.setNotSearchableFlagIgnored(true);

    String escapedSegment = CharMatcher.is('"').replaceFrom(segment, "\\\"");
    String query = SEGMENT + ":\"" + escapedSegment + "\" AND " + CONTEXTS + ':' + contextId;
    queryBean.setQuery(query);

    SearchResultBean searchResult = searchResultFactory.createSearchResult(queryBean, cacheForSeconds);
    Optional<T> result = FluentIterable.from(searchResult.getHits()).filter(resultClass).first();
    return result.isPresent() ? result.get() : null;
  }

}
