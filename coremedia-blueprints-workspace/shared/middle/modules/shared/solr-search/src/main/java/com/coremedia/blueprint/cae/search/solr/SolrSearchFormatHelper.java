package com.coremedia.blueprint.cae.search.solr;

import org.apache.solr.common.params.MapSolrParams;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Provides access to search engine specific String formatting
 */
public final class SolrSearchFormatHelper {

  private SolrSearchFormatHelper() {
  }

  public static String fromPastToValue(String value) {
    StringBuilder builder = new StringBuilder(SolrQueryBuilder.OPENING_BRACKET);
    builder.append(SolrQueryBuilder.ANY_VALUE_TO);
    builder.append(value);
    builder.append(SolrQueryBuilder.CLOSING_BRACKET);
    return builder.toString();
  }

  public static String fromValueIntoFuture(String value) {
    StringBuilder builder = new StringBuilder(SolrQueryBuilder.OPENING_BRACKET);
    builder.append(value);
    builder.append(SolrQueryBuilder.TO_ANY_VALUE);
    builder.append(SolrQueryBuilder.CLOSING_BRACKET);
    return builder.toString();
  }

  public static String calendarToString(Calendar calendar) {
    return calendar.getTime().toInstant().toString();
  }

  public static String dateToString(Date date) {
    return date.toInstant().toString();
  }

  /**
   * Formats the given Map as Solr local params query fragment.
   */
  public static String formatLocalParameters(Map<String, String> map) {
    return map.isEmpty() ? "" : new MapSolrParams(map).toLocalParamsString();
  }

}
