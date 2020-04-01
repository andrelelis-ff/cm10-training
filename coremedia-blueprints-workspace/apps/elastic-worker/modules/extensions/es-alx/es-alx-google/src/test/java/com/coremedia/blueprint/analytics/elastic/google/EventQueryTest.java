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
public class EventQueryTest {
  static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final int PROFILE_ID = 46635897;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Analytics analytics;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructionIllegalCategory() {
    //noinspection NullableProblems
    new EventQuery(123456, 10, 10, null, "foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructionIllegalAction() {
    //noinspection NullableProblems
    new EventQuery( 123456, 10, 10, "foo", null);
  }

  @Test
  public void testVerifyQuerySyntaxTotalEvents() throws Exception {
    int timeRange = 10;
    int maxResults = 100;
    EventQuery defaultQuery = new EventQuery(PROFILE_ID, timeRange, maxResults, "TestCategory", "TestAction");
    final DaysBack daysBack = new DaysBack(timeRange);
    final Analytics.Data.Ga.Get query = defaultQuery.getDataQuery(analytics);

    verify(query).setMaxResults(maxResults);
    verify(analytics.data().ga()).get("ga:" + PROFILE_ID,
            SIMPLE_DATE_FORMAT.format(daysBack.getStartDate()),
            SIMPLE_DATE_FORMAT.format(daysBack.getEndDate()),
            GoogleAnalyticsQuery.METRIC_UNIQUE_PAGEVIEWS);
    verify(query).setDimensions("ga:eventCategory,ga:eventAction,ga:eventLabel");
    verify(query).setMetrics("ga:totalEvents");
    verify(query).setFilters("ga:eventCategory==TestCategory;ga:eventAction==TestAction");
    verify(query).setSort("-ga:totalEvents");
  }


  @Test
  public void testCreateQuery() {
    EventQuery query = createDefaultQuery();

    assertEquals(30, query.getTimeRange());
    assertEquals(20, query.getMaxResults());
    assertEquals(1234, query.getProfileId());

    assertTrue(query.toString().contains("1234"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInvalidQuery() {
    ImmutableMap<String, Object> settings = ImmutableMap.<String, Object>of(
            RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE, 30
    );
    EventQuery query = new EventQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings));
    fail(query + " should not exist");
  }

  @Test
  public void process() {
    EventQuery query = createDefaultQuery();
    List<String> row1 = Arrays.asList("xyz", "label1");
    List<String> row2 = Arrays.asList("abc", "label2");
    List<List<String>> dataList = new ArrayList<>();
    dataList.add(row1);
    dataList.add(row2);
    List<GaData.ColumnHeaders> columnHeaders = Arrays.asList(new GaData.ColumnHeaders().setName("something"), new GaData.ColumnHeaders().setName(EventQuery.DIMENSION_LABEL));

    List<String> processedList = query.process(dataList, columnHeaders);
    assertEquals("label1", processedList.get(0));
    assertEquals("label2", processedList.get(1));
  }

  private EventQuery createDefaultQuery() {
    ImmutableMap<String, Object> settings = ImmutableMap.<String, Object>of(
            RetrievalUtil.KEY_LIMIT, 20,
            RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE, 30,
            RetrievalUtil.DOCUMENT_PROPERTY_ACTION, "myAction",
            RetrievalUtil.DOCUMENT_PROPERTY_CATEGORY, "myCategory",
            GoogleAnalyticsQuery.KEY_PID, 1234
    );
    return new EventQuery(SettingsUtil.createProxy(GoogleAnalyticsSettings.class, settings));
  }
}
