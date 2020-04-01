package com.coremedia.blueprint.analytics.google;


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

import static com.coremedia.blueprint.analytics.google.GoogleAnalytics.GOOGLE_ANALYTICS_SERVICE_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAnalyticsTest {
  private final static String CONTENT_ID = "1234";
  private final static Integer NAV_ID = 5678;
  private final static String CONTENT_TYPE = "CM_Image";

  private GoogleAnalytics googleAnalytics;

  @Mock
  private Page page;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Object content;

  @Mock
  private Navigation navigation;

  @Mock
  private CMNavigation cmNavigation;

  private Map<String, Object> settings;

  @Before
  public void setup() {
    googleAnalytics = new GoogleAnalytics(page, settingsService);
    when(page.getContentId()).thenReturn(CONTENT_ID);
    when(page.getContent()).thenReturn(content);
    when(page.getContentType()).thenReturn(CONTENT_TYPE);
    when(page.getNavigation()).thenReturn(navigation);
    doReturn(Arrays.asList(cmNavigation)).when(navigation).getNavigationPathList();
    when(cmNavigation.getContentId()).thenReturn(NAV_ID);
    settings = new HashMap<>();
    settings.put("webPropertyId", "UA-12345-678");
    settings.put("domainName", "auto");
    when(settingsService.settingAsMap(GOOGLE_ANALYTICS_SERVICE_KEY, String.class, Object.class, page)).thenReturn(settings);
  }

  @Test
  public void getContent() {
    assertEquals(content, googleAnalytics.getContent());
  }

  @Test
  public void getContentType() {
    assertEquals(CONTENT_TYPE, googleAnalytics.getContentType());
  }

  @Test
  public void getContentId() {
    assertEquals(CONTENT_ID, googleAnalytics.getContentId());
  }

  @Test
  public void getNavigationPathIds() {
    assertEquals(String.valueOf(NAV_ID), googleAnalytics.getNavigationPathIds()[0]);
  }

  @Test
  public void isConfigValid() {
    assertTrue(googleAnalytics.isConfigValid());
  }

  @Test
  public void getDomainName() {
    assertEquals("auto", googleAnalytics.getDomainName());
  }

  @Test
  public void testIsDisabledAdFeaturesPlugin_settingNotSet_returnsFalseAsDefault() {
    settings.put(GoogleAnalytics.DISABLE_AD_FEATURES_PLUGIN,  null);
    boolean isFeatureDisabled = googleAnalytics.isAdvertisingFeaturesPluginDisabled();
    assertFalse(isFeatureDisabled);
  }

  @Test
  public void testIsDisabledAdFeaturesPlugin_settingReturnsTrue_returnsTrue() {
    settings.put(GoogleAnalytics.DISABLE_AD_FEATURES_PLUGIN,  true);
    boolean isFeatureDisabled = googleAnalytics.isAdvertisingFeaturesPluginDisabled();
    assertTrue(isFeatureDisabled);
  }
}