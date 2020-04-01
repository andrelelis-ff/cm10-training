package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMDynamicList;

/**
 * Interface for beans of document type "CMALXBaseList". Instances represent "top-n-lists" and are used
 * for data retrieval and rendering of "top-n-lists".
 */
public interface CMALXBaseList<T> extends CMDynamicList<T> {

  /**
   * Maximum length of the top n list
   */
  int DEFAULT_MAX_LENGTH = 10;

  /**
   * Default time range in days
   */
  int DEFAULT_TIME_RANGE = 90;  // default is 90 days

  /**
   * time range content property name
   */
  String TIME_RANGE = "timeRange";

  /**
   * analytics provider property name (the analytics service provider to use at rendering time)
   */
  String ANALYTICS_PROVIDER = "analyticsProvider";

  /**
   * Get the time range to fetch the report for. The configured value or {@link #DEFAULT_TIME_RANGE}
   * @return number of days to be included in the report
   */
  int getTimeRange();

  /**
   * The analytics service provider name to use when rendering the list
   * @return a String matching one of the configured analytics service providers
   */
  String getAnalyticsProvider();

}