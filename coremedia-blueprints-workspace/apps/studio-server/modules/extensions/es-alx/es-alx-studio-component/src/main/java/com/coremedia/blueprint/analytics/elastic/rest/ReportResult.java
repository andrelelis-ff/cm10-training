package com.coremedia.blueprint.analytics.elastic.rest;

import java.util.Date;
import java.util.List;

public class ReportResult {

  private List<AlxData> data;
  private Date timeStamp;

  public ReportResult(List<AlxData> data, Date timeStamp) {
    this.data = data;
    this.timeStamp = timeStamp == null ? null : new Date(timeStamp.getTime());
  }

  public List<AlxData> getData() {
    return data;
  }

  public void setData(List<AlxData> data) {
    this.data = data;
  }

  public Date getTimeStamp() {
    return timeStamp == null ? null : new Date(timeStamp.getTime());
  }

  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp == null ? null : new Date(timeStamp.getTime());
  }
}
