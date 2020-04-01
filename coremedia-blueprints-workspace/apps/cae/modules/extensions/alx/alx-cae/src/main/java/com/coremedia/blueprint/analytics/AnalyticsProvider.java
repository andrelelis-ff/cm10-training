package com.coremedia.blueprint.analytics;


import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;

public class AnalyticsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(AnalyticsProvider.class);

  private static final String DISABLED = "disabled";

  private final String serviceKey;

  private Page aggregator;

  private SettingsService settingsService;

  /**
   * Create analytics provider
   *
   * @param serviceKey the service name of this instance
   */
  protected AnalyticsProvider(String serviceKey, Page aggregator, SettingsService settingsService) {
    this.serviceKey = serviceKey;
    this.aggregator = aggregator;
    this.settingsService = settingsService;
  }

  public String getServiceKey() {
    return serviceKey;
  }

  public Page getAggregator() {
    return aggregator;
  }

  /**
   * The navigation path list of the content's current channel
   *
   * @return list of navigation items
   */
  protected List<? extends Linkable> getNavigationPath() {
    return aggregator.getNavigation().getNavigationPathList();
  }

  /**
   * Transform a list of navigation items into a string array for rendering
   *
   * @param transformer a function to transform the current navigation path items
   * @return a string array
   */
  protected String[] transformNavigationPath(Function<Linkable, String> transformer) {
    final List<? extends Linkable> navigationPath = getNavigationPath();
    final String[] result = new String[navigationPath.size()];
    Lists.transform(navigationPath, transformer).toArray(result);
    return result;
  }

  /**
   * The navigation path from the root navigation to the navigation the currently rendered {@link Page} belongs to.
   * The path is represented as an Array of numeric <i>content IDs</i>.
   *
   * @return string array of numeric content ids for rendering
   */
  public String[] getNavigationPathIds() {
    return transformNavigationPath(new NavigationToNumericId());
  }

  /**
   * The navigation path from the root navigation to the navigation the currently rendered {@link Page} belongs to.
   * The path is represented as an Array of <i>segments names</i>
   *
   * @return string array of segment names for rendering
   */
  public String[] getNavigationPathSegments() {
    return transformNavigationPath(new NavigationToSegment());
  }

  /**
   * The id of the content being rendered on the aggregator, e.g. the numeric content id of a CMS document
   *
   * @return the content id
   * @see com.coremedia.blueprint.common.contentbeans.CMObject#getContentId()
   */
  public String getContentId() {
    return aggregator.getContentId();
  }

  /**
   * The type or classifier of the content being rendered
   */
  public String getContentType() {
    return aggregator.getContentType();
  }

  public Object getContent() {
    return aggregator.getContent();
  }

  /**
   * Check if this service is enabled, that is
   * <ul>
   * <li>
   * if it is not explicitly disabled, and
   * </li>
   * <li>
   * if the service's configuration is considered {@link #isConfigValid valid}
   * </li>
   * </ul>
   *
   * @return true if and only if all the conditions above match
   */
  public final boolean isEnabled() {
    final Object o = getSettings().get(DISABLED);
    boolean enabled = false;
    if (o instanceof Boolean && (Boolean) o) {
      LOG.debug("tracking is disabled for {}", this);
    } else {
      enabled = isConfigValid();
      if (!enabled) {
        LOG.debug("tracking configuration is invalid: {}", this);
      }
    }
    return enabled;
  }

  /**
   * Decide if the current page's settings contain a valid tracking configuration
   *
   * @return true - there's nothing to check per default
   */
  protected boolean isConfigValid() {
    return true;
  }

  protected Map<String, Object> getSettings() {
    return settingsService.settingAsMap(serviceKey, String.class, Object.class, getAggregator());
  }

  protected boolean isNonEmptyString(Object o, String propertyName) {
    final boolean b = o instanceof CharSequence && StringUtils.isNotEmpty((CharSequence) o);
    if (!b) {
      LOG.debug("required property {} is not set: {}", propertyName, this);
    }
    return b;
  }

  @Override
  public String toString() {
    return "AnalyticsProvider{" +
            "aggregator=" + aggregator +
            ",settings[" + serviceKey + "]" +
            '}';
  }

  private static class NavigationToNumericId implements Function<Linkable, String> {
    @Override
    public String apply(@Nullable Linkable input) {
      if (input != null && input instanceof CMNavigation) {
        return String.valueOf(((CMNavigation) input).getContentId());
      }
      return null;
    }
  }

  private static class NavigationToSegment implements Function<Linkable, String> {
    @Override
    public String apply(@Nullable Linkable input) {
      if (input != null) {
        return input.getSegment();
      }
      return null;
    }
  }
}
