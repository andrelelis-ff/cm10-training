package com.coremedia.blueprint.analytics.settings.webtrends;

import com.coremedia.blueprint.analytics.settings.AbstractAnalyticsSettingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Webtrends specific implementation of an {@link com.coremedia.blueprint.analytics.settings.AnalyticsSettingsProvider}
 * that creates deep-link report URLs.
 */
@Named
public class WebtrendsSettingsProvider extends AbstractAnalyticsSettingsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(WebtrendsSettingsProvider.class);

  private static final String WEBTRENDS = "webtrends";

  private static final String KEY_SPACE_ID = "spaceId";
  private static final String KEY_REPORT_ID = "reportId";
  private static final String KEY_PROFILE_ID = "profileId";

  private static final String DEFAULT_REPORT_URL_PREFIX = "https://analytics.webtrends.com/analytics";

  @Override
  protected String buildReportUrl(Map<String, Object> settings, String linkToSelf) {
    if (settings == null) {
      return null;
    }

    final Object spaceId = settings.get(KEY_SPACE_ID);

    if(spaceId != null){
      final Object profileId = settings.get(KEY_PROFILE_ID);
      final Object reportId = settings.get(KEY_REPORT_ID);

      final String reportUrlPrefix = getFromMap(settings, KEY_REPORT_URL_PREFIX, DEFAULT_REPORT_URL_PREFIX);
      final StringBuilder stringBuilder = new StringBuilder(reportUrlPrefix);
      if(!reportUrlPrefix.endsWith("/")){
        stringBuilder.append('/');
      }
      stringBuilder.append("spaces").append('/').append(spaceId).append('/');
      stringBuilder.append("profiles").append('/').append(profileId).append('/');
      stringBuilder.append("reports").append('/').append(reportId).append('/');
      try {
        stringBuilder.append("urldashboard?url=").append(URLEncoder.encode(linkToSelf, UTF_8));
        return stringBuilder.toString();
      } catch (UnsupportedEncodingException e) {
        LOG.warn("cannot append drill-down URL fragment", e);
      }

    }
    return null;
  }

  @Override
  public String getServiceKey() {
    return WEBTRENDS;
  }

  @Override
  protected boolean absolute() {
    return true;
  }

}
