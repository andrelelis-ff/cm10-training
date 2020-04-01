package com.coremedia.blueprint.analytics.elastic.rest;

public class AlxData {

  private String key;
  private long value;

  public AlxData(String key, Long value) {
    this.key = key;
    this.value = value != null ? value : 0;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }
}
