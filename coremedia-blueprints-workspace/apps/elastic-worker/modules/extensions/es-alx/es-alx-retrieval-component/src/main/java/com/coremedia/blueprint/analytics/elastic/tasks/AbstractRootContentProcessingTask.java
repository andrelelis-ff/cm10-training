package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract implementation of a retrieval task processing a root navigation content.
 */
public abstract class AbstractRootContentProcessingTask implements Runnable {

  private final RootContentProcessingTaskHelper rootContentProcessingTaskHelper;

  /**
   * Create instance with a given {@link RootContentProcessingTaskHelper} instance. When running, the task
   * delegates work to the rootContentProcessingTaskHelper
   * @param rootContentProcessingTaskHelper the non-null instance to delegate to
   */
  protected AbstractRootContentProcessingTask(RootContentProcessingTaskHelper rootContentProcessingTaskHelper) {
    checkNotNull(rootContentProcessingTaskHelper, "rootContentProcessingTaskHelper must not be null");
    this.rootContentProcessingTaskHelper = rootContentProcessingTaskHelper;
  }

  /**
   * Overwrite this method to fire the Analytics provider specific queries (e.g. fetching tracked pageviews).
   *
   * @param rootNavigation the current root navigation being the source of the passed-in serviceSettings
   * @param serviceSettings the provider-specific settings (sub-map) of the rootNavigation's settings
   * @param analyticsServiceProvider the {@link AnalyticsServiceProvider} instance to fetch data for
   */
  abstract void processRootNavigation(@NonNull Content rootNavigation, @NonNull Map<String, Object> serviceSettings, @NonNull AnalyticsServiceProvider analyticsServiceProvider);

  /**
   * When running, the task delegates to {@link RootContentProcessingTaskHelper#collectRootNavigationsForTask(AbstractRootContentProcessingTask)}
   */
  @Override
  public final void run() {
    rootContentProcessingTaskHelper.collectRootNavigationsForTask(this);
  }

}
