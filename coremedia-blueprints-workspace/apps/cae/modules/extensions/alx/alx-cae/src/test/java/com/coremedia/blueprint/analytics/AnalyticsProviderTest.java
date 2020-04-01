package com.coremedia.blueprint.analytics;


import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticsProviderTest {

  public static final String GOOGLE_ANALYTICS = "googleAnalytics";
  private final static String CONTENT_ID = "1234";
  private final static Integer NAV_ID = 5678;
  private final static String CONTENT_TYPE = "CM_Image";
  public static final String SEGMENT = "Segment";

  private AnalyticsProvider analyticsProvider;

  @Mock
  private Page page;

  @Mock
  private Object content;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Navigation navigation;

  @Mock
  private CMNavigation cmNavigation;

  @Before
  public void setup() {
    analyticsProvider = new AnalyticsProvider(GOOGLE_ANALYTICS, page, settingsService);
    when(page.getContentId()).thenReturn(CONTENT_ID);
    when(page.getContent()).thenReturn(content);
    when(page.getContentType()).thenReturn(CONTENT_TYPE);
    when(page.getNavigation()).thenReturn(navigation);
    doReturn(Arrays.asList(cmNavigation)).when(navigation).getNavigationPathList();
    when(cmNavigation.getContentId()).thenReturn(NAV_ID);
    when(cmNavigation.getSegment()).thenReturn(SEGMENT);
    Map<String, Object> map = new HashMap<>();
    map.put("webPropertyId", "UA-12345-678");
    map.put("domainName", "auto");
    when(settingsService.settingAsMap(GOOGLE_ANALYTICS, String.class, Object.class, page)).thenReturn(map);
  }

  @Test
  public void getServiceKey() {
    assertEquals(GOOGLE_ANALYTICS, analyticsProvider.getServiceKey());
  }

  @Test
  public void getAggregator() {
    assertEquals(page, analyticsProvider.getAggregator());
  }

  @Test
  public void getNavigationPathIds() {
    assertEquals(String.valueOf(NAV_ID), analyticsProvider.getNavigationPathIds()[0]);
  }

  @Test
  public void getNavigationPathSegments() {
    assertEquals(SEGMENT, analyticsProvider.getNavigationPathSegments()[0]);
  }

  @Test
  public void getContentId() {
    assertEquals(CONTENT_ID, analyticsProvider.getContentId());
  }

  @Test
  public void getContentType() {
    assertEquals(CONTENT_TYPE, analyticsProvider.getContentType());
  }

  @Test
  public void getContent() {
    assertEquals(content, analyticsProvider.getContent());
  }

  @Test
  public void isEnabled() {
    assertTrue(analyticsProvider.isEnabled());
  }

  @Test
  public void isDisabled() {
    Map<String, Object> map = new HashMap<>();
    map.put("disabled", true);
    when(settingsService.settingAsMap(GOOGLE_ANALYTICS, String.class, Object.class, page)).thenReturn(map);
    assertFalse(analyticsProvider.isEnabled());
  }

}
