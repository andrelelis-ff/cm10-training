package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.blueprint.base.analytics.elastic.util.DaysBack;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Base class for queries to Google's Data Export API.
 * <p/>
 * Gathers common parameters, that shape a query to Google's Data Export API
 */
public abstract class GoogleAnalyticsQuery {

  // keys used in queries are prefixed by 'ga:'
  static final String KEY_PREFIX = "ga:";

  // Slots for Google Analytics' "custom vars" (dimension1 ... n)
  static final String CUSTOMVAR_CONTENT_ID = "dimension1";
  static final String CUSTOMVAR_CONTENT_TYPE = "dimension2";
  static final String DIMENSION_CONTENT_ID = KEY_PREFIX + CUSTOMVAR_CONTENT_ID;
  static final String DIMENSION_CONTENT_TYPE = KEY_PREFIX + CUSTOMVAR_CONTENT_TYPE;
  static final String DIMENSION_PAGEVIEWS = KEY_PREFIX + "pageviews";
  static final String METRIC_UNIQUE_PAGEVIEWS = KEY_PREFIX + "uniquePageviews";
  static final String TRACKING_DATE = KEY_PREFIX + "date";

  // Maximum number of rows to be included in a response allowed by Google. In combination with start-index this can be
  // used to retrieve a subset of elements, or alone to restrict the number of returned elements, starting with the first.
  // The default is set to 10000 if max-results is not supplied.
  static final int DEFAULT_MAX_RESULTS = 10000;

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAnalyticsQuery.class);

  static final String KEY_PID = "pid";

  private final int profileId;
  private final int timeRange;
  private final int maxResults;

  /**
   * @param profileId  ID of the Google Analytics profile that shall track the visit
   * @param timeRange  Days from now the query will extend to (e.g. a value of 14 means
   *                   "look back two weeks") Maps to the corresponding document type
   *                   property of 'CMALXBaseList'
   * @param maxResults The number of results this query will be limited to. Maps to the 'CMALXBaseList' document's settings
   *                   property 'limit'. If the property is not set, it defaults to {@link com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#DEFAULT_LIMIT}
   */
  protected GoogleAnalyticsQuery(int profileId, int timeRange, int maxResults) {
    assertGreaterThanZero(profileId, "profileId");

    this.profileId = profileId;
    this.timeRange = timeRange > 0 ? timeRange : RetrievalUtil.DEFAULT_TIMERANGE;
    this.maxResults = maxResults > 0 ? maxResults : DEFAULT_MAX_RESULTS;
  }

  private static void assertGreaterThanZero(int i, String paramName) {
    if (i <= 0) {
      throw new IllegalArgumentException("Parameter '" + paramName + "' must not be negative or zero.");
    }
  }

  /**
   * Builds the URL that represents the call to Google's Data Export API.
   * <p/>
   * This method creates a query and invokes the following method before returning its URL:
   * <p/>
   *
   * @return the URL that represents the call to Google's Data Export API
   */
  public final Analytics.Data.Ga.Get getDataQuery(Analytics analytics) throws IOException {
    final DaysBack daysBack = new DaysBack(timeRange);
    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Analytics.Data.Ga.Get get = analytics.data().ga()
            .get(KEY_PREFIX + profileId,
                    dateFormat.format(daysBack.getStartDate()),
                    dateFormat.format(daysBack.getEndDate()),
                    METRIC_UNIQUE_PAGEVIEWS);

    // limit results
    // (no matter when set, the corresponding URL parameter
    // will be always put in front of the parameter list)
    if (maxResults > 0) {
      get.setMaxResults(maxResults);
    }

    customizeQuery(get);

    return get;
  }

  /**
   * Customize the given query
   *
   * @param query the query to customize
   * @see com.coremedia.blueprint.analytics.elastic.google.GoogleAnalyticsQuery#getDataQuery(com.google.api.services.analytics.Analytics)
   */
  protected void customizeQuery(final Analytics.Data.Ga.Get query) {
    // nothing to do
  }

  protected int getColumnIndex(List<GaData.ColumnHeaders> columnHeaders, String columnName) {
    int columnIndex = -1;
    for (int index = 0; index < columnHeaders.size(); index++) {
      if (columnName.equals(columnHeaders.get(index).getName())) {
        columnIndex = index;
      }
    }
    return columnIndex;
  }

  /**
   * @return see superclass' constructor's JavaDoc
   * @see GoogleAnalyticsQuery#GoogleAnalyticsQuery(int, int, int)
   */
  public final int getMaxResults() {
    return maxResults;
  }

  /**
   * @return see superclass' constructor's JavaDoc
   * @see GoogleAnalyticsQuery#GoogleAnalyticsQuery(int, int, int)
   */
  public int getProfileId() {
    return profileId;
  }

  /**
   * @return see superclass' constructor's JavaDoc
   * @see GoogleAnalyticsQuery#GoogleAnalyticsQuery(int, int, int)
   */
  public int getTimeRange() {
    return timeRange;
  }

  /**
   * Check whether the given settings can be used to retrieve data. GoogleAnalyticsQuery
   * instances may only be created if this method returns <code>true</code>.
   *
   * @param settings the effective settings used for retrieval
   * @return true if and only if all required properties are available
   */
  static boolean canCreateQuery(GoogleAnalyticsSettings settings) {
    final int profileId = settings.getPid();
    boolean ok = true;
    if (profileId < 1) {
      LOGGER.info("google analytics profile id must be greater than zero but is : {}, disabling retrieval", profileId);
      ok = false;
    }

    return ok;
  }
}
