package com.coremedia.blueprint.analytics.elastic.webtrends;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCTYPE_EVENTLIST;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCTYPE_PAGELIST;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE;
import static com.coremedia.blueprint.analytics.elastic.webtrends.ElasticWebtrendsServiceProvider.WEBTRENDS_SERVICE_KEY;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ElasticWebtrendsServiceProviderTest {

  private Map<String,Object> serviceSettings = new HashMap<>();

  private static final int TIME_RANGE = 30;
  private static final String USER_NAME = "myUserName";
  private static final String ACCOUNT_NAME = "myAccount";
  private static final String PROFILE_ID = "myProfileId";
  private static final String REPORT_ID = "myReportId";
  private static final String PASSWORD = "myPassword";

  @InjectMocks
  private final ElasticWebtrendsServiceProvider provider = new ElasticWebtrendsServiceProvider();

  @Mock
  private Content cmalxBaseList;

  @Mock
  private Content cmAlxPageList;

  @Mock
  private Content cmAlxEventList;

  @Mock
  private SettingsService settingsService;

  @Before
  public void setUp() throws Exception {
    serviceSettings.clear();
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(Content.class), any(Content.class))).thenReturn(serviceSettings);

    ContentType pageType = mock(ContentType.class);
    ContentType eventType = mock(ContentType.class);
    ContentType baseType = mock(ContentType.class);

    when(cmAlxPageList.getType()).thenReturn(pageType);
    when(cmAlxEventList.getType()).thenReturn(eventType);
    when(cmalxBaseList.getType()).thenReturn(baseType);

    when(pageType.isSubtypeOf(DOCTYPE_PAGELIST)).thenReturn(true);
    when(eventType.isSubtypeOf(DOCTYPE_EVENTLIST)).thenReturn(true);
  }

  @Test
  public void testNoSettings() throws Exception {
    when(settingsService.setting(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(null);
    assertEquals(emptyMap(), provider.computeEffectiveRetrievalSettings(cmalxBaseList, mock(Content.class)));
    assertEquals(Collections.emptyList(), provider.fetchDataFor(cmalxBaseList, serviceSettings));
  }

  @Test
  public void testInvalidSettings() throws Exception {
    serviceSettings.put("accountName", "account");
    serviceSettings.put("userName", "user");
    serviceSettings.put("profileId", "profile");
    serviceSettings.put("reportId", "report");
    serviceSettings.put("password", "pass");
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(serviceSettings);
    ImmutableMap<Object, Object> effectiveSettings = ImmutableMap.builder()
            .put("accountName", "account")
            .put("userName", "user")
            .put("profileId", "profile")
            .put("reportId", "report")
            .put("password", "pass")
            .put("limit", 1000)
            .put("documentType", "")
            .put("timeRange", 30)
            .put("interval", 1440)
            .put("sortByMeasure", "Hits")
            .put("category", "")
            .put("maxLength", 10)
            .put("action", "")
            .put("baseChannel", emptyList()).build();
    assertEquals(effectiveSettings, provider.computeEffectiveRetrievalSettings(cmalxBaseList, mock(Content.class)));
    assertEquals(Collections.emptyList(), provider.fetchDataFor(cmalxBaseList, serviceSettings));
  }

  @Test
  public void testNoCredentials() throws Exception {
    serviceSettings.put("profileId", "profile");
    serviceSettings.put("reportId", "report");
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(serviceSettings);

    ImmutableMap<Object, Object> effectiveSettings = ImmutableMap.builder()
            .put("accountName", "")
            .put("userName", "")
            .put("profileId", "profile")
            .put("reportId", "report")
            .put("password", "")
            .put("limit", 1000)
            .put("documentType", "")
            .put("timeRange", 30)
            .put("interval", 1440)
            .put("sortByMeasure", "Hits")
            .put("category", "")
            .put("maxLength", 10)
            .put("action", "")
            .put("baseChannel", emptyList()).build();
    assertEquals(effectiveSettings, provider.computeEffectiveRetrievalSettings(cmalxBaseList, mock(Content.class)));
    assertEquals(Collections.emptyList(), provider.fetchDataFor(cmalxBaseList, serviceSettings));
  }

  @Test
  public void computeEffectiveSettingsWithEmptySettings() {
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(new HashMap<String, Object>());

    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    // expect empty settings when called with empty map
    assertEquals("maps shoudn't differ: " + Maps.difference(Collections.EMPTY_MAP, effectiveSettings),
            Collections.EMPTY_MAP,
            effectiveSettings);
  }

  @Test
  public void computeEffectiveSettingsWithUnimportantSettings() {
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(ImmutableMap.<String, Object>of("unused", "does not matter"));

    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    // expect all the retrieval defaults as settings when map contained any data
    Map<String, Object> expectedEffectiveSettings = new HashMap<>();
    expectedEffectiveSettings.putAll(getEmptyEffectiveSettings());

    assertEquals("maps shoudn't differ: " + Maps.difference(expectedEffectiveSettings, effectiveSettings),
            expectedEffectiveSettings,
            effectiveSettings);
  }

  @Test
  public void computeEffectiveSettingsWithTimeRangeChanged() {
    initSettingsWithTimeRange();
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(serviceSettings);

    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    // expect all the retrieval defaults as settings
    Map<String, Object> expectedEffectiveSettings = new HashMap<>();
    expectedEffectiveSettings.putAll(getEmptyEffectiveSettings());
    expectedEffectiveSettings.put(DOCUMENT_PROPERTY_TIME_RANGE, TIME_RANGE);

    assertEquals("maps shoudn't differ: " + Maps.difference(expectedEffectiveSettings, effectiveSettings),
            expectedEffectiveSettings,
            effectiveSettings);
  }

  @Test
  public void computeEffectiveSettingsWithDifferentValues() {
    initChangedDefaultSettings();
    when(settingsService.mergedSettingAsMap(eq(WEBTRENDS_SERVICE_KEY), eq(String.class), eq(Object.class), any(Content.class), any(Content.class))).thenReturn(serviceSettings);

    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    // expect all configured settings plus the retrieval defaults
    Map<String, Object> expectedEffectiveSettings = new HashMap<>();
    expectedEffectiveSettings.putAll(getEmptyEffectiveSettings());
    expectedEffectiveSettings.putAll(serviceSettings);

    assertEquals("maps shoudn't differ: " + Maps.difference(expectedEffectiveSettings, effectiveSettings),
            expectedEffectiveSettings,
            effectiveSettings);
  }

  private void initChangedDefaultSettings() {
    serviceSettings.put(ElasticWebtrendsServiceProvider.USER_NAME, USER_NAME);
    serviceSettings.put(ElasticWebtrendsServiceProvider.ACCOUNT_NAME, ACCOUNT_NAME);
    serviceSettings.put(ElasticWebtrendsServiceProvider.PROFILE_ID, PROFILE_ID);
    serviceSettings.put(ElasticWebtrendsServiceProvider.REPORT_ID, REPORT_ID);
    serviceSettings.put(DOCUMENT_PROPERTY_TIME_RANGE, TIME_RANGE);
    serviceSettings.put(ElasticWebtrendsServiceProvider.KEY_PASSWORD, PASSWORD);
  }

  private void initSettingsWithTimeRange() {
    serviceSettings.put(DOCUMENT_PROPERTY_TIME_RANGE, TIME_RANGE);
  }

  private Map<String, Object> getEmptyEffectiveSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.putAll(ElasticWebtrendsServiceProvider.DEFAULT_RETRIEVAL_SETTINGS);
    return settings;
  }
}
