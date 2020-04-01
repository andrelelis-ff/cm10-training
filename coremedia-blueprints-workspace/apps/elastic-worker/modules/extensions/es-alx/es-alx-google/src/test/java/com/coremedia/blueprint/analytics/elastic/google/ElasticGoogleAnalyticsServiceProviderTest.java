package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.SslUtils;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.analytics.elastic.google.ElasticGoogleAnalyticsServiceProvider.GOOGLE_ANALYTICS_SERVICE_KEY;
import static com.coremedia.blueprint.analytics.elastic.google.GoogleAnalyticsQuery.KEY_PID;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCUMENT_PROPERTY_ACTION;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCUMENT_PROPERTY_CATEGORY;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCUMENT_PROPERTY_MAX_LENGTH;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.DOCUMENT_PROPERTY_TIME_RANGE;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.KEY_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest({SecurityUtils.class, JacksonFactory.class, NetHttpTransport.class,
        GoogleNetHttpTransport.class, SslUtils.class,  PageViewQuery.class, PageViewHistoryQuery.class, OverallPerformanceQuery.class,
        EventQuery.class, GaData.class, ElasticGoogleAnalyticsServiceProvider.class, RetrievalUtil.class})
public class ElasticGoogleAnalyticsServiceProviderTest {

  @InjectMocks
  private final ElasticGoogleAnalyticsServiceProvider provider = new ElasticGoogleAnalyticsServiceProvider();

  @Mock
  private Content cmAlxBaseList;

  @Mock
  private Content cmAlxPageList;

  @Mock
  private Content cmAlxEventList;

  @Mock
  private ContentType baseType;

  @Mock
  private ContentType pageType;

  @Mock
  private ContentType eventType;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Map<String, Object> googleAnalyticsSettings;

  @Mock
  private Content content;

  @Mock
  private Analytics analytics;

  @Mock
  private Content contentBlob;

  @Mock
  private Blob blob;

  @Mock
  private PrivateKey privateKey;

  @Mock
  private NetHttpTransport netHttpTransport;

  @Mock
  private MockHttpTransport mockHttpTransport;

  @Mock
  private JacksonFactory jacksonFactory;

  @Mock
  private NetHttpTransport.Builder builder;

  @Mock
  private SSLContext sslContext;

  @Mock
  private PageViewQuery pageViewQuery;

  @Mock
  private PageViewHistoryQuery pageViewHistoryQuery;

  @Mock
  private OverallPerformanceQuery overallPerformanceQuery;

  @Mock
  private EventQuery eventQuery;

  @Mock
  private Analytics.Data.Ga.Get analyticsQuery;

  @Mock
  private GaData gaData;


  private static final int PID = 1234;
  private static final int TIME_RANGE = 30;

  private static final String TEST_APPLICATION_NAME = "KarHeinzCrop-KarlHeinzSeineKillerApp-42.0";

  @Before
  public void setup() throws Exception {
    when(cmAlxBaseList.getType()).thenReturn(baseType);
    when(cmAlxPageList.getType()).thenReturn(pageType);
    when(cmAlxEventList.getType()).thenReturn(eventType);

    when(baseType.isSubtypeOf("CMALXBaseList")).thenReturn(true);

    when(pageType.isSubtypeOf("CMALXBaseList")).thenReturn(true);
    when(pageType.isSubtypeOf("CMALXPageList")).thenReturn(true);

    when(eventType.isSubtypeOf("CMALXBaseList")).thenReturn(true);
    when(eventType.isSubtypeOf("CMALXEventList")).thenReturn(true);

    when(cmAlxBaseList.getInteger(DOCUMENT_PROPERTY_MAX_LENGTH)).thenReturn(null);
    when(cmAlxPageList.getInteger(DOCUMENT_PROPERTY_MAX_LENGTH)).thenReturn(null);
    when(cmAlxEventList.getInteger(DOCUMENT_PROPERTY_MAX_LENGTH)).thenReturn(null);
    when(cmAlxBaseList.getInteger(DOCUMENT_PROPERTY_TIME_RANGE)).thenReturn(null);
    when(cmAlxPageList.getInteger(DOCUMENT_PROPERTY_TIME_RANGE)).thenReturn(null);
    when(cmAlxEventList.getInteger(DOCUMENT_PROPERTY_TIME_RANGE)).thenReturn(null);

    googleAnalyticsSettings = new HashMap<>();
    when(contentBlob.getBlob("data")).thenReturn(blob);
    when(blob.getSize()).thenReturn(42);
    mockStatic(SecurityUtils.class);
    mockStatic(GoogleNetHttpTransport.class);
    mockStatic(NetHttpTransport.class);
    mockStatic(JacksonFactory.class);
    mockStatic(SslUtils.class);
    when(SecurityUtils.loadPrivateKeyFromKeyStore(nullable(KeyStore.class), nullable(InputStream.class), anyString(), anyString(), anyString())).thenReturn(privateKey);
    when(GoogleNetHttpTransport.newTrustedTransport()).thenReturn(netHttpTransport);
    when(SslUtils.initSslContext(any(SSLContext.class), any(KeyStore.class), any(TrustManagerFactory.class))).thenReturn(sslContext);
    when(JacksonFactory.getDefaultInstance()).thenReturn(jacksonFactory);
  }

  @Test
  public void fetchNoDataPageList() {
    getSettingsWithTimeRange();
    List<String> reportDataItems = provider.fetchDataFor(cmAlxPageList, googleAnalyticsSettings);
    assertEquals(0, reportDataItems.size());
  }

  @Test
  public void fetchDataPageList() throws Exception {
    when(settingsService.mergedSettingAsMap(eq(GOOGLE_ANALYTICS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(cmAlxPageList), any(Content.class))).thenReturn(getSettingsWithTimeRange());
    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    Map<String, Object> expectedEffectiveSettings = getEmptyEffectiveSettings();
    expectedEffectiveSettings.putAll(googleAnalyticsSettings);
    assertEquals("maps shoudn't differ: " + Maps.difference(expectedEffectiveSettings, effectiveSettings),
            expectedEffectiveSettings,
            effectiveSettings);

    getChangedDefaultSettings();
    whenNew(PageViewQuery.class).withAnyArguments().thenReturn(pageViewQuery);
    when(pageViewQuery.getDataQuery(any(Analytics.class))).thenReturn(analyticsQuery);
    when(analyticsQuery.execute()).thenReturn(gaData);
    when(gaData.getTotalResults()).thenReturn(1);
    List<String> pageViews = new ArrayList<>();
    pageViews.add("1234");
    when(pageViewQuery.process(anyList(), anyList())).thenReturn(pageViews);
    List<String> reportDataItems = provider.fetchDataFor(cmAlxPageList, googleAnalyticsSettings);

    assertEquals(1, reportDataItems.size());
  }

  @Test
  public void fetchDataEventList() throws Exception {
    whenNew(EventQuery.class).withAnyArguments().thenReturn(eventQuery);
    when(eventQuery.getDataQuery(any(Analytics.class))).thenReturn(analyticsQuery);
    when(analyticsQuery.execute()).thenReturn(gaData);
    List<String> pageViews = new ArrayList<>();
    pageViews.add("1234");
    when(eventQuery.process(anyList(), anyList())).thenReturn(pageViews);
    when(gaData.getTotalResults()).thenReturn(1);
    getChangedDefaultSettings();
    List<String> reportDataItems = provider.fetchDataFor(cmAlxEventList, googleAnalyticsSettings);

    assertEquals(1, reportDataItems.size());
  }

  @Test
  public void emptyListForInvalidContentbean() throws GeneralSecurityException, IOException {
    getChangedDefaultSettings();
    List<String> reportDataItems = provider.fetchDataFor(cmAlxBaseList, googleAnalyticsSettings);

    assertEquals("No report data items for invalid contentbean.", 0, reportDataItems.size());
  }

  @Test
  public void fetchPageViews() throws Exception {
    String contentId = "12";
    String dateString = "20130713";
    long uniqueViews = 42L;

    when(content.getId()).thenReturn(contentId);

    getChangedDefaultSettings();
    whenNew(PageViewHistoryQuery.class).withAnyArguments().thenReturn(pageViewHistoryQuery);
    whenNew(OverallPerformanceQuery.class).withAnyArguments().thenReturn(overallPerformanceQuery);
    when(pageViewHistoryQuery.getDataQuery(any(Analytics.class))).thenReturn(analyticsQuery);
    when(overallPerformanceQuery.getDataQuery(any(Analytics.class))).thenReturn(analyticsQuery);
    when(analyticsQuery.execute()).thenReturn(gaData);
    HashMap<String, Map<String, Long>> processedResult = new HashMap<>();
    Map<String, Long> map = new HashMap<>();
    map.put(dateString, uniqueViews);
    processedResult.put(contentId, map);
    when(pageViewHistoryQuery.process(anyList(), anyList())).thenReturn(processedResult);
    when(gaData.getTotalResults()).thenReturn(1);
    Map<String, Map<String, Long>> result = provider.fetchPageViews(content, googleAnalyticsSettings);

    assertEquals(1, result.size());
    assertEquals(42L, (Object) result.get(contentId).get(dateString));
  }

  @Test
  public void fetchPageViewsWithInvalidSettings() {
    when(settingsService.mergedSettingAsMap(eq(GOOGLE_ANALYTICS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(cmAlxBaseList), any(Content.class))).thenReturn(getSettingsWithTimeRange());
    Map<String, Object> effectiveSettings = getEmptyEffectiveSettings();
    effectiveSettings.putAll(googleAnalyticsSettings);
    assertEquals(effectiveSettings, provider.computeEffectiveRetrievalSettings(cmAlxBaseList, mock(Content.class)));
    provider.computeEffectiveRetrievalSettings(cmAlxBaseList, mock(Content.class));

    Map<String, Map<String, Long>> result = provider.fetchPageViews(content, googleAnalyticsSettings);

    assertEquals(0, result.size());
  }

  @Test
  public void testServiceKey() {
    assertEquals(GOOGLE_ANALYTICS_SERVICE_KEY, provider.getServiceKey());
  }

  @Test
  public void computeEffectiveSettingsWithEmptySettings() {
    when(settingsService.mergedSettingAsMap(eq(GOOGLE_ANALYTICS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(cmAlxPageList), any(Content.class))).thenReturn(new HashMap<String, Object>());

    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    // expect empty settings when called with empty map
    assertEquals("maps shoudn't differ: " + Maps.difference(Collections.EMPTY_MAP, effectiveSettings),
            Collections.EMPTY_MAP,
            effectiveSettings);
  }

  @Test
  public void computeEffectiveSettingsWithUnimportantSettings() {
    when(settingsService.mergedSettingAsMap(eq(GOOGLE_ANALYTICS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(cmAlxPageList), any(Content.class))).thenReturn(getSettingsWithUnused());

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
    when(settingsService.mergedSettingAsMap(eq(GOOGLE_ANALYTICS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(cmAlxPageList), any(Content.class))).thenReturn(getSettingsWithTimeRange());

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
    when(settingsService.mergedSettingAsMap(eq(GOOGLE_ANALYTICS_SERVICE_KEY), eq(String.class), eq(Object.class), eq(cmAlxPageList), any(Content.class))).thenReturn(getChangedDefaultSettings());

    Map<String, Object> effectiveSettings = provider.computeEffectiveRetrievalSettings(cmAlxPageList, mock(Content.class));

    // expect all configured settings plus the retrieval defaults
    Map<String, Object> expectedEffectiveSettings = new HashMap<>();
    expectedEffectiveSettings.putAll(getEmptyEffectiveSettings());
    expectedEffectiveSettings.putAll(googleAnalyticsSettings);

    assertEquals("maps shoudn't differ: " + Maps.difference(expectedEffectiveSettings, effectiveSettings),
            expectedEffectiveSettings,
            effectiveSettings);
  }

  private Map<String, Object> getChangedDefaultSettings() {
    googleAnalyticsSettings.put(DOCUMENT_PROPERTY_TIME_RANGE, TIME_RANGE);
    googleAnalyticsSettings.put(DOCUMENT_PROPERTY_ACTION, "myAction");
    googleAnalyticsSettings.put(DOCUMENT_PROPERTY_CATEGORY, "myCategory");
    googleAnalyticsSettings.put(KEY_PID, PID);
    googleAnalyticsSettings.put(KEY_LIMIT, 20);
    googleAnalyticsSettings.put(ElasticGoogleAnalyticsServiceProvider.SERVICE_ACCOUNT_EMAIL, "abcd@efgh.com");
    googleAnalyticsSettings.put(ElasticGoogleAnalyticsServiceProvider.P12_FILE, contentBlob);
    googleAnalyticsSettings.put(ElasticGoogleAnalyticsServiceProvider.APPLICATION_NAME, TEST_APPLICATION_NAME);

    return googleAnalyticsSettings;
  }

  private Map<String, Object> getSettingsWithTimeRange() {
    googleAnalyticsSettings.put(DOCUMENT_PROPERTY_TIME_RANGE, TIME_RANGE);
    return googleAnalyticsSettings;
  }

  private Map<String, Object> getSettingsWithUnused() {
    googleAnalyticsSettings.put("unused", "does not matter");
    return googleAnalyticsSettings;
  }

  private Map<String, Object> getEmptyEffectiveSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.putAll(ElasticGoogleAnalyticsServiceProvider.DEFAULT_RETRIEVAL_SETTINGS);
    return settings;
  }
}