package com.coremedia.blueprint.analytics.elastic.retrieval;

import com.coremedia.cap.content.Content;

import java.util.List;
import java.util.Map;

/**
 * An analytics service provider is supposed to fetch data from 3rd party analytics services and to
 * use elastic-core to temporarily store that data for further processing.
 * Instances should be managed beans which are then auto-wired and included in data retrieval tasks.
 *
 * @see com.coremedia.blueprint.analytics.elastic.tasks.FetchPageViewHistoryTask
 * @see com.coremedia.blueprint.analytics.elastic.tasks.FetchReportsTask
 */
public interface AnalyticsServiceProvider {

  /**
   * Return the service key of this analytics service provider.
   *
   * @return the service key
   */
  String getServiceKey();

  /**
   * Fetch data as configured by the given cmalxBaseList (and its {@link #getServiceKey()} settings.
   * The returned list must be ordered according to the recorded hit count.
   *
   * @param cmalxBaseListContent the list to provide the report configuration
   * @return a list of report data items (of maximum size {@link com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#getLimit(Map<String, Object)}
   * @throws Exception may throw various kinds of exceptions
   */
  List<String> fetchDataFor(Content cmalxBaseListContent, Map<String, Object> effectiveSettings) throws Exception;

  /**
   * Returns a content page views report for the given settings and time range.
   *
   * @param content the root navigation content
   * @param settings a map of the desired 3d party analytics settings to provide the report configuration.
   * @return a map of each contents visit date and page views number.
   * @throws Exception may throw various kinds of exceptions
   */
  Map<String, Map<String, Long>> fetchPageViews(Content content, Map<String, Object> settings) throws Exception;

  /**
   * Compute the effective retrieval settings for TOP N lists
   * @param cmalxBaseList the current cmalxBaseList content to fetch data for
   * @param rootNavigation the root context which shall be used for retrieving the settings
   * @return the filtered and extended settings that are effectively used for data retrieval and post processing of the
   * retrieved data, hence the configuration of the Top N list to be displayed
   */
  Map<String, Object> computeEffectiveRetrievalSettings(Content cmalxBaseList, Content rootNavigation);
}
