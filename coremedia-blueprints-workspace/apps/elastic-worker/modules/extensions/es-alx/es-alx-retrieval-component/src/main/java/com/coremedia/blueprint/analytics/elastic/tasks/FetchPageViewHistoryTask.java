package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * A task to retrieve the number of page views from the configured analytics service providers.
 */
@Named
public class FetchPageViewHistoryTask extends AbstractRootContentProcessingTask {

  private final FetchPageViewHistoryRootContentProcessor fetchPageViewHistoryRootContentProcessor;

  @Inject
  public FetchPageViewHistoryTask(FetchPageViewHistoryRootContentProcessor fetchPageViewHistoryRootContentProcessor, RootContentProcessingTaskHelper rootContentProcessingTaskHelper) {
    super(rootContentProcessingTaskHelper);
    this.fetchPageViewHistoryRootContentProcessor = fetchPageViewHistoryRootContentProcessor;
  }

  @Override
  void processRootNavigation(@NonNull Content rootNavigation, @NonNull Map<String, Object> serviceSettings, @NonNull AnalyticsServiceProvider analyticsServiceProvider) {
    fetchPageViewHistoryRootContentProcessor.processRootContent(rootNavigation, serviceSettings, analyticsServiceProvider);
  }
}
