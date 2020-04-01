package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.cap.content.Content;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OverallPerformanceQueryTest {
  private static final int PID = 1234;
  private static final int TIME_RANGE = 30;
  private static final String CONTENT_ID = "1234";
  private static final String SEGMENT = "segment";
  private OverallPerformanceQuery overallPerformanceQuery;

  @Mock
  private Content content;

  @Mock
  private GoogleAnalyticsSettings googleAnalyticsSettings;

  @Mock
  private Analytics.Data.Ga.Get analyticsQuery;


  @Before
  public void setup() {
    when(googleAnalyticsSettings.getTimeRange()).thenReturn(TIME_RANGE);
    when(googleAnalyticsSettings.getPid()).thenReturn(PID);
    when(content.getId()).thenReturn(CONTENT_ID);
    when(content.getString(SEGMENT)).thenReturn(SEGMENT);
    overallPerformanceQuery = new OverallPerformanceQuery(content, googleAnalyticsSettings);
  }

  @Test
  public void process() {
    String pagePath1 = "path1/";
    String pagePath2 = "path2/";
    String day1 = "20130709";
    String day2 = "20130710";
    String day3 = "20130711";

    List<String> row1 = Arrays.asList(pagePath1, day1, "13");
    List<String> row2 = Arrays.asList(pagePath1, day2, "42");
    List<String> row3 = Arrays.asList(pagePath2, day3, "1");

    List<List<String>> dataList = new ArrayList<>();
    dataList.add(row1);
    dataList.add(row2);
    dataList.add(row3);

    List<GaData.ColumnHeaders> columnHeaders = Arrays.asList(new GaData.ColumnHeaders().setName(OverallPerformanceQuery.PAGE_PATH_LEVEL1),
            new GaData.ColumnHeaders().setName(OverallPerformanceQuery.TRACKING_DATE),
            new GaData.ColumnHeaders().setName(OverallPerformanceQuery.METRIC_UNIQUE_PAGEVIEWS));

    Map<String, Map<String, Long>> processedEntries = overallPerformanceQuery.process(dataList, columnHeaders);

    assertEquals(1, processedEntries.size());
    assertEquals(3, processedEntries.get(CONTENT_ID).entrySet().size());
    assertEquals((Object) 13l, processedEntries.get(CONTENT_ID).get(day1));
    assertEquals((Object) 42l, processedEntries.get(CONTENT_ID).get(day2));
    assertEquals((Object) 1l, processedEntries.get(CONTENT_ID).get(day3));
  }

  @Test
  public void testToString() {
    assertNotNull(overallPerformanceQuery.toString());
  }

  @Test
  public void getContentPath() {

    String contentPath = overallPerformanceQuery.getContentPath();
    assertEquals(SEGMENT, contentPath);
  }

  @Test
  public void customizeQuery() {
    overallPerformanceQuery.customizeQuery(analyticsQuery);
    verify(analyticsQuery).setDimensions(join(new Object[]{GoogleAnalyticsQuery.TRACKING_DATE, OverallPerformanceQuery.PAGE_PATH_LEVEL1}, ','));
    verify(analyticsQuery).setMetrics(GoogleAnalyticsQuery.METRIC_UNIQUE_PAGEVIEWS);
    verify(analyticsQuery).setFilters(join(new Object[]{OverallPerformanceQuery.CONTENT_TYPE_FILTER,
            String.format(OverallPerformanceQuery.PATH_FILTER_TEMPLATE, SEGMENT)}, ";"));
  }
}