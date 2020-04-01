package com.coremedia.blueprint.analytics.settings.google;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAnalyticsSettingsProviderTest {
  @Test
  public void testBuildReportUrlEmptySettings() throws Exception {
    assertNull("no settings - no deep link url", testling.buildReportUrl(null, getClass().getName()));
  }

  @Test
  public void testBuildReportUrlOnlyAccountIdSet() throws Exception {
    when(settings.get("accountId")).thenReturn("myAccountId");
    assertEquals("http://www.google.com/analytics/web/#report/content-pages/amyAccountIdwnullpnull/%3F_r.drilldown%3Danalytics.pagePath%3Acom.coremedia.blueprint.analytics.settings.google.GoogleAnalyticsSettingsProviderTest/",
            testling.buildReportUrl(settings, getClass().getName()));
  }
  @Test
  public void testBuildReportUrl() throws Exception {
    when(settings.get("accountId")).thenReturn("myAccountId");
    when(settings.get("wpid")).thenReturn(1234);
    when(settings.get("pid")).thenReturn(4321);
    assertEquals("http://www.google.com/analytics/web/#report/content-pages/amyAccountIdw1234p4321/%3F_r.drilldown%3Danalytics.pagePath%3Acom.coremedia.blueprint.analytics.settings.google.GoogleAnalyticsSettingsProviderTest/",
            testling.buildReportUrl(settings, getClass().getName()));
  }

  @Mock
  private Map<String, Object> settings;
  private final GoogleAnalyticsSettingsProvider testling = new GoogleAnalyticsSettingsProvider();
}
