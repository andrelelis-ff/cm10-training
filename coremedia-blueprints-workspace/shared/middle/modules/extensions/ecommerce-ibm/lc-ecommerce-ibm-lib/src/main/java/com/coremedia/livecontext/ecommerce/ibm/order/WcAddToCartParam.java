package com.coremedia.livecontext.ecommerce.ibm.order;

import java.util.List;

/**
 * Parameter model for REST update shopping cart call
 */
public class WcAddToCartParam {

  private List<OrderItem> orderItem;

  public WcAddToCartParam() {
  }

  public List<OrderItem> getOrderItem() {
    return orderItem;
  }

  public void setOrderItem(List<OrderItem> orderItem) {
    this.orderItem = orderItem;
  }

  public static class OrderItem {

    private String productId;
    private String quantity;

    public OrderItem(String productId, String quantity) {
      this.productId = productId;
      this.quantity = quantity;
    }

    public String getProductId() {
      return productId;
    }

    public String getQuantity() {
      return quantity;
    }
  }
}
