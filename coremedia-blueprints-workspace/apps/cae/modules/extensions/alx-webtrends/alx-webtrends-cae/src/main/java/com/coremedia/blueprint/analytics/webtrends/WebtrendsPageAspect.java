package com.coremedia.blueprint.analytics.webtrends;

import com.coremedia.blueprint.analytics.AnalyticsProvider;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;

/**
 * <p>An analytics page aspect that makes Webtrends account data visible to the view.</p>
 * <ul>
 * <li><b>dcsid:</b> the Webtrends dcsid.
 * The dcsid is a unique identifier that associates the data collected from a website or app with a space or profile
 * The dcsid is retrieved from the settings property
 *   of the page being rendered using the key 'dcsid'.</li>
 * <li><b>dcssip:</b> the host name to track. The host name is retrieved from the settings property
 *   of the page being rendered using the key 'dcssip'.</li>
 * </ul>
 * <p>
 *   Both properties 'dcsid' and 'dcssip' are required.
 * </p>
 * <p>If you want to disable tracking to Webtrends, set
 * the corresponding settings property <code>webtrends.enabled</code>
 * to <code>false</code>.</p>
 */
public class WebtrendsPageAspect extends AnalyticsProvider {

  public static final String DCSSIP = "dcssip";

  /**
   * The Webtrends service key.
   */
  public static final String WEBTRENDS_SERVICE_KEY = "webtrends";
  static final String DCSID = "dcsid";

  public WebtrendsPageAspect(Page page, SettingsService settingsService) {
    super(WEBTRENDS_SERVICE_KEY, page, settingsService);
  }

  public Object getDcsid() {
    return getSettings().get(DCSID);
  }

  public Object getDcssip() {
    return getSettings().get(DCSSIP);
  }

  @Override
  protected boolean isConfigValid() {
    return isNonEmptyString(getDcsid(), DCSID) && isNonEmptyString(getDcssip(), DCSSIP);
  }
}
