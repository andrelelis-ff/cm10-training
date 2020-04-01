package com.coremedia.blueprint.cae.sitemap;

import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SitemapGenerationJobTest {
  private static Calendar timeOfDay1121;

  @BeforeClass
  public static void setNow1121() {
    timeOfDay1121 = Calendar.getInstance();
    timeOfDay1121.set(2015, Calendar.JULY, 23, 11, 21);
  }

  @Test
  public void testStarttimeDisabled() {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("-");
    int initialDelay = testling.initialDelayMinutes();
    assertEquals(-1, initialDelay);
  }

  @Test
  public void testStarttimeRelative() {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("+42");
    int initialDelay = testling.initialDelayMinutes();
    assertEquals(42, initialDelay);
  }

  @Test
  public void testStarttimeAbsolute() throws ParseException {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("01:30");
    int initialDelay = testling.initialDelayMinutes();;
    // Cannot check more concrete, since we cannot control the current time.
    assertTrue(initialDelay >= 0);
  }

  @Test
  public void testStarttimeAbsoluteToday() throws ParseException {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("23:45");
    int initialDelay = testling.minutesUntilStarttime(timeOfDay1121);
    assertEquals(744, initialDelay);
  }

  @Test
  public void testStarttimeAbsoluteTomorrow() throws ParseException {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("01:30");
    int initialDelay = testling.minutesUntilStarttime(timeOfDay1121);
    assertEquals(849, initialDelay);
  }

  @Test
  public void testStarttimeAbsoluteMidnight() throws ParseException {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("00:00");
    int initialDelay = testling.minutesUntilStarttime(timeOfDay1121);
    assertEquals(759, initialDelay);
  }

  @Test
  public void testStarttimeRobustness() {
    SitemapGenerationJob testling = new SitemapGenerationJob();
    testling.setStartTime("bullshit");
    int initialDelay = testling.initialDelayMinutes();
    assertEquals(-2, initialDelay);
  }
}
