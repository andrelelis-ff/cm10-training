package com.coremedia.blueprint.analytics.elastic.google;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates queries to Google's Data Export API
 * that ask for events.
 */
public final class EventQuery extends GoogleAnalyticsListQuery {

  static final String DIMENSION_CATEGORY = KEY_PREFIX + "eventCategory";
  static final String DIMENSION_ACTION = KEY_PREFIX + "eventAction";
  static final String DIMENSION_LABEL = KEY_PREFIX + "eventLabel";
  static final String DIMENSIONS = DIMENSION_CATEGORY + "," + DIMENSION_ACTION + "," + DIMENSION_LABEL;
  private static final String METRIC_TOTAL_EVENTS = KEY_PREFIX + "totalEvents";
  private static final String FILTER_PATTERN = DIMENSION_CATEGORY + "==%s;" + DIMENSION_ACTION + "==%s";
  private static final String SORT_CRITERION = "-" + METRIC_TOTAL_EVENTS;

  // the Event Category
  private String category;

  // the event action
  private String action;

  /**
   * Constructor
   *
   * @param profileId  see superclass' constructor's JavaDoc
   * @param timeRange  see superclass' constructor's JavaDoc
   * @param maxResults see superclass' constructor's JavaDoc
   * @param category   the event category
   * @param action     the actions to fetch
   */
  EventQuery(final int profileId,
             final int timeRange,
             final int maxResults,
             final String category,
             final String action) {
    super(profileId, timeRange, maxResults);

    if (category == null) {
      throw new IllegalArgumentException("Parameter 'category' must be set.");
    }
    if (action == null) {
      throw new IllegalArgumentException("Parameter 'action' must be set.");
    }

    this.category = category;
    this.action = action;
  }

  public EventQuery(GoogleAnalyticsSettings googleAnalyticsSettings) {
    this(
            googleAnalyticsSettings.getPid(),
            googleAnalyticsSettings.getTimeRange(),
            googleAnalyticsSettings.getLimit(),
            googleAnalyticsSettings.getCategory(),
            googleAnalyticsSettings.getAction());
  }

  @Override
  protected void customizeQuery(final Analytics.Data.Ga.Get query) {
    // configure dimensions
    query.setDimensions(DIMENSIONS);

    // configure event filter
    query.setFilters(String.format(FILTER_PATTERN, category, action));

    // configure sorting
    query.setMetrics(METRIC_TOTAL_EVENTS);

    // sort descending
    query.setSort(SORT_CRITERION);
  }

  @Override
  public String toString() {
    return String.format("[query: profileId=%s, timeRange=%s, maxResults=%s, category=%s, action=%s]",
            getProfileId(),
            getTimeRange(),
            getMaxResults(),
            category,
            action);
  }

  @Override
  public List<String> process(List<List<String>> dataEntries, List<GaData.ColumnHeaders> columnHeaders) {
    int index = getColumnIndex(columnHeaders, EventQuery.DIMENSION_LABEL);
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
