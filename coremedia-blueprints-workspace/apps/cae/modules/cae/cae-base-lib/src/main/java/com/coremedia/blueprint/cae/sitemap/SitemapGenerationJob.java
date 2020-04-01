package com.coremedia.blueprint.cae.sitemap;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SitemapGenerationJob implements SmartLifecycle {
  private static final Logger LOG = LoggerFactory.getLogger(SitemapGenerationJob.class);

  private static final int DAILY = 24 * 60;

  // formats of the blueprint.sitemap.starttime property value
  private static final String STARTTIME_DISABLE = "-";
  private static final String STARTTIME_RELATIVE_PREFIX = "+";
  private static final String STARTTIME_TIME_OF_DAY_FORMAT = "HH:mm";
  private static final int DISABLED = -1;
  private static final int FAILURE = -2;

  private ScheduledExecutorService executor;
  private String startTime = STARTTIME_DISABLE;
  private long periodMinutes = DAILY;
  private long counter = 0;

  private SitemapTrigger sitemapTrigger;


  // --- configuration ----------------------------------------------

  /**
   * Initial time of day to start sitemap generation.
   * <p>
   * Supported formats:
   * "23:45": time of day, 24h, timezone of the host (recommended for production)
   * "+10": minutes after CAE start (useful for testsystems)
   * "-": disable periodic sitemap generation (useful for development)
   * <p>
   * Defaults to "-", i.e. disabled.
   */
  public void setStartTime(String startTime) {
    Objects.requireNonNull(startTime);
    this.startTime = startTime.trim();
  }

  /**
   * The period for sitemap generation.
   * <p>
   * Defaults to 1440 (once a day).
   * <p>
   * Should be a divisor of 1440 (minutes of a day), otherwise
   * sitemap generation would run at different times each day.
   * Should be not too small, since sitemap generation is costly.
   */
  public void setPeriodMinutes(long periodMinutes) {
    if (periodMinutes<=0) {
      throw new IllegalArgumentException("periodMinutes must be > 0.");
    }
    this.periodMinutes = periodMinutes;
  }

  @Required
  public void setSitemapTrigger(SitemapTrigger sitemapTrigger) {
    this.sitemapTrigger = sitemapTrigger;
  }


  // --- SmartLifecycle ---------------------------------------------

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    try {
      stop();
    } finally {
      callback.run();
    }
  }

  @Override
  public synchronized void start() {
    LOG.info("Schedule sitemap generation");
    if (executor==null) {
      executor = Executors.newScheduledThreadPool(1);
      try {
        scheduleSitemapGeneration(executor);
      } catch (Exception e) {
        LOG.error("SitemapGenerationJob: Miserable failure!", e);
      }
    }
  }

  @Override
  public synchronized void stop() {
    LOG.info("Shutdown sitemap generation");
    if (executor!=null) {
      executor.shutdownNow();
      executor = null;
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return executor!=null && !executor.isTerminated();
  }

  @Override
  public int getPhase() {
    return 0;
  }


  // --- internal ---------------------------------------------------

  private void scheduleSitemapGeneration(ScheduledExecutorService scheduler) {
    final Runnable job = new Runnable() {
      public void run() {
        LOG.info("Start sitemap generation job #{}", ++counter);
        try {
          sitemapTrigger.generateSitemaps();
        } catch (Exception e) {
          LOG.error("Sitemap generation job #{} failed.", counter, e);
        }
        LOG.info("Finished sitemap generation job #{}", counter);
      }
    };

    int initialDelay = initialDelayMinutes();
    if (initialDelay>=0) {
      LOG.info("First sitemap generation job will start in {} minutes.", initialDelay);
      scheduler.scheduleAtFixedRate(job, initialDelay, periodMinutes, TimeUnit.MINUTES);
    } else {
      LOG.info("Periodic sitemap generation is disabled. You can still manually trigger sitemap generation by the sitemap generation URL.");
    }
  }

  @VisibleForTesting
  int initialDelayMinutes() {
    try {
      if (STARTTIME_DISABLE.equals(startTime)) {
        // disabled
        return DISABLED;
      } else if (startTime.startsWith(STARTTIME_RELATIVE_PREFIX)) {
        // relative, minutes to delay
        return Integer.parseInt(startTime.substring(STARTTIME_RELATIVE_PREFIX.length()));
      } else {
        // absolute, time of day
        return minutesUntilStarttime(now());
      }
    } catch (Exception e) {
      LOG.error("Cannot evaluate sitemap generation start time {}", startTime, e);
      return FAILURE;
    }
  }

  /**
   * @param from must always be {@link #now()} in production use, only parameterized for testing.
   */
  @VisibleForTesting
  int minutesUntilStarttime(Calendar from) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat(STARTTIME_TIME_OF_DAY_FORMAT);
    Date date = dateFormat.parse(startTime);
    return minutesUntil(from, date);
  }

  private static int minutesUntil(Calendar fromTime, Date toTime) {
    Calendar calTo = Calendar.getInstance();
    calTo.setTime(toTime);
    int minuteOfDayTo = calTo.get(Calendar.HOUR_OF_DAY) * 60 + calTo.get(Calendar.MINUTE);

    int minuteOfDayFrom = fromTime.get(Calendar.HOUR_OF_DAY) * 60 + fromTime.get(Calendar.MINUTE);

    int result = minuteOfDayTo - minuteOfDayFrom;
    if (result<0) {
      result += 24 * 60;
    }
    return result;
  }

  private static Calendar now() {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(System.currentTimeMillis());
    return now;
  }
}
