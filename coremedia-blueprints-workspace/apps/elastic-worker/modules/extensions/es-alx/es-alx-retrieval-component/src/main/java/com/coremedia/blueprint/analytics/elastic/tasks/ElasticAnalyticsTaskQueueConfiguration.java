package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.elastic.core.api.tasks.configuration.TaskQueue;
import com.coremedia.elastic.core.api.tasks.configuration.TaskQueueConfiguration;
import com.coremedia.elastic.core.api.tasks.configuration.TaskQueueConfigurationBuilder;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressWarnings("UnusedDeclaration")
@Named
public class ElasticAnalyticsTaskQueueConfiguration implements TaskQueueConfiguration {

  static final long FETCH_INTERVAL = 60L * 1000L; // 1 min

  private static final String QUEUE_NAME = "elasticAnalyticsTaskQueue";

  @Inject
  private TaskQueueConfigurationBuilder builder;

  @Override
  public Iterable<TaskQueue> getTaskQueues() {
    return builder.
            configureTask(QUEUE_NAME, FetchReportsTask.class, FETCH_INTERVAL).
            configureTask(QUEUE_NAME, FetchPageViewHistoryTask.class, FETCH_INTERVAL).
            configureTask(QUEUE_NAME, FetchPublicationsHistoryTask.class, FETCH_INTERVAL).
            build();
  }
}
