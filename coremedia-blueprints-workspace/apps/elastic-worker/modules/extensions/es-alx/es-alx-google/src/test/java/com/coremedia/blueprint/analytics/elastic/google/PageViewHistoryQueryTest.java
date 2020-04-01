package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.blueprint.base.analytics.elastic.util.DaysBack;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PageViewHistoryQueryTest {

  public static final int TIME_RANGE = 7;
  public static final int PROFILE_ID = 12345678;
  private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Analytics analytics;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructionIllegalProfileId() {
    new PageViewHistoryQuery(0, 10, 0);
  }

  @Test
  public void testConstructionIllegalTimeRange() {
    assertEquals(RetrievalUtil.DEFAULT_TIMERANGE, new PageViewHistoryQuery(46635897, 0, 0).getTimeRange());
  }

  @Test
  public void testGetUrlVerifySyntax() throws Exception {
    int maxResults = 100;
    final PageViewHistoryQuery defaultQuery = new PageViewHistoryQuery(PROFILE_ID, TIME_RANGE, maxResults);
    final DaysBack daysBack = new DaysBack(TIME_RANGE);
    final Analytics.Data.Ga.Get query = defaultQuery.getDataQuery(analytics);

    verify(query).setMaxResults(maxResults);
    verify(analytics.data().ga()).get("ga:" + PROFILE_ID,
            SIMPLE_DATE_FORMAT.format(daysBack.getStartDate()),
            SIMPLE_DATE_FORMAT.format(daysBack.getEndDate()),
            GoogleAnalyticsQuery.METRIC_UNIQUE_PAGEVIEWS);
    verify(query).setDimensions("ga:date,ga:dimension1,ga:dimension2");
    verify(query).setMetrics("ga:uniquePageviews");
    verify(query).setSort("-ga:dimension1");
  }

  @Test
  public void testGetProfileId() {
    int profileId = createDefaultQuery().getProfileId();
    Assert.assertEquals(PROFILE_ID, profileId);
  }

  @Test
  public void testGetTimeRange() {
    int timeRange = createDefaultQuery().getTimeRange();
    Assert.assertEquals(TIME_RANGE, timeRange);
  }

  @Test
  public void testProcess() {
    String contentId1 = "42";
    String contentId2 = "44";
    String day1 = "20130709";
    String day2 = "20130710";

    List<String> row1 = Arrays.asList(contentId1, day1, "13");
    List<String> row2 = Arrays.asList(contentId1, day2, "42");
    List<String> row3 = Arrays.asList(contentId2, day1, "1");
    List<List<String>> dataList = new ArrayList<>();
    dataList.add(row1);
    dataList.add(row2);
    dataList.add(row3);
    List<GaData.ColumnHeaders> columnHeaders = Arrays.asList(new GaData.ColumnHeaders().setName(PageViewHistoryQuery.DIMENSION_CONTENT_ID),
            new GaData.ColumnHeaders().setName(PageViewHistoryQuery.TRACKING_DATE),
            new GaData.ColumnHeaders().setName(PageViewHistoryQuery.METRIC_UNIQUE_PAGEVIEWS));

    Map<String, Map<String, Long>> processedEntries = createDefaultQuery().process(dataList, columnHeaders);

    assertEquals(2, processedEntries.size());
    assertEquals(2, processedEntries.get(contentId1).size());
    assertEquals((Object) 13L, processedEntries.get(contentId1).get(day1));
    assertEquals((Object) 42L, processedEntries.get(contentId1).get(day2));
    assertEquals(1, processedEntries.get(contentId2).size());
    assertEquals((Object) 1L, processedEntries.get(contentId2).get(day1));
    assertEquals(null, processedEntries.get(contentId2).get(day2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInvalidQuery() {
    ImmutableMap<String, Object> settings = ImmutableMap.of();
    PageViewHistoryQuery query = new PageViewHistoryQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings).getPid(), 10, 0);
    fail(query + " should not exist");
  }

  @Test
  public void testCreateQuery() {
    int profileId = 1234;
    int timeRange = 20;

    ImmutableMap<String, Object> settings = ImmutableMap.<String, Object>of(
            GoogleAnalyticsQuery.KEY_PID, profileId
    );
    PageViewHistoryQuery query = new PageViewHistoryQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings).getPid(), timeRange, 0);
    assertEquals(profileId, query.getProfileId());
    assertEquals(PageViewHistoryQuery.DEFAULT_MAX_RESULTS, query.getMaxResults());
    assertEquals(timeRange, query.getTimeRange());
    assertTrue(query.toString().contains(String.valueOf(profileId)));
  }

  private PageViewHistoryQuery createDefaultQuery() {
    ImmutableMap<String, Object> settings = ImmutableMap.<String, Object>of(
            RetrievalUtil.KEY_LIMIT, 20,
            RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE, TIME_RANGE,
            GoogleAnalyticsQuery.KEY_PID, PROFILE_ID
    );
    return new PageViewHistoryQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings));
  }
}
