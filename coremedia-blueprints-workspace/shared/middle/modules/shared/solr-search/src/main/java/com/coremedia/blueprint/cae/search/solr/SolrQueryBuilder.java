package com.coremedia.blueprint.cae.search.solr;

import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.Map;

/**
 * A SolrQueryBuilder is responsible for creating Solrj specific {@link SolrQuery} objects from generic
 * {@link com.coremedia.blueprint.cae.search.SearchQueryBean}s. Different implementations may want to interpret the {@link SearchQueryBean}
 * in different ways.
 */
public interface SolrQueryBuilder {

  String SORT_ORDER_ASC = "ASC";
  String SORT_ORDER_DESC = "DESC";

  Map<String, SolrQuery.ORDER> SORT_ORDER_MAPPING = ImmutableMap.of(
          SORT_ORDER_ASC, SolrQuery.ORDER.asc,
          SORT_ORDER_DESC, SolrQuery.ORDER.desc,

          // keys that were used in previous CMS versions and that may still appear in old content
          "ASCENDING", SolrQuery.ORDER.asc,
          "DESCENDING", SolrQuery.ORDER.desc
  );

  String ANY_VALUE = "*";
  String OPENING_BRACKET = "[";
  String CLOSING_BRACKET = "]";
  String ANY_FIELD_ANY_VALUE = ANY_VALUE + ":" + ANY_VALUE;
  String ANY_VALUE_TO = ANY_VALUE + " TO ";
  String TO_ANY_VALUE = " TO " + ANY_VALUE;
  String FIELD_SET_ANY_VALUE = OPENING_BRACKET + ANY_VALUE + " TO " + ANY_VALUE + CLOSING_BRACKET;

  // Solr operators for con- and disjunction
  String AND = " AND ";
  String OR = " OR ";

  SolrQuery buildQuery(SearchQueryBean input);

  boolean isPreview();

}
