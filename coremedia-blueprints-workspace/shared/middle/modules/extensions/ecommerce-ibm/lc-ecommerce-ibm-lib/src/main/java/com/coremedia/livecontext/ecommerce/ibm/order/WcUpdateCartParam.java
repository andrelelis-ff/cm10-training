package com.coremedia.livecontext.ecommerce.ibm.order;

import java.util.List;

/**
 * Parameter model for REST update shopping cart call
 */
public class WcUpdateCartParam {

  private List<OrderItem> orderItem;

  public WcUpdateCartParam() {
  }

  public List<OrderItem> getOrderItem() {
    return orderItem;
  }

  public void setOrderItem(List<OrderItem> orderItem) {
    this.orderItem = orderItem;
  }

  public static class OrderItem {

    private String orderItemId;
    private String quantity;

    public OrderItem(String orderItemId, String quantity) {
      this.orderItemId = orderItemId;
      this.quantity = quantity;
    }

    public String getOrderItemId() {
      return orderItemId;
    }

    public String getQuantity() {
      return quantity;
    }
  }
}
