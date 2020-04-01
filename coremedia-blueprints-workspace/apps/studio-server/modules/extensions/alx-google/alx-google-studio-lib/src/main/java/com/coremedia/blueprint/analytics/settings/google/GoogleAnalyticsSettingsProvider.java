package com.coremedia.blueprint.analytics.settings.google;

import com.coremedia.blueprint.analytics.settings.AbstractAnalyticsSettingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Google specific implementation of an {@link com.coremedia.blueprint.analytics.settings.AnalyticsSettingsProvider}
 * that creates deep-link report URLs.
 */
@Named
public class GoogleAnalyticsSettingsProvider extends AbstractAnalyticsSettingsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(GoogleAnalyticsSettingsProvider.class);

  private static final String GOOGLE_ANALYTICS = "googleAnalytics";

  /**
   * The name of the report to create the drill-down URL for.
   */
  private static final String KEY_PAGE_REPORT = "pageReport";
  /**
   * 8 digit numeric Google Analytics account ID to create the drill-down URL for: The X part of aXXXXXXXXwYYYYYYYYpZZZZZZZZ
   */
  private static final String KEY_ACCOUNT_ID = "accountId";
  /**
   * 8 digit numeric Google Analytics profile Id key to be used in drill-down urls: The Y part of aXXXXXXXXwYYYYYYYYpZZZZZZZZ
   */
  private static final String KEY_PID = "pid";
  /**
   * 8 digit numeric Google Analytics web property ID to create the drill-down URL for: The Z part of aXXXXXXXXwYYYYYYYYpZZZZZZZZ
   */
  private static final String KEY_WPID = "wpid";

  static final String DEFAULT_REPORT_NAME = "content-pages";
  static final String DEFAULT_REPORT_URL = "http://www.google.com/analytics/web/#report/";
  static final String DRILLDOWN_ARG_NAME = "?_r.drilldown=analytics.pagePath:";

  @Override
  protected String buildReportUrl(Map<String, Object> settings, String linkToSelf){
    if (settings != null) {
      final Object accountId = settings.get(KEY_ACCOUNT_ID);
      final String pageReport = getFromMap(settings, KEY_PAGE_REPORT, DEFAULT_REPORT_NAME);
      if (accountId != null) {
        final Object wpid = settings.get(KEY_WPID);
        final Object pid = settings.get(KEY_PID);

        final String reportUrlPrefix = getFromMap(settings, KEY_REPORT_URL_PREFIX, DEFAULT_REPORT_URL);
        final StringBuilder stringBuilder = new StringBuilder(reportUrlPrefix);
        if (!reportUrlPrefix.endsWith("/")) {
          stringBuilder.append('/');
        }
        stringBuilder.append(pageReport).append('/');
        stringBuilder.append("a").append(accountId);
        stringBuilder.append('w').append(wpid);
        stringBuilder.append('p').append(pid);
        stringBuilder.append('/');

        try {
          stringBuilder.append(URLEncoder.encode(DRILLDOWN_ARG_NAME, UTF_8));
          stringBuilder.append(URLEncoder.encode(linkToSelf, UTF_8));
          stringBuilder.append('/');
          return stringBuilder.toString();

        } catch (UnsupportedEncodingException e) {
          LOG.warn("cannot append drill-down URL fragment", e);
        }

      }
    }
    return null;
  }

  @Override
  public String getServiceKey() {
    return GOOGLE_ANALYTICS;
  }

  @Override
  protected boolean absolute() {
    return false;
  }
}
