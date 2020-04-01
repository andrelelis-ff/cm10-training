package com.coremedia.livecontext.ecommerce.ibm.common;

/**
 * Common attributes for a list of results.
 */
public class WcTopAttributes {
  private boolean recordSetComplete;
  private int recordSetCount;
  private int recordSetStartNumber;
  private int recordSetTotal;
  private String resourceId;
  private String resourceName;

  public boolean isRecordSetComplete() {
    return recordSetComplete;
  }

  public void setRecordSetComplete(boolean recordSetComplete) {
    this.recordSetComplete = recordSetComplete;
  }

  public int getRecordSetCount() {
    return recordSetCount;
  }

  public void setRecordSetCount(int recordSetCount) {
    this.recordSetCount = recordSetCount;
  }

  public int getRecordSetStartNumber() {
    return recordSetStartNumber;
  }

  public void setRecordSetStartNumber(int recordSetStartNumber) {
    this.recordSetStartNumber = recordSetStartNumber;
  }

  public int getRecordSetTotal() {
    return recordSetTotal;
  }

  public void setRecordSetTotal(int recordSetTotal) {
    this.recordSetTotal = recordSetTotal;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
}
