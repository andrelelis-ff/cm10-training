package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.GaData;
import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Implements data retrieval for Google Analytics backed by CoreMedia Elastic storage.
 */
@SuppressWarnings("UnusedDeclaration")
@Named
public class ElasticGoogleAnalyticsServiceProvider implements AnalyticsServiceProvider {

  /**
   * The Google Analytics service key.
   */
  public static final String GOOGLE_ANALYTICS_SERVICE_KEY = "googleAnalytics";
  static final String APPLICATION_NAME = "applicationName";
  static final String SERVICE_ACCOUNT_EMAIL = "serviceAccountEmail";
  static final String P12_FILE = "p12File";
  static final String KEY_PASS = "notasecret";

  static final Map<String, Object> DEFAULT_RETRIEVAL_SETTINGS = ImmutableMap.<String, Object>builder()
          .putAll(RetrievalUtil.DEFAULT_RETRIEVAL_SETTINGS)
          .put(GoogleAnalyticsQuery.KEY_PID, Defaults.defaultValue(int.class))
          .put(APPLICATION_NAME, "")
          .put(SERVICE_ACCOUNT_EMAIL, "")
          .put(P12_FILE, new Object())
          .build();

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticGoogleAnalyticsServiceProvider.class);

  @Inject
  private SettingsService settingsService;

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();


  @Override
  public String getServiceKey() {
    return GOOGLE_ANALYTICS_SERVICE_KEY;
  }

  @Override
  public List<String> fetchDataFor(Content cmalxBaseList, Map<String, Object> effectiveSettings) {
    LOGGER.debug("fetching data for {}", cmalxBaseList);
    final GoogleAnalyticsSettings googleAnalyticsSettings = SettingsUtil.createProxy(GoogleAnalyticsSettings.class, effectiveSettings);

    String applicationName = googleAnalyticsSettings.getApplicationName();
    String clientId = googleAnalyticsSettings.getServiceAccountEmail();
    PrivateKey privateKey = getPrivateKeyFromContent(googleAnalyticsSettings);

    if (validateConnectionSettings(applicationName, clientId, privateKey)) {
      final GoogleAnalyticsListQuery query = createTopNListQuery(cmalxBaseList, googleAnalyticsSettings);
      if (query != null) {
        try {
          Analytics analytics = initializeAnalytics(applicationName, clientId, privateKey);
          GaData gaData = call(analytics, query);
          if (gaData != null && gaData.getTotalResults() > 0) {
            LOGGER.debug("Got {} tracked page views from Google Analytics", gaData.getTotalResults());
            return query.process(gaData.getRows(), gaData.getColumnHeaders());
          } else {
            LOGGER.debug("Got no tracked page views from Google Analytics");
          }
        } catch (Exception e) {
          LOGGER.debug("cannot initialize Google Analytics for {}.", cmalxBaseList, e);
        }
      } else {
        LOGGER.debug("cannot fetch data for {}. Cannot create query", cmalxBaseList);
      }
    }
    return Collections.emptyList();
  }

  private PrivateKey getPrivateKeyFromContent(GoogleAnalyticsSettings googleAnalyticsSettings) {
    Content p12File = googleAnalyticsSettings.getP12File();
    if (null != p12File) {
      Blob blob = p12File.getBlob("data");
      if (null != blob && blob.getSize() > 0) {
        try (InputStream inputStream = blob.getInputStream()) {
          return SecurityUtils.loadPrivateKeyFromKeyStore(
                  SecurityUtils.getPkcs12KeyStore(), inputStream, KEY_PASS,
                  "privatekey", KEY_PASS);
        } catch (IOException e) {
          LOGGER.warn("Cannot load inputStream for p12File {}", p12File.getName());
          LOGGER.debug("Cannot load inputStream for p12File {}", p12File.getName(), e);
        } catch (GeneralSecurityException e) {
          LOGGER.warn("Cannot load private key from store for blob {}", p12File.getName());
          LOGGER.debug("Cannot load private key from store for blob {}", p12File.getName(), e);
        }
      }
    }
    return null;
  }


  private static Analytics initializeAnalytics(String applicationName, String clientId, PrivateKey googlePrivateKey) throws GeneralSecurityException, IOException {
    // Authorization.
    Credential credential = authorize(clientId, googlePrivateKey);

    // Set up and return Google Analytics API client.
    return new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
            applicationName).build();

  }

  /**
   * Authorizes the installed application to access user's protected data.
   */
  private static Credential authorize(String clientId, PrivateKey googlePrivateKey) throws GeneralSecurityException, IOException {
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

// Build service account credential.
    return new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setServiceAccountId(clientId)
            .setServiceAccountScopes(Collections.singleton(AnalyticsScopes.ANALYTICS_READONLY))
            .setServiceAccountPrivateKey(googlePrivateKey)
            .build();
  }

  GoogleAnalyticsListQuery createTopNListQuery(Content cmalxBaseList, GoogleAnalyticsSettings googleAnalyticsSettings) {
    GoogleAnalyticsListQuery query = null;
    if (GoogleAnalyticsQuery.canCreateQuery(googleAnalyticsSettings)) {
      if (cmalxBaseList.getType().isSubtypeOf(RetrievalUtil.DOCTYPE_PAGELIST)) {
        query = new PageViewQuery(googleAnalyticsSettings);
      } else if (cmalxBaseList.getType().isSubtypeOf(RetrievalUtil.DOCTYPE_EVENTLIST)) {
        query = new EventQuery(googleAnalyticsSettings);
      }
    }
    return query;
  }

  @Override
  public Map<String, Map<String, Long>> fetchPageViews(Content content, Map<String, Object> rootNavigationSettings) {

    final GoogleAnalyticsSettings googleAnalyticsSettings = SettingsUtil.createProxy(GoogleAnalyticsSettings.class, rootNavigationSettings);
    Map<String, Map<String, Long>> result = Collections.emptyMap();
    if (GoogleAnalyticsQuery.canCreateQuery(googleAnalyticsSettings)) {
      final int timeRange = googleAnalyticsSettings.getTimeRange();
      LOGGER.info("fetching data for the last {} days", timeRange > 0 ? timeRange : RetrievalUtil.DEFAULT_TIMERANGE);

      PageViewHistoryQuery query = new PageViewHistoryQuery(googleAnalyticsSettings);

      String applicationName = googleAnalyticsSettings.getApplicationName();
      String serviceAccountEmail = googleAnalyticsSettings.getServiceAccountEmail();
      PrivateKey privateKey = getPrivateKeyFromContent(googleAnalyticsSettings);

      if (validateConnectionSettings(applicationName, serviceAccountEmail, privateKey)) {

        try {
          Analytics analytics = initializeAnalytics(applicationName, serviceAccountEmail, privateKey);
          GaData gaData = call(analytics, query);
          if (gaData != null && gaData.getTotalResults() > 0) {
            result = newHashMap(query.process(gaData.getRows(), gaData.getColumnHeaders()));
          }
          // process pageviews map for the root navigation node
          OverallPerformanceQuery overAllQuery = new OverallPerformanceQuery(content, googleAnalyticsSettings);
          GaData overAllDataEntries = call(analytics, overAllQuery);
          if (overAllDataEntries != null && overAllDataEntries.getTotalResults() > 0) {
            result.putAll(overAllQuery.process(overAllDataEntries.getRows(), overAllDataEntries.getColumnHeaders()));
          }
        } catch (Exception e) {
          LOGGER.debug("cannot initialize Google Analytics for {}.", content, e);
        }
      }
    } else {
      LOGGER.debug("cannot fetch page views for {}. Cannot create google Analytics query.", content);
    }
    return result;
  }

  private boolean validateConnectionSettings(String applicationName, String serviceAccountEmail, PrivateKey privateKey) {
    if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(serviceAccountEmail)) {
      LOGGER.info("Configuration missing, retrieval from Google Analytics disabled.");
      return false;
    } else if (privateKey == null) {
      LOGGER.info("Google p12 file missing, retrieval from Google Analytics disabled.");
      return false;
    }
    return true;
  }

  /**
   * @return List of results as returned by Google Analytics.
   */
  @Nullable
  private GaData call(@NonNull Analytics analytics, @NonNull GoogleAnalyticsQuery googleAnalyticsQuery) {
    try {
      final Analytics.Data.Ga.Get query = googleAnalyticsQuery.getDataQuery(analytics);
      LOGGER.debug("Firing Google Data Export API query: '{}'", query);
      return query.execute();
    } catch (IOException e) {
      logWarningForServiceAccess(googleAnalyticsQuery, e);
    }
    return null;
  }

  private void logWarningForServiceAccess(GoogleAnalyticsQuery googleAnalyticsQuery, Exception e) {
    LOGGER.warn("Caught exception while retrieving data for '{}' from google analytics: {}",
            googleAnalyticsQuery,
            e.getMessage());
  }

  @Override
  public Map<String, Object> computeEffectiveRetrievalSettings(Content cmalxBaseList, Content rootNavigation) {
    return RetrievalUtil.computeEffectiveRetrievalSettings(GOOGLE_ANALYTICS_SERVICE_KEY, DEFAULT_RETRIEVAL_SETTINGS, cmalxBaseList, rootNavigation, settingsService);
  }
}
