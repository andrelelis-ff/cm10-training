package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.join;

public final class OverallPerformanceQuery extends GoogleAnalyticsQuery {

  static final String PAGE_PATH_LEVEL1 = KEY_PREFIX + "pagePathLevel1";

  static final String CONTENT_TYPE_FILTER = DIMENSION_CONTENT_TYPE + "!=CMAction";
  static final String PATH_FILTER_TEMPLATE = PAGE_PATH_LEVEL1 + "==/%s/";
  private static final String DIMENSIONS = join(new Object[]{TRACKING_DATE, PAGE_PATH_LEVEL1}, ',');

  private Content content;

  /**
   * @param content  the navigation content to limit the query to
   * @param settings the GoogleAnalyticsSettings to use (although
   * @see GoogleAnalyticsQuery#GoogleAnalyticsQuery(int, int, int)
   */
  OverallPerformanceQuery(Content content,
                          GoogleAnalyticsSettings settings) {
    super(settings.getPid(),
            settings.getTimeRange(),
            settings.getLimit());
    this.content = content;
  }

  @Override
  protected void customizeQuery(final Analytics.Data.Ga.Get query) {
    query.setDimensions(DIMENSIONS);
    query.setMetrics(METRIC_UNIQUE_PAGEVIEWS);
    query.setFilters(join(new Object[]{CONTENT_TYPE_FILTER, String.format(PATH_FILTER_TEMPLATE, getContentPath())}, ";"));
  }

  @Override
  public String toString() {
    return String.format("[query: path=%s, profileId=%s, timeRange=%s]",
            getContentPath(),
            getProfileId(),
            getTimeRange());
  }

  public String getContentPath() {
    return content == null ? null : content.getString("segment");
  }

  public Map<String, Map<String, Long>> process(List<List<String>> dataEntries, List<GaData.ColumnHeaders> columnHeaders) {
    final Map<String, Map<String, Long>> allContentsWithVisits = newHashMap();
    int indexTrackingDate = getColumnIndex(columnHeaders, OverallPerformanceQuery.TRACKING_DATE);
    int indexPagePath = getColumnIndex(columnHeaders, OverallPerformanceQuery.PAGE_PATH_LEVEL1);
    int indexPageViews = getColumnIndex(columnHeaders, OverallPerformanceQuery.METRIC_UNIQUE_PAGEVIEWS);

    String contentId = getContentId(content);

    for (List<String> list : dataEntries) {
      final String strVisitDate = list.get(indexTrackingDate);
      final String pagePath = list.get(indexPagePath);
      final Long uniquePageViews = Long.valueOf(list.get(indexPageViews));

      // the endswith / (e.g. /media/) ensures, that the complete drilldown of all subsequent content will be gathered,
      // otherwise just the single channel (/media) visit  is intended
      if (pagePath.endsWith("/") && uniquePageViews != null && uniquePageViews > 0) {
        if (allContentsWithVisits.get(contentId) != null) {
          allContentsWithVisits.get(contentId).put(strVisitDate, uniquePageViews);
        } else {
          Map<String, Long> visitsData = newHashMap();
          visitsData.put(strVisitDate, uniquePageViews);
          allContentsWithVisits.put(contentId, visitsData);
        }
      }
    }
    return allContentsWithVisits;
  }

  private String getContentId(Content content) {
    return content == null ? null : Integer.toString(IdHelper.parseContentId(content.getId()));
  }
}
