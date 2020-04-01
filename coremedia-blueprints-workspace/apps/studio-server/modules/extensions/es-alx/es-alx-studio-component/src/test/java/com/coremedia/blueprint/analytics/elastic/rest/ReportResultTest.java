package com.coremedia.blueprint.analytics.elastic.rest;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ReportResultTest {

  @Mock
  private AlxData alxData;

  @Test
  public void alxPageViewResult() {
    Date timeStamp = new Date();
    List<AlxData> alxDataList = Arrays.asList(alxData);
    ReportResult reportResult = new ReportResult(alxDataList, timeStamp);
    assertEquals(timeStamp, reportResult.getTimeStamp());
    assertEquals(alxDataList, reportResult.getData());
    reportResult.setData(null);
    reportResult.setTimeStamp(null);
    assertEquals(null, reportResult.getTimeStamp());
    assertEquals(null, reportResult.getData());
  }
}
