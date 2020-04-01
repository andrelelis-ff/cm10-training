package com.coremedia.blueprint.analytics.elastic.google;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * The @PageViewHistoryQuery encapsulates queries to
 * Google's number of different unique pages within a session from given custom variables.
 */
public final class PageViewHistoryQuery extends GoogleAnalyticsQuery {

  private static final String SORT_CRITERION = "-" + DIMENSION_CONTENT_ID;
  private static final String DIMENSIONS = join(new Object[]{TRACKING_DATE, DIMENSION_CONTENT_ID, DIMENSION_CONTENT_TYPE}, ',');

  PageViewHistoryQuery(int profileId,
                       int timeRange,
                       int limit) {
    super(profileId, timeRange, limit);
  }

  public PageViewHistoryQuery(GoogleAnalyticsSettings googleAnalyticsSettings) {
    this(googleAnalyticsSettings.getPid(),
            googleAnalyticsSettings.getTimeRange(),
            googleAnalyticsSettings.getLimit());
  }

  @Override
  protected void customizeQuery(final Analytics.Data.Ga.Get query) {
    query.setDimensions(DIMENSIONS);

    query.setMetrics(METRIC_UNIQUE_PAGEVIEWS);
    query.setSort(SORT_CRITERION);
  }

  @Override
  public String toString() {
    return String.format("[query: profileId=%s, timeRange=%s]",
            getProfileId(),
            getTimeRange());
  }

  /**
   * Process the results of the Google's Core Reporting API data response and returns a map of content with their unique page views and related
   * visit date.
   */
  public Map<String, Map<String, Long>> process(List<List<String>> dataEntries, List<GaData.ColumnHeaders> columnHeaders) {

    final Map<String, Map<String, Long>> allContentsWithVisits = new TreeMap<>();
    int contentIdIndex = getColumnIndex(columnHeaders, DIMENSION_CONTENT_ID);
    int trackingDateIndex = getColumnIndex(columnHeaders, TRACKING_DATE);
    int uniquePageViewsIndex = getColumnIndex(columnHeaders, METRIC_UNIQUE_PAGEVIEWS);

    for (List<String> list : dataEntries) {
      String contentId = list.get(contentIdIndex);
      String strVisitDate = list.get(trackingDateIndex);
      Long uniquePageViews = Long.valueOf(list.get(uniquePageViewsIndex));

      if (uniquePageViews != null && uniquePageViews > 0) {
        if (allContentsWithVisits.get(contentId) != null) {
          allContentsWithVisits.get(contentId).put(strVisitDate, uniquePageViews);
        } else {
          Map<String, Long> visitsData = new TreeMap<>();
          visitsData.put(strVisitDate, uniquePageViews);
          allContentsWithVisits.put(contentId, visitsData);
        }
      }
    }
    return allContentsWithVisits;
  }

}
