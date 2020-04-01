package com.coremedia.blueprint.analytics.elastic.google;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * Encapsulates queries to Google's Universal Data Export API</a>
 * that ask for pageviews.
 */
public final class PageViewQuery extends GoogleAnalyticsListQuery {

  private static final String DIMENSION_TITLE = KEY_PREFIX + "pageTitle";
  private static final String DIMENSION_PATH = KEY_PREFIX + "pagePath";
  private static final String DIMENSIONS = join(new Object[]{DIMENSION_TITLE, DIMENSION_PATH, DIMENSION_CONTENT_ID, DIMENSION_CONTENT_TYPE}, ',');

  /**
   * Constructor
   *
   * @param profileId  see superclass' constructor's JavaDoc
   * @param timeRange  see superclass' constructor's JavaDoc
   * @param maxResults see superclass' constructor's JavaDoc
   * @see GoogleAnalyticsQuery#GoogleAnalyticsQuery(int, int, int)
   */
  public PageViewQuery(int profileId,
                       int timeRange,
                       int maxResults) {
    super(profileId, timeRange, maxResults);
  }

  public PageViewQuery(GoogleAnalyticsSettings googleAnalyticsSettings) {
    super(googleAnalyticsSettings.getPid(),
            googleAnalyticsSettings.getTimeRange(),
            googleAnalyticsSettings.getLimit());
  }

  @Override
  protected void customizeQuery(final Analytics.Data.Ga.Get query) {
    // configure dimensions
    query.setDimensions(DIMENSIONS);

    // configure sorting
    query.setMetrics(DIMENSION_PAGEVIEWS);

    // sort descending
    query.setSort("-" + DIMENSION_PAGEVIEWS);
  }

  @Override
  public String toString() {
    return String.format("[query: profileId=%s, timeRange=%s, maxResults=%s]",
            getProfileId(),
            getTimeRange(),
            getMaxResults());
  }

  @Override
  public List<String> process(List<List<String>> dataEntries, List<GaData.ColumnHeaders> columnHeaders) {
    int index = getColumnIndex(columnHeaders, GoogleAnalyticsQuery.DIMENSION_CONTENT_ID);
    List<String> list = new ArrayList<>();
    if (index >= 0) {
      for (List<String> row : dataEntries) {
        String cell = row.get(index);
        list.add(cell);
      }
    }
    return list;
  }
}
