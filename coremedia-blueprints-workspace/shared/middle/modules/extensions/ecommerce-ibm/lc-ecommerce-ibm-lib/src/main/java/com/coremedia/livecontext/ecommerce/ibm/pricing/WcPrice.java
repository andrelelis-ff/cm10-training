package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;

import java.util.Map;

public class WcPrice {
  protected String priceDescription;
  protected String priceUsage;
  protected String priceValue;
  private String currency;

  protected Map<String, Object> dataMap;

  public void setDataMap(Map<String, Object> data) {
    if (data != null) {
      dataMap = data;
      currency = (String) DataMapHelper.getValueForPath(data, "EntitledPrice.UnitPrice.price.currency");
      Object valueForPath = DataMapHelper.getValueForPath(data, "EntitledPrice.UnitPrice.price.value");
      if (valueForPath != null) {
        priceValue = String.valueOf(valueForPath);
      }
    }
  }

  public String getPriceValue() {
    return priceValue != null ? priceValue : "";
  }

  public void setPriceValue(String priceValue) {
    this.priceValue = priceValue;
  }

  public String getPriceUsage() {
    return priceUsage;
  }

  public void setPriceUsage(String priceUsage) {
    this.priceUsage = priceUsage;
  }

  public String getPriceDescription() {
    return priceDescription;
  }

  public void setPriceDescription(String priceDescription) {
    this.priceDescription = priceDescription;
  }

  public void setSKUPriceDescription(String SKUPriceDescription) {
    this.priceUsage = SKUPriceDescription;
  }

  public void setSKUPriceUsage(String SKUPriceUsage) {
    this.priceUsage = SKUPriceUsage;
  }

  public void setSKUPriceValue(String SKUPriceValue) {
    this.priceValue = SKUPriceValue;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
