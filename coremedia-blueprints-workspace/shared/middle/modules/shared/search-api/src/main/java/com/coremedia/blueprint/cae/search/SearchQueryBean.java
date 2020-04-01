package com.coremedia.blueprint.cae.search;

import com.coremedia.blueprint.cae.search.facet.FacetFilterBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The SearchQueryBean is a search engine independent representation of a search query. It provides the set of all
 * query parameters that should be supported by search engine integrations for the CoreMedia Website blueprint.
 */
public class SearchQueryBean {

  /**
   * The default value for the facet.limit parameter in Solr.
   */
  public static final int DEFAULT_FACET_LIMIT = 100;

  /**
   * Field names in the index. Need to map Solr schema
   */
  public enum SEARCH_HANDLER {
    DYNAMICCONTENT("/select"),
    FULLTEXT("/cmdismax"),
    SUGGEST("/suggest");

    private String name;

    SEARCH_HANDLER(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }


  // basics
  private SEARCH_HANDLER searchHandler = SEARCH_HANDLER.DYNAMICCONTENT;
  private String query = "";
  private List<Condition> filters = new ArrayList<>();
  private int offset = 0;
  private int limit = 10; //NOSONAR
  private List<String> sortFields = new ArrayList<>();

  // faceting support
  private ImmutableMap<String, String> facetFields = ImmutableMap.of();
  private String facetPrefix = "";
  private int facetMinCount = -1;
  private int facetLimit = DEFAULT_FACET_LIMIT;
  private String facetFilters;

  // spellcheck/didyoumean support
  private boolean spellcheckSuggest = false;

  // highlighting search results
  private boolean highlightingEnabled = false;

  //the context to search in
  private String context;

  //can be set to prohibit that the notsearchable flag is set as param in the query (used for document types != CMTeasable)
  private boolean notSearchableFlagIgnored = false;

  /**
   * Returns if the notsearchable flag should be ignored or not.
   * Default is 'false', so that this param is build for the query.
   * @return true if the notsearchable flag should be ignored, false otherwise
   */
  public boolean isNotSearchableFlagIgnored() {
    return notSearchableFlagIgnored;
  }

  /**
   * Can be called to keep the 'notsearchable' attribute out of the search query.
   * @param ignore true if the notsearchable flag should be ignored
   */
  public void setNotSearchableFlagIgnored(boolean ignore) {
    this.notSearchableFlagIgnored = ignore;
  }


  /**
   * Returns the literal value of the query. Can be a simple String for fulltext searches (e.g. London, "San Francisco").
   *
   * @return the query string.
   */
  public String getQuery() {
    return query;
  }

  /**
   * Sets the literal value of the query.
   *
   * @param query a string query.
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * Returns the list of filters.
   * A filter is a {@link Condition} representing a restriction that should be met by a search result, e.g.
   * "documentType IS CMArticle".
   *
   * @return the list of the filters.
   */
  public List<Condition> getFilters() {
    return filters;
  }


  /**
   * Sets the list of filters.
   * A filter is a {@link Condition} representing a restriction that should be met by a search result, e.g.
   * "documentType IS CMArticle".
   *
   * @param filters a list of filters
   */
  public void setFilters(List<Condition> filters) {
    this.filters = filters;
  }

  /**
   * Adds a filter to the list of filters.
   * A filter is a {@link Condition} representing a restriction that should be met by a search result, e.g.
   * "documentType IS CMArticle".
   *
   * @param filter a filter to add to the list of filters
   */
  public void addFilter(Condition filter) {
    this.filters.add(filter);
  }

  /**
   * Returns the offset in the complete search result for the query where the returned hits should begin.
   * The offset is 0 bound. The default value is {@link #offset}.
   *
   * @return the search result offset for this query
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Sets the offset in the complete search result for the query where the returned hits should begin.
   * The offset is 0 bound. The default value is {@link #offset}.
   *
   * @param offset the search result offset for this query
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * Returns the maximum number of hits to be returned as a result of this query. The default value is <code>10</code>.
   *
   * @return the maximum number of hits to be returned as a result of this query
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Sets the maximum number of hits to be returned as a result of this query. The default value is <code>10</code>.
   *
   * @param limit the maximum number of hits to be returned as a result of this query
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * Returns the list of fields the search result should be sorted by.
   * A field contains the field's name and and optional sort direction parameter, e.g. "score", "name ASC",
   * "publicationdate DESC".
   *
   * @return the list of fields the search result should be sorted by
   */
  public List<String> getSortFields() {
    return sortFields;
  }

  /**
   * Sets the list of fields the search result should be sorted by.
   * A field contains the field's name and and optional sort direction parameter, e.g. "score", "name ASC",
   * "publicationdate DESC".
   *
   * @param sortFields the list of fields the search result should be sorted by
   */
  public void setSortFields(List<String> sortFields) {
    this.sortFields = new ArrayList<>(sortFields);
  }

  /**
   * Returns the list of field names for which facets should be returned.
   *
   * <p>This method returns the values of the map returned by {@link #getFacetFieldsMap()}.
   *
   * @return immutable list of field names for which facets should be returned.
   */
  public List<String> getFacetFields() {
    return ImmutableList.copyOf(facetFields.values());
  }

  /**
   * Sets the field names for which facets should be returned.
   *
   * <p>This is a convenience method, which calls {@link #setFacetFieldsMap(Map)} with a map of identical keys and
   * values, i.e. where symbolic facet names and field names are identical.
   *
   * @param facetFields the field names for which facets should be returned.
   */
  public void setFacetFields(List<String> facetFields) {
    LinkedHashMap<String, String> map = facetFields.stream()
      .collect(Collectors.toMap(facetField -> facetField, facetField -> facetField, (a, b) -> b, LinkedHashMap::new));
    setFacetFieldsMap(map);
  }

  /**
   * Sets a map from symbolic names of requested search facets to their index field names.
   *
   * <p>The given map's iteration order defines the order of facet results in {@link SearchResultBean#getFacetResult()}.
   *
   * @param facetFields map from symbolic names to index field names, empty to not request facets
   * @since 1810
   */
  public void setFacetFieldsMap(Map<String, String> facetFields) {
    this.facetFields = ImmutableMap.copyOf(facetFields);
  }

  /**
   * Returns the map from symbolic names of requested search facets to their index field names.
   *
   * @return map from symbolic names to index field names, empty to not request facets
   * @since 1810
   */
  public Map<String, String> getFacetFieldsMap() {
    return facetFields;
  }

  /**
   * Returns the maximum number of facets to return. The default value is {@value #DEFAULT_FACET_LIMIT}.
   *
   * @return the maximum number of facets to return
   */
  public int getFacetLimit() {
    return facetLimit;
  }

  /**
   * Sets the maximum number of facets to return. The default value is {@value #DEFAULT_FACET_LIMIT}.
   *
   * @param facetLimit the maximum number of facets to return
   */
  @SuppressWarnings("unused")
  public void setFacetLimit(int facetLimit) {
    this.facetLimit = facetLimit;
  }

  /**
   * Returns the minimum number of occurences of a term required to be included as a facet in the results. The default
   * value is <code>-1</code> which means that there is no minimum number.
   *
   * @return the minimum number of occurences of a term required to be included as a facet in the results
   */
  public int getFacetMinCount() {
    return facetMinCount;
  }

  /**
   * Sets the minimum number of occurences of a term required to be included as a facet in the results.  The default
   * value is <code>-1</code> which means that there is no minimum number.
   *
   * @param facetMinCount the minimum number of occurences of a term required to be included as a facet in the results
   */
  public void setFacetMinCount(int facetMinCount) {
    this.facetMinCount = facetMinCount;
  }

  /**
   * Returns the prefix that facets need to have to be included in the result. The prefix is an empty String by default.
   *
   * @return the prefix that facets need to have to be included in the result
   */
  public String getFacetPrefix() {
    return facetPrefix;
  }

  /**
   * Sets the prefix that facets need to have to be included in the result. The prefix is an empty String by default.
   *
   * @param facetPrefix the prefix that facets need to have to be included in the result
   */
  public void setFacetPrefix(String facetPrefix) {
    this.facetPrefix = facetPrefix;
  }

  /**
   * Sets the search filters for selected facet values.
   *
   * @param facetFilters search filters in the format of {@link FacetFilterBuilder#build()}
   * @since 1810
   */
  public void setFacetFilters(String facetFilters) {
    this.facetFilters = facetFilters;
  }

  /**
   * Returns the search filters for selected facet values.
   *
   * @return search filters in the format of {@link FacetFilterBuilder#build()}
   * @since 1810
   */
  public String getFacetFilters() {
    return facetFilters;
  }

  /**
   * Returns whether this query should include spell suggestions along the result. The default value is <code>false</code>.
   *
   * @return whether this query should include spell suggestions along the result.
   */
  public boolean isSpellcheckSuggest() {
    return spellcheckSuggest;
  }

  /**
   * Sets whether this query should include spell suggestions along the result. The default value is <code>false</code>.
   *
   * @param spellcheckSuggest whether this query should include spell suggestions along the result.
   */
  public void setSpellcheckSuggest(boolean spellcheckSuggest) {
    this.spellcheckSuggest = spellcheckSuggest;
  }

  public boolean isHighlightingEnabled() {
    return highlightingEnabled;
  }

  public void setHighlightingEnabled(boolean highlightingEnabled) {
    this.highlightingEnabled = highlightingEnabled;
  }

  /**
   * Returns the numerical id of the context that the search should be restricted to.
   *
   * @return the numerical id of the context that the search should be restricted to.
   */
  public String getContext() {
    return context;
  }

  /**
   * Sets the context of the site to search in. This is important if you are running multiple sites.
   *
   * @param context The numerical id of the site to search in
   */

  public void setContext(String context) {
    this.context = context;
  }

  /**
   * Returns the search handler {@link SEARCH_HANDLER} used for this search.
   *
   * @return the search handler {@link SEARCH_HANDLER} used for this search.
   */

  public SEARCH_HANDLER getSearchHandler() {
    return searchHandler;
  }

  /**
   * Sets the search handler {@link SEARCH_HANDLER} to use.
   *
   * @param searchHandler The search handler {@link SEARCH_HANDLER} to use for this search
   */

  public void setSearchHandler(SEARCH_HANDLER searchHandler) {
    this.searchHandler = searchHandler;
  }

}
