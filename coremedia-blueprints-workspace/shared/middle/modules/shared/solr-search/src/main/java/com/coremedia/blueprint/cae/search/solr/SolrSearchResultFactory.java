package com.coremedia.blueprint.cae.search.solr;

import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.ValueAndCount;
import com.coremedia.blueprint.cae.search.facet.FacetFilters;
import com.coremedia.blueprint.cae.search.facet.FacetResult;
import com.coremedia.blueprint.cae.search.facet.FacetValue;
import com.coremedia.blueprint.id.Representation;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cache.EvaluationException;
import com.coremedia.cap.content.ContentRepository;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The Solr specific implementation of a {@link SearchResultFactory}.
 */
public class SolrSearchResultFactory implements SearchResultFactory, InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(SolrSearchResultFactory.class);

  private SolrQueryBuilder queryBuilder;
  private ContentRepository contentRepository;
  private Representation<Object> representationMapper;
  private SolrClient solrClient;
  private String collection;
  private Map<String, Function<String, String>> facetFieldLabelFunctions = Collections.emptyMap();

  @Override
  public SearchResultBean createSearchResult(SearchQueryBean searchInput, long cacheForInSeconds) {
    if (cacheForInSeconds <= 0) {
      return createSearchResultUncached(searchInput);
    }
    SolrQuery query = queryBuilder.buildQuery(searchInput);
    SolrQueryCacheKey cacheKey = new SolrQueryCacheKey(query, cacheForInSeconds);
    try {
      QueryResponse solrQueryResponse = contentRepository.getConnection().getCache().get(cacheKey);
      return createResultBean(searchInput, solrQueryResponse);
    } catch (EvaluationException e) {
      LOG.error("Error performing search: {}", query, e.getCause());
      return createEmptyResultBean(searchInput);
    }
  }

  @Override
  public SearchResultBean createSearchResultUncached(SearchQueryBean searchInput) {
    SolrQuery query = queryBuilder.buildQuery(searchInput);
    try {
      QueryResponse solrQueryResponse = doSearchSolr(query);
      return createResultBean(searchInput, solrQueryResponse);
    } catch (SolrServerException | IOException e) {
      LOG.error("Error performing search: {}", query, e);
      return createEmptyResultBean(searchInput);
    }
  }

  protected SearchResultBean createEmptyResultBean(SearchQueryBean searchInput) {
    SearchResultBean searchResultBean = new SearchResultBean();
    searchResultBean.setSearchQuery(searchInput);
    searchResultBean.setHitsPerPage(searchInput.getLimit());
    return searchResultBean;
  }

  protected SearchResultBean createResultBean(SearchQueryBean searchInput, QueryResponse q) {
    SearchResultBean searchResultBean = createEmptyResultBean(searchInput);
    // translate hits
    searchResultBean.setHits(translateHits(q));
    // translate highlighted results
    searchResultBean.setHighlightingResults(translateHighlightResults(q));
    searchResultBean.setNumHits(q.getResults().getNumFound());
    Map<String, String> facetFieldsMap = searchInput.getFacetFieldsMap();
    if (!facetFieldsMap.isEmpty()) {
      // translate facets (if applicable)
      searchResultBean.setFacetResult(translateFacets(searchInput, q));
    }
    if (searchInput.isSpellcheckSuggest()) {
      // translate search suggestion (if applicable)
      searchResultBean.setSpellSuggestion(translateSpellSuggestion(q));
    }
    if (searchInput.getSearchHandler() == SearchQueryBean.SEARCH_HANDLER.SUGGEST) {
      searchResultBean.setAutocompleteSuggestions(translateAutocompleteSuggestions(searchInput.getQuery(), q));
    }
    return searchResultBean;
  }

  protected List<Object> translateHits(QueryResponse q) {
    SolrDocumentList documentList = q.getResults();
    List<Object> hits = new ArrayList<>();
    for (SolrDocument solrDocument : documentList) {
      String id = (String) solrDocument.getFieldValue(SearchConstants.FIELDS.ID.toString());
      Object hit = representationMapper.fromID(id);
      if (representationMapper.isValid(hit)) {
        hits.add(hit);
      }
    }
    return hits;
  }

  protected FacetResult translateFacets(SearchQueryBean searchInput,
                                        QueryResponse response) {
    ImmutableMap.Builder<String, Collection<FacetValue>> builder = ImmutableMap.builder();
    for (FacetField facetField : response.getFacetFields()) {
      String facet = facetField.getName();
      if (facetField.getValues() != null) {
        List<FacetValue> values = facetField.getValues().stream()
          .map(count -> createFacetValue(searchInput, facet, count))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
        builder.put(facet, values);
      }
    }
    return new FacetResult(builder.build());
  }

  @Nullable
  private FacetValue createFacetValue(SearchQueryBean searchInput, String facet, FacetField.Count count) {
    String value = count.getName();
    if (value == null) {
      return null;
    }
    String fieldName = searchInput.getFacetFieldsMap().get(facet);
    String label = createFacetLabel(fieldName, value);
    if (label == null) {
      return null;
    }
    Map<String, List<String>> filters = FacetFilters.parse(searchInput.getFacetFilters());
    boolean enabled = filters.getOrDefault(facet, Collections.emptyList()).contains(value);
    return new FacetValue(facet, value, count.getCount(), label, enabled);
  }

  private String createFacetLabel(String fieldName, String value) {
    Function<String, String> labelFunction = facetFieldLabelFunctions.get(fieldName);
    return labelFunction == null ? value : labelFunction.apply(value);
  }

  protected String translateSpellSuggestion(QueryResponse q) {
    SpellCheckResponse solrSpellCheckResponse = q.getSpellCheckResponse();
    String suggestion = EMPTY;
    if (solrSpellCheckResponse != null) {
      suggestion = solrSpellCheckResponse.getCollatedResult();
      if (isNotBlank(suggestion)) {
        if (suggestion.startsWith("\"")) {
          suggestion = suggestion.substring(1);
        }
        if (suggestion.endsWith("\"")) {
          suggestion = suggestion.substring(0, suggestion.length() - 1);
        }
      }
    }
    return suggestion;
  }

  private List<ValueAndCount> translateAutocompleteSuggestions(String query, QueryResponse response) {
    // no suggestions for empty query
    if (query.isEmpty()) {
      return Collections.emptyList();
    }
    // no suggestions for a completed word
    char lastChar = query.charAt(query.length() - 1);
    if (lastChar == ' ' || lastChar == '-' || lastChar == '+' || lastChar == '"') {
      return Collections.emptyList();
    }

    List<ValueAndCount> result = new ArrayList<>();
    for (SpellCheckResponse.Collation collation : getCollationsFromSpellCheckResponse(response)) {
      ValueAndCount valueAndCount = getAutoCompleteSuggestionFromCollation(collation, query);
      if (valueAndCount != null) {
        result.add(valueAndCount);
      }
    }

    // if the query itself returns results, add it to the suggestions as well to show the count
    long hits = response.getResults().getNumFound();
    if (hits > 0) {
      result.add(new ValueAndCount(query, hits));
    }
    return result;
  }

  /**
   * Returns a list of {@link SpellCheckResponse.Collation collations} from the given Solr response.
   *
   * @param response Solr query response
   * @return list of collations
   */
  private List<SpellCheckResponse.Collation> getCollationsFromSpellCheckResponse(QueryResponse response) {
    SpellCheckResponse r = response.getSpellCheckResponse();
    if (r != null) {
      List<SpellCheckResponse.Collation> collatedResults = r.getCollatedResults();
      if (collatedResults != null) {
        return collatedResults;
      }
    }
    return Collections.emptyList();
  }

  /**
   * Returns a suggested query with count from the given Solr spellcheck collation.
   *
   * @param collation collation
   * @param query original query, used for preserving the case in the returned suggestion
   * @return suggestion or null if no suggestion can be obtained from the given collation
   */
  private ValueAndCount getAutoCompleteSuggestionFromCollation(SpellCheckResponse.Collation collation, String query) {
    // ignore collations with duplicate words. We don't want to suggest "Peter peter" for query "Peter p"
    if (!isCollationWithDuplicateCorrection(collation)) {
      String collationQuery = collation.getCollationQueryString();
      long hits = collation.getNumberOfHits();
      // ignore collations with open phrase queries. We don't check phrases. No suggestion for "\"Peter p"
      if (collationQuery != null && hits > 0 && !isOpenPhraseQuery(collationQuery)
              && !collationQuery.trim().isEmpty()) {
        String suggestion = preserveCase(query, collationQuery);
        return new ValueAndCount(suggestion, hits);
      }
    }
    return null;
  }

  /**
   * Returns whether the given collation from the SpellCheckComponent contains the same corrections for different
   * terms (such as "peter peter" for a query "peter p"). We don't want to return such duplicates.
   *
   * @param collation collation element from spell check result
   * @return true if duplicate corrections, false otherwise
   */
  private static boolean isCollationWithDuplicateCorrection(SpellCheckResponse.Collation collation) {
    List<SpellCheckResponse.Correction> misspellingsAndCorrections = collation.getMisspellingsAndCorrections();
    Collection<String> corrections = new ArrayList<>(misspellingsAndCorrections.size());
      for (SpellCheckResponse.Correction misspellingAndCorrection : misspellingsAndCorrections) {
        String correction = misspellingAndCorrection.getCorrection();
        if (corrections.contains(correction)) {
          return true;
        }
        corrections.add(correction);
      }
    return false;
  }

  /**
   * Returns whether the given suggested query is a started but not finished phrase query where the phrase query
   * spans multiple terms.
   * <p>
   * For example, it returns true for
   * <ul><li>{@code "foo ba}</li><li>{@code foo "bar hoo}</li></ul>
   * but false for
   * <ul><li>{@code foo}</li><li>{@code "foo bar" hoo}</li><li>{@code "foo}</li><li>{@code foo "bar}</li></ul>
   *
   * @param query query
   * @return true if open phrase query that spans multiple terms, false otherwise
   */
  private static boolean isOpenPhraseQuery(String query) {
    return hasUnbalancedQuotes(query) && query.lastIndexOf('"') < query.lastIndexOf(' ');
  }

  /**
   * Returns whether the given char sequence contains an odd number of double-quotes (").
   *
   * @param charSequence char sequence
   * @return true if odd, false if even
   */
  private static boolean hasUnbalancedQuotes(CharSequence charSequence) {
    boolean unbalanced = false;
    for (int i = 0; i < charSequence.length(); i++) {
      if (charSequence.charAt(i) == '\"') {
        unbalanced = !unbalanced;
      }
    }
    return unbalanced;
  }

  /**
   * Return the given suggestion but make its characters match the case of the given query string.
   *
   * @param query query string
   * @param suggestion suggestion
   * @return modified suggestion
   */
  static String preserveCase(String query, String suggestion) {
    if (query.isEmpty()) {
      return suggestion;
    }
    StringBuilder sb = new StringBuilder();
    int queryIndex = 0;
    int suggestionIndex = 0;

    while (queryIndex < query.length() && suggestionIndex < suggestion.length()) {
      int queryChar = query.charAt(queryIndex);
      int queryLower = Character.toLowerCase(queryChar);

      while (suggestionIndex < suggestion.length()) {
        int c = suggestion.charAt(suggestionIndex++);
        if (queryLower == c || queryChar == c || queryChar == Character.toLowerCase(c)) {
          sb.appendCodePoint(queryChar);
          ++queryIndex;
          break;
        }
        sb.appendCodePoint(c);
      }
    }

    sb.append(suggestion.substring(suggestionIndex, suggestion.length()));
    return sb.toString();
  }

  // contentbean : property : property value with search term highlighted
  protected Map<Object, Map<String, List<String>>> translateHighlightResults(QueryResponse q) {
    Map<Object, Map<String, List<String>>> highlightResults = new HashMap<>();

    Map<String, Map<String, List<String>>> highlightSnippets = q.getHighlighting();
    if (highlightSnippets != null) {
      for (Map.Entry<String, Map<String, List<String>>> entry : highlightSnippets.entrySet()) {
        String id = entry.getKey().substring(entry.getKey().indexOf("#") + 1);
        Object result = representationMapper.fromID(id);
        if (representationMapper.isValid(result)) {
          highlightResults.put(result, entry.getValue());
        }
      }
    }
    return highlightResults;
  }


  protected QueryResponse doSearchSolr(SolrQuery query) throws IOException, SolrServerException {
    return solrClient.query(collection, query);
  }

  @Override
  public void afterPropertiesSet() {
    if (solrClient == null) {
      throw new IllegalStateException("Required property not set: solrClient");
    }
    if (collection == null) {
      throw new IllegalStateException("Required property not set: collection");
    }
    LOG.info("Configured to search in collection {} of {}", collection, solrClient);
  }

  @Required
  public void setQueryBuilder(SolrQueryBuilder queryBuilder) {
    this.queryBuilder = queryBuilder;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public void setSolrClient(SolrClient solrClient) {
    this.solrClient = solrClient;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  private class SolrQueryCacheKey extends CacheKey<QueryResponse> {
    private static final String CACHE_CLASS = "com.coremedia.blueprint.cae.search.solr.SolrQueryCacheKey";
    private final SolrQuery solrQuery;
    private final long cacheForInSeconds;

    // redundant, only for efficiency
    private final String myEqualsValue;

    SolrQueryCacheKey(SolrQuery query, long cacheForInSeconds) {
      if (cacheForInSeconds <= 0) {
        throw new IllegalArgumentException("Query has unreasonable cache time: " + cacheForInSeconds);
      }
      this.cacheForInSeconds = cacheForInSeconds;
      this.solrQuery = query;
      myEqualsValue = solrQuery.toQueryString();
    }

    @Override
    public String cacheClass(Cache cache, QueryResponse value) {
      return CACHE_CLASS;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SolrQueryCacheKey that = (SolrQueryCacheKey) o;
      return cacheForInSeconds==that.cacheForInSeconds && myEqualsValue.equals(that.myEqualsValue);
    }

    @Override
    public int hashCode() {
      return Long.valueOf(cacheForInSeconds).hashCode() * 29 + myEqualsValue.hashCode();
    }

    /**
     * @return QueryResponse
     */
    @Override
    public QueryResponse evaluate(Cache cache) throws IOException, SolrServerException {
      QueryResponse queryResponse = null;
      try {
        Cache.disableDependencies();
        queryResponse = doSearchSolr(solrQuery);
      } finally {
        Cache.enableDependencies();
      }
      LOG.debug("Caching for {} s", cacheForInSeconds);
      Cache.cacheFor(cacheForInSeconds, TimeUnit.SECONDS);
      return queryResponse;
    }
  }

  @Required
  public void setRepresentationMapper(Representation<Object> representationMapper) {
    this.representationMapper = representationMapper;
  }

  /**
   * Sets a map from facet name to {@link Function} to compute {@link FacetValue#getLabel() labels}
   * for {@link FacetValue}s from their indexed value.
   *
   * <p>Functions can return {@code null} for a invalid values, for which no label can be computed.
   * Such invalid values will be omitted from the results.

   * <p>Functions can return empty strings to indicate that no label could be provided for a valid value.
   *
   * <p>If no function is registered for a facet, indexed values will be used as labels.
   *
   * @param facetFieldLabelFunctions map from facet name to function
   * @since 1810
   */
  public void setFacetFieldLabelFunctions(Map<String, Function<String, String>> facetFieldLabelFunctions) {
    this.facetFieldLabelFunctions = facetFieldLabelFunctions;
  }
}
