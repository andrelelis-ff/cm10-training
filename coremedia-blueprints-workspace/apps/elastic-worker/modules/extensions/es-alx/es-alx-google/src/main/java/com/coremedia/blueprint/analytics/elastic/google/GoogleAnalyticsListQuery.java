package com.coremedia.blueprint.analytics.elastic.google;


import com.google.api.services.analytics.model.GaData;

import java.util.List;

public abstract class GoogleAnalyticsListQuery extends GoogleAnalyticsQuery {

  protected GoogleAnalyticsListQuery(int profileId, int timeRange, int maxResults) {
    super(profileId, timeRange, maxResults);
  }

  public abstract List<String> process(List<List<String>> dataEntries, List<GaData.ColumnHeaders> columnHeaders);
}
