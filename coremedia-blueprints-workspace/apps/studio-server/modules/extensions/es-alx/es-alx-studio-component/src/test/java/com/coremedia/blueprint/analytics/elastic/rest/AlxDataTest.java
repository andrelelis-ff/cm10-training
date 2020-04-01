package com.coremedia.blueprint.analytics.elastic.rest;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.coremedia.blueprint.base.analytics.elastic.ReportModel.REPORT_DATE_FORMAT;
import static org.junit.Assert.assertEquals;

public class AlxDataTest {

  @Test
  public void test() {
    Date today = new Date();
    DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
    String dateString = dateFormat.format(today);
    AlxData data = new AlxData(dateString, 3L);
    assertEquals(dateString, data.getKey());
    assertEquals(3, data.getValue());
    data.setKey("20130416");
    data.setValue(12);
    assertEquals("20130416", data.getKey());
    assertEquals(12, data.getValue());
  }

  @Test
  public void testWithNull() {
    Date today = new Date();
    DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
    String dateString = dateFormat.format(today);
    AlxData data = new AlxData(dateString, null);
    assertEquals(dateString, data.getKey());
    assertEquals(0, data.getValue());
  }
}
