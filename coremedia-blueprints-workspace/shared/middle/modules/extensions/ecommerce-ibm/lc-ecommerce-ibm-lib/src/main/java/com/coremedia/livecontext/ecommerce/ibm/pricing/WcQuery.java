package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.livecontext.ecommerce.ibm.catalog.WcPartNumber;

import java.util.List;

public class WcQuery {
  String name = "byPartNumber";
  List<String> currencies;
  List<WcPartNumber> products;
  List<String> contractIds;

  public WcQuery(List<WcPartNumber> products, List<String> currencies, List<String> contractIds) {
    this.currencies = currencies;
    this.products = products;
    this.contractIds = contractIds;
  }

  public List<String> getCurrencies() {
    return currencies;
  }

  public void setCurrencies(List<String> currencies) {
    this.currencies = currencies;
  }

  public List<String> getContractIds() {
    return contractIds;
  }

  public void setContractIds(List<String> contractIds) {
    this.contractIds = contractIds;
  }

  public List<WcPartNumber> getProducts() {
    return products;
  }

  public void setProducts(List<WcPartNumber> products) {
    this.products = products;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
