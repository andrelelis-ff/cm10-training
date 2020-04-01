package com.coremedia.blueprint.analytics.elastic.google;

import com.coremedia.cap.content.Content;

/**
 * Utility interface for usage with {@link com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil}
 */
interface GoogleAnalyticsSettings {

  /**
   * The maximum number of records to fetch from google.
   *
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#KEY_LIMIT
   */
  int getLimit();

  /**
   * The profile to query
   *
   * @see GoogleAnalyticsQuery#KEY_PID
   */
  int getPid();

  /**
   * Only relevant for event queries
   *
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#DOCUMENT_PROPERTY_CATEGORY
   */
  String getCategory();

  /**
   * Only relevant for event queries
   *
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#DOCUMENT_PROPERTY_ACTION
   */
  String getAction();

  /**
   *
   * @see ElasticGoogleAnalyticsServiceProvider#APPLICATION_NAME
   */
  String getApplicationName();

  /**
   *
   * @see com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil#DOCUMENT_PROPERTY_TIME_RANGE
   */
  int getTimeRange();

  /**
   * Returns the google service account Email Address
   */
  String getServiceAccountEmail();

  /**
   * Returns the Google Analytics p12 file
   */
  Content getP12File();
}
