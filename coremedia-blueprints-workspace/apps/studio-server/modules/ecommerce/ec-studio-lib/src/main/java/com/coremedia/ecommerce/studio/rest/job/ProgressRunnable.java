package com.coremedia.ecommerce.studio.rest.job;

import com.coremedia.rest.cap.jobs.JobContext;
import org.springframework.scheduling.TaskScheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

class ProgressRunnable implements Runnable {
  private JobContext jobContext;
  private ScheduledFuture<?> scheduledFuture;
  private TaskScheduler taskScheduler;
  private float currentProgress = 0;

  ProgressRunnable(JobContext jobContext, TaskScheduler taskScheduler) {
    this.jobContext = jobContext;
    this.taskScheduler = taskScheduler;
  }

  @Override
  public void run() {
    if (currentProgress < 0.99) {
      currentProgress = currentProgress + (1 - currentProgress) / 4;
      jobContext.notifyProgress(currentProgress);
    }
    schedule();
  }

  /**
   * Schedule execution of this runnable. It's synchronized so that we can be sure that scheduledFuture is set when
   * returning from this call
   */
  synchronized void schedule() {
    if (scheduledFuture != null && scheduledFuture.isCancelled()) {
      return;
    }

    long delayInMillis = 500L;
    Date startTime = new Date(System.currentTimeMillis() + delayInMillis);
    scheduledFuture = taskScheduler.schedule(this, startTime);
  }

  void cancel() {
    scheduledFuture.cancel(true);
  }
}
