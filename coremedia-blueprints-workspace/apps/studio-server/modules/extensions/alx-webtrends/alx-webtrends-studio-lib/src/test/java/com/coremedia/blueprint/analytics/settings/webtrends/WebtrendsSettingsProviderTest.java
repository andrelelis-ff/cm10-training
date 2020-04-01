package com.coremedia.blueprint.analytics.settings.webtrends;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebtrendsSettingsProviderTest {
  @Test
  public void testBuildReportUrlEmptySettings() throws Exception {
    assertNull("no settings - no deep link url", webtrendsSettingsProvider.buildReportUrl(null, getClass().getName()));
  }

  @Test
  public void testBuildReportUrlOnlyAccountIdSet() throws Exception {
    when(settings.get("spaceId")).thenReturn("mySpaceId");
    assertEquals("https://analytics.webtrends.com/analytics/spaces/mySpaceId/profiles/null/reports/null/urldashboard?url=com.coremedia.blueprint.analytics.settings.webtrends.WebtrendsSettingsProviderTest",
            webtrendsSettingsProvider.buildReportUrl(settings, getClass().getName()));
  }
  @Test
  public void testBuildReportUrl() throws Exception {
    when(settings.get("spaceId")).thenReturn("mySpaceId");
    when(settings.get("reportId")).thenReturn(1234);
    when(settings.get("profileId")).thenReturn(4321);

    assertEquals("https://analytics.webtrends.com/analytics/spaces/mySpaceId/profiles/4321/reports/1234/urldashboard?url=com.coremedia.blueprint.analytics.settings.webtrends.WebtrendsSettingsProviderTest",
            webtrendsSettingsProvider.buildReportUrl(settings, getClass().getName()));
  }

  @Mock
  private Map<String, Object> settings;
  private final WebtrendsSettingsProvider webtrendsSettingsProvider = new WebtrendsSettingsProvider();
}
