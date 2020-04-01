package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.blueprint.base.analytics.elastic.util.DaysBack;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PageViewQueryTest {
  static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final int PROFILE_ID = 46635897;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Analytics analytics;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructionIllegalProfileId() {
    //noinspection NullableProblems
    new PageViewQuery(0, 10, 10);
  }

  @Test
  public void testDefaultTimeRange() {
    assertEquals(RetrievalUtil.DEFAULT_TIMERANGE, new PageViewQuery(PROFILE_ID, 0, 10).getTimeRange());
  }

  @Test
  public void testDefaultMaxResults() {
    assertEquals(GoogleAnalyticsQuery.DEFAULT_MAX_RESULTS, new PageViewQuery(PROFILE_ID, 10, 0).getMaxResults());
  }

  @Test
  public void testGetDataQuery() throws Exception {
    int timeRange = 10;
    int maxResults = 100;
    final PageViewQuery defaultQuery = new PageViewQuery(PROFILE_ID, timeRange, maxResults);
    final DaysBack daysBack = new DaysBack(timeRange);
    final Analytics.Data.Ga.Get query = defaultQuery.getDataQuery(analytics);

    verify(query).setMaxResults(maxResults);
    verify(analytics.data().ga()).get("ga:" + PROFILE_ID,
            SIMPLE_DATE_FORMAT.format(daysBack.getStartDate()),
            SIMPLE_DATE_FORMAT.format(daysBack.getEndDate()),
            GoogleAnalyticsQuery.METRIC_UNIQUE_PAGEVIEWS);
    verify(query).setDimensions("ga:pageTitle,ga:pagePath,ga:dimension1,ga:dimension2");
    verify(query).setMetrics("ga:pageviews");
    verify(query).setSort("-ga:pageviews");
  }

  @Test
  public void testCreateQuery() {
    PageViewQuery query = createDefaultQuery();

    assertEquals(30, query.getTimeRange());
    assertEquals(20, query.getMaxResults());
    assertEquals(1234, query.getProfileId());

    assertTrue(query.toString().contains("1234"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInvalidQuery() {
    ImmutableMap<String, Object> settings = ImmutableMap.<String, Object>of(
            RetrievalUtil.KEY_LIMIT, 20,
            RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE, 30
    );

    final PageViewQuery query = new PageViewQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings));
    fail(query + " should be null");
  }

  @Test
  public void process() {
    PageViewQuery query = createDefaultQuery();
    List<String> row1 = Arrays.asList("xyz", "contentId1");
    List<String> row2 = Arrays.asList("abc", "contentId2");
    List<List<String>> dataList = new ArrayList<>();
    dataList.add(row1);
    dataList.add(row2);
    List<GaData.ColumnHeaders> columnHeaders = Arrays.asList(new GaData.ColumnHeaders().setName("something"), new GaData.ColumnHeaders().setName(GoogleAnalyticsListQuery.DIMENSION_CONTENT_ID));

    List<String> processedList = query.process(dataList, columnHeaders);
    assertEquals("contentId1", processedList.get(0));
    assertEquals("contentId2", processedList.get(1));
  }

  private PageViewQuery createDefaultQuery() {
    ImmutableMap<String, Object> settings = ImmutableMap.<String, Object>of(
            RetrievalUtil.KEY_LIMIT, 20,
            RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE, 30,
            GoogleAnalyticsQuery.KEY_PID, 1234
    );
    return new PageViewQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings));
  }}
