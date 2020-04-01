package com.coremedia.blueprint.analytics.settings;

import com.coremedia.cap.content.Content;

/**
 * Analytics settings providers provide a map of properties to be used by the STUDIO part of the ALX integration.
 */
public interface AnalyticsSettingsProvider {

  /**
   * Return the service key of this analytics settings provider.
   *
   * @return the service key
   */
  String getServiceKey();

  String getReportUrlFor(Content content);

}
