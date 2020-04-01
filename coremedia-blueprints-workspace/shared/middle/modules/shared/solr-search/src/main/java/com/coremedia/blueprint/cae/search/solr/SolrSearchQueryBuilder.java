package com.coremedia.blueprint.cae.search.solr;

import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchFilterProvider;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.cae.search.facet.FacetFilters;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The default {@link SolrQueryBuilder} implementation.
 */
public class SolrSearchQueryBuilder implements SolrQueryBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(SolrSearchQueryBuilder.class);
  private static final String CONSTANT_FILTER_QUERY = createConstantFilterQuery();

  private SearchPreprocessor<SearchQueryBean> searchPreprocessor;
  private List<SearchFilterProvider> searchFilterProviders;

  private boolean preview = false;

  public void setSearchPreprocessor(SearchPreprocessor<SearchQueryBean> searchPreprocessor) {
    this.searchPreprocessor = searchPreprocessor;
  }

  public void setSearchFilterProviders(List<SearchFilterProvider> searchFilterProviders) {
    this.searchFilterProviders = searchFilterProviders;
  }

  @Override
  public SolrQuery buildQuery(SearchQueryBean input) {
    SolrQuery q = new SolrQuery();
    setDefaultValues(input, q);
    setContext(input, q);
    setInput(input, q);
    setFacets(input, q);
    setSpellcheck(input, q);
    if (input.isHighlightingEnabled()) {
      setHighlight(q);
    }
    return q;
  }

  protected void setDefaultValues(SearchQueryBean input, SolrQuery q) {
    q.setParam(SolrSearchParams.QT, input.getSearchHandler().toString());
    q.addField(SearchConstants.FIELDS.ID.toString());
    String defaultQuery = CONSTANT_FILTER_QUERY;
    if (!input.isNotSearchableFlagIgnored()) {
      // exclude documents marked as "notSearchable" from result
      Condition notSearchableCondition = Condition.is(SearchConstants.FIELDS.NOT_SEARCHABLE, Value.exactly("false"));
      defaultQuery = CONSTANT_FILTER_QUERY + AND + convertCondition(notSearchableCondition);
    }
    q.addFilterQuery(defaultQuery);
    if (searchFilterProviders != null) {
      for (SearchFilterProvider searchFilterProvider : searchFilterProviders) {
        for (Object cond : searchFilterProvider.getFilter(this.isPreview())) {
          String conditionAsString = null;
          if (cond instanceof Condition) {
            conditionAsString = convertCondition((Condition) cond);
          } else if (cond instanceof String) {
            conditionAsString = (String) cond;
          } else {
            LOG.warn("Cannot handle Filter values of this type");
          }
          if (StringUtils.hasText(conditionAsString)) {
            q.addFilterQuery(conditionAsString);
          }
        }
      }
    }
  }

  protected void setContext(SearchQueryBean input, SolrQuery q) {
    if (StringUtils.hasLength(input.getContext())) {
      Condition context = Condition.is(
              SearchConstants.FIELDS.NAVIGATION_PATHS,
              Value.exactly("\\/" + input.getContext())
      );
      q.addFilterQuery(convertCondition(context));
    }
  }

  protected void setInput(SearchQueryBean input, SolrQuery q) {
    // set the query
    if (StringUtils.hasLength(input.getQuery())) {
      if (searchPreprocessor != null) {
        searchPreprocessor.preProcess(input);
      }
      setQuery(q, input.getQuery());
    }
    // convert the filters
    setFilters(q, input.getFilters());
    // handle basics (offset, limit, sorting)
    setOffset(q, input.getOffset());
    setLimit(q, input.getLimit());
    setSortFields(q, input.getSortFields());
  }

  protected static void setFilters(SolrQuery q, List<Condition> filters) {
    for (Condition cond : filters) {
      String conditionAsString = convertCondition(cond);
      q.addFilterQuery(conditionAsString);
    }
  }

  protected static void setOffset(SolrQuery solrQuery, int offset) {
    if (offset < 0) {
      solrQuery.setStart(null);
    } else {
      solrQuery.setStart(offset);
    }
  }

  protected static void setLimit(SolrQuery solrQuery, int limit) {
    int lim = Math.min(limit, SolrSearchParams.MAX_LIMIT);
    if (lim < 0) {
      solrQuery.setRows(SolrSearchParams.MAX_LIMIT);
    } else {
      solrQuery.setRows(lim);
    }
  }

  protected static void setSortFields(SolrQuery solrQuery, List<String> sortFields) {
    for (String sortField : sortFields) {
      solrQuery.addSort(newSortClause(sortField));
    }
  }

  private static SolrQuery.SortClause newSortClause(String sortSpec) {
    sortSpec = sortSpec.trim();

    for (Map.Entry<String, SolrQuery.ORDER> entry : SORT_ORDER_MAPPING.entrySet()) {
      if (sortSpec.length() > entry.getKey().length() + 1 &&
          sortSpec.substring(sortSpec.length() - entry.getKey().length() - 1).equalsIgnoreCase(' ' + entry.getKey())) {
        String sort = sortSpec.substring(0, sortSpec.length() - entry.getKey().length() - 1).trim();
        return new SolrQuery.SortClause(sort, entry.getValue());
      }
    }

    // default order is descending
    return new SolrQuery.SortClause(sortSpec, SolrQuery.ORDER.desc);
  }

  protected static void setQuery(SolrQuery solrQuery, String query) {
    solrQuery.setQuery(getQueryClause(query));
  }

  protected static void setFacets(SearchQueryBean input, SolrQuery q) {
    Map<String, String> facetFields = input.getFacetFieldsMap();
    if (!facetFields.isEmpty()) {
      q.setFacet(true);

      Map<String, List<String>> filterValuesByFacetKey = FacetFilters.parse(input.getFacetFilters());

      // Request faceting on fields. On the fly, add currently enabled filters for facet values
      int tagId = 0;
      for (Map.Entry<String, String> facet : facetFields.entrySet()) {
        String facetKey = facet.getKey();
        String facetField = facet.getValue();
        Map<String, String> facetFieldLocalParams = new HashMap<>();

        // Set active filters and exclude them from faceting, facet results are returned as if the filter wasn't set
        List<String> filterValues = filterValuesByFacetKey.get(facetKey);
        if (filterValues != null && !filterValues.isEmpty()) {
          String filter = filterValues.stream()
            .map(ClientUtils::escapeQueryChars)
            .collect(Collectors.joining(SolrQueryBuilder.OR, "(", ")"));

          String tag = "f" + tagId++;
          String filterLocalParams = SolrSearchFormatHelper.formatLocalParameters(ImmutableMap.of("tag", tag));
          String fq = String.format("%s%s:%s", filterLocalParams, ClientUtils.escapeQueryChars(facetField), filter);
          q.addFilterQuery(fq);
          facetFieldLocalParams.put("ex", tag);
        }

        if (!facetKey.equals(facetField)) {
          facetFieldLocalParams.put("key", facetKey);
        }
        q.addFacetField(SolrSearchFormatHelper.formatLocalParameters(facetFieldLocalParams) + facetField);
      }

      // add prefix
      String facetPrefix = input.getFacetPrefix();
      if (facetPrefix != null && !facetPrefix.isEmpty()) {
        q.setFacetPrefix(facetPrefix);
      }
      if (input.getFacetMinCount() > 0) {
        q.setFacetMinCount(input.getFacetMinCount());
      }
      if (input.getFacetLimit() != SearchQueryBean.DEFAULT_FACET_LIMIT) {
        q.setFacetLimit(input.getFacetLimit());
      }

    }
  }

  protected static void setSpellcheck(SearchQueryBean input, SolrQuery q) {
    if (input.isSpellcheckSuggest()) {
      q.setParam("spellcheck", "true");
    }
  }

  protected static void setHighlight(SolrQuery q) {
    q.setHighlight(true);
    q.setParam("hl.fl",
            SearchConstants.FIELDS.TEASER_TITLE.toString(),
            SearchConstants.FIELDS.TEASER_TEXT.toString(),
            SearchConstants.FIELDS.HTML_DESCRIPTION.toString());
  }

  protected static String getQueryClause(String query) {
    if ("".equals(query) || "*".equals(query) || "?".equals(query) || "+".equals(query) || "-".equals(query)) {
      return "";
    }
    return escapeLocalParamsQueryString(query);
  }

  /**
   * Escapes LocalParams {!...} in query string.
   *
   * @param query the query string
   * @return the escaped query string
   * @see <a href="https://lucene.apache.org/solr/guide/8_4/local-parameters-in-queries.html">
   *   Solr Reference Guide: Local Parameters in Queries</a>
   */
  private static String escapeLocalParamsQueryString(String query) {
    return query.startsWith("{!") ? "\\" + query : query;
  }

  protected static String createConstantFilterQuery() {
    // exclude error documents from result
    Condition feederStateCondition = Condition.is("feederstate", Value.exactly("SUCCESS"));
    return convertCondition(feederStateCondition);
  }

  public static String convertCondition(Condition cond) {
    StringBuilder sb = new StringBuilder();
    // prefix with "-" if a "NOT" query
    sb.append(cond.getOp() == Condition.Operators.ISNOT ? "-" : "");
    // add field
    sb.append(cond.getField());
    // add colon
    sb.append(":");
    // add square brackets for range queries
    sb.append(cond.getOp() == Condition.Operators.LOWERTHAN || cond.getOp() == Condition.Operators.GREATERTHAN ?
            OPENING_BRACKET : "");
    sb.append(cond.getOp() == Condition.Operators.LOWERTHAN ?
            ANY_VALUE_TO : "");
    // add value
    sb.append(convertValue(cond.getValue()));
    sb.append(cond.getOp() == Condition.Operators.GREATERTHAN ?
            TO_ANY_VALUE : "");
    sb.append(cond.getOp() == Condition.Operators.LOWERTHAN || cond.getOp() == Condition.Operators.GREATERTHAN ?
            CLOSING_BRACKET : "");
    return sb.toString();
  }

  protected static String convertValue(Value value) {
    String sep = value.getOp() == Value.Operators.AND ? AND : OR;
    // join values by operator
    Collection<String> values = value.getValue();
    String result = StringUtils.collectionToDelimitedString(values, sep);
    if (values.size() < 2) {
      return result;
    }
    return "(" + result + ")";

  }

  @Override
  public boolean isPreview() {
    return preview;
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
  }
}
