package com.coremedia.blueprint.analytics.elastic.webtrends;

import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * This service provider fetches data from Webtrends and stores it using CoreMedia's elastic core
 * infrastructure.
 */
@Named
public class ElasticWebtrendsServiceProvider implements AnalyticsServiceProvider {

  public static final String WEBTRENDS_SERVICE_KEY = "webtrends";

  private static final Logger LOG = LoggerFactory.getLogger(ElasticWebtrendsServiceProvider.class);

  @Inject
  private SettingsService settingsService;

  private static final String WEBTRENDS_REST_URL_PATTERN = "https://ws.webtrends.com/v3/Reporting/profiles/{0,number,#}/reports/{1}/?totals=none&start_period=current_day-{2,number,#}&end_period=current_day&period_type=agg&range={3,number,#}&sortby={4}&format=json";
  private static final String WEBTRENDS_HOST = "ws.webtrends.com";
  private static final int WEBTRENDS_PORT = 443;

  static final String ACCOUNT_NAME = "accountName";
  static final String USER_NAME = "userName";
  static final String PROFILE_ID = "profileId";
  static final String REPORT_ID = "reportId";
  static final String KEY_PASSWORD = "password"; // NOSONAR false positive: Credentials should not be hard-coded

  private static final String SORT_BY_MEASURE = "sortByMeasure";
  private static final String DEFAULT_SORT_BY_MEASURE = "Hits";

  static final Map<String, Object> DEFAULT_RETRIEVAL_SETTINGS = ImmutableMap.<String,Object>builder()
          .putAll(RetrievalUtil.DEFAULT_RETRIEVAL_SETTINGS)
          .put(SORT_BY_MEASURE, DEFAULT_SORT_BY_MEASURE)
          .put(REPORT_ID, "")
          .put(PROFILE_ID, 0)
          .put(USER_NAME, "")
          .put(ACCOUNT_NAME, "")
          .put(KEY_PASSWORD, "")
          .build();

  @Override
  public List<String> fetchDataFor(Content cmalxBaseList, Map<String, Object> effectiveSettings) throws Exception {
    final Object profileId = effectiveSettings.get(PROFILE_ID); // int
    final Object reportId = effectiveSettings.get(REPORT_ID); // String

    final UsernamePasswordCredentials credentials = createCredentials(effectiveSettings);

    if(profileId instanceof Integer &&  reportId instanceof String && credentials.getPassword() != null && credentials.getUserName().length() > 1) {

      final int timeRange = RetrievalUtil.getTimeRange(effectiveSettings);
      final int limit = RetrievalUtil.getLimit(effectiveSettings);
      final String sortByMeasure = RetrievalUtil.getValue(effectiveSettings, DEFAULT_SORT_BY_MEASURE, SORT_BY_MEASURE);

      LOG.trace("fetching report for '{}' with effectiveSettings '{}'", cmalxBaseList, effectiveSettings);
      return fetchReportData((Integer)profileId, (String)reportId, timeRange, limit, sortByMeasure, credentials);

    } else {
      LOG.debug("config is incomplete, cannot fetch data for '{}' with effectiveSettings '{}'", cmalxBaseList, effectiveSettings);
    }

    return emptyList();
  }

  private UsernamePasswordCredentials createCredentials(Map<String, Object> settings) {
    final Object password = settings.get(KEY_PASSWORD);
    final Object accountName = settings.get(ACCOUNT_NAME);
    final Object userName = settings.get(USER_NAME);

    return new UsernamePasswordCredentials(join(new Object[]{accountName, userName}, "\\"), join(password));
  }

  @Override
  public String getServiceKey() {
    return WEBTRENDS_SERVICE_KEY;
  }

  private List<String> fetchReportData(int profileId, String reportId, int timeRange, int limit, String sortByMeasure, UsernamePasswordCredentials credentials) {
    try (DefaultHttpClient httpClient = new DefaultHttpClient()) {
      httpClient.getCredentialsProvider().setCredentials(
              new AuthScope(WEBTRENDS_HOST, WEBTRENDS_PORT),
              credentials);

      final String url = MessageFormat.format(WEBTRENDS_REST_URL_PATTERN, profileId, reportId, timeRange, limit, sortByMeasure);
      final HttpGet httpget = new HttpGet(url);
      final HttpResponse response = httpClient.execute(httpget);
      final StatusLine statusLine = response.getStatusLine();

      if(HttpStatus.SC_OK == statusLine.getStatusCode()) {
        final List<String> strings = processResponse(sortByMeasure, response);
        LOG.debug("result list is {}", strings);
        return strings;
      } else {
        LOG.info("status line is {}", statusLine);
      }
    } catch (IOException e) {
      LOG.info("exception while fetching report: {}", e.getMessage());
    }
    return emptyList();
  }

  private List<String> processResponse(String sortByMeasureName, HttpResponse response) throws IOException {
    final JsonElement jsonElement = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent()));
    LOG.trace("report data JSON is {}", jsonElement);

    final JsonArray data = jsonElement.getAsJsonObject().getAsJsonArray("data");
    if (data != null && data.size() > 0) {
      return processReport(sortByMeasureName, data.get(0).getAsJsonObject());
    }
    return emptyList();
  }

  private List<String> processReport(String sortByMeasureName, JsonObject dataElem) {
    final JsonElement subRowElem = dataElem.get("SubRows");
    if(subRowElem != null && subRowElem.isJsonArray()) {
      final JsonArray subRows = subRowElem.getAsJsonArray();

      if (subRows != null && subRows.size() > 0) {
        final JsonObject reportData = subRows.get(0).getAsJsonObject();

        if(reportData != null) {
          final Set<Map.Entry<String, JsonElement>> entries = reportData.entrySet();
          final List<ReportEntry> reportEntries = new ArrayList<>(entries.size());
          for (Map.Entry<String, JsonElement> entry : entries) {
            final String key = entry.getKey();
            final JsonObject value = entry.getValue().getAsJsonObject();
            final JsonElement sortByMeasure = value.getAsJsonObject("measures").get(sortByMeasureName);
            reportEntries.add(new ReportEntry(key, sortByMeasure.getAsDouble()));
          }

          Collections.sort(reportEntries, Collections.reverseOrder());
          return Lists.transform(reportEntries, new ReportEntryTransformer());
        } else {
          LOG.trace("report data is empty");
        }
      } else {
        LOG.trace("cannot find 'SubRows' array to evaluate");
      }
    }
    return emptyList();
  }

  @Override
  public Map<String, Map<String, Long>> fetchPageViews(Content content, Map<String, Object> settings) throws Exception {
    LOG.debug("page view retrieval not supported by service provider '{}'", getServiceKey());
    return Collections.emptyMap();
  }

  @Override
  public Map<String, Object> computeEffectiveRetrievalSettings(Content cmalxBaseList, Content rootNavigation) {
    return RetrievalUtil.computeEffectiveRetrievalSettings(WEBTRENDS_SERVICE_KEY, DEFAULT_RETRIEVAL_SETTINGS, cmalxBaseList, rootNavigation, settingsService);
  }
}