package com.coremedia.livecontext.ecommerce.ibm.order;

import java.util.List;

public class WcCart {

  private String buyerDistinguishedName,
          buyerId,
          grandTotal,
          grandTotalCurrency,
          lastUpdateDate,
          orderStatus,
          prepareIndicator,
          recordSetComplete,
          recordSetCount,
          recordSetStartNumber,
          recordSetTotal,
          orderId;

  private List<WcOrderItem> orderItem;

  public String getOrderId() {
    return orderId;
  }

  public String getBuyerDistinguishedName() {
    return buyerDistinguishedName;
  }

  public String getBuyerId() {
    return buyerId;
  }

  public String getGrandTotal() {
    return grandTotal;
  }

  public String getGrandTotalCurrency() {
    return grandTotalCurrency;
  }

  public String getLastUpdateDate() {
    return lastUpdateDate;
  }

  public String getOrderStatus() {
    return orderStatus;
  }

  public String getPrepareIndicator() {
    return prepareIndicator;
  }

  public String getRecordSetComplete() {
    return recordSetComplete;
  }

  public String getRecordSetCount() {
    return recordSetCount;
  }

  public String getRecordSetStartNumber() {
    return recordSetStartNumber;
  }

  public String getRecordSetTotal() {
    return recordSetTotal;
  }

  public List<WcOrderItem> getOrderItem() {
    return orderItem;
  }

  public String getId() {
    return getBuyerId() + "-" + getOrderId();
  }
}
