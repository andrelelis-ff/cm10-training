
package com.coremedia.livecontext.ecommerce.ibm.order;

import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.order.Cart;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartImpl extends AbstractIbmCommerceBean implements Cart {

  private WcCart delegate;
  private List<OrderItem> orderItems = Collections.emptyList();

  public WcCart getDelegate() {
    return delegate;
  }

  @Override
  public void setDelegate(Object delegate) {
    this.delegate = (WcCart) delegate;
    if (delegate == null || this.delegate.getOrderItem() == null) {
      orderItems = Collections.emptyList();
    } else {
      orderItems = new ArrayList<>(this.delegate.getOrderItem().size());
      for (WcOrderItem wcOrderItem :this.delegate.getOrderItem()) {
        orderItems.add(new OrderItemImpl(wcOrderItem));
      }
    }
  }

  @Override
  public List<OrderItem> getOrderItems() {
    return orderItems;
  }

  @Override
  public BigDecimal getTotalQuantity() {
    BigDecimal totalQuantity = new BigDecimal(0);

    if (orderItems != null) {
      for (OrderItem orderItem : orderItems) {
        totalQuantity = totalQuantity.add(orderItem.getQuantity());
      }
    }

    return totalQuantity;
  }

  @Override
  public OrderItem findOrderItemById(String orderItemId) {
    //todo write unit test
    for (OrderItem orderItem :getOrderItems()) {
      if (orderItemId.equals(orderItem.getExternalId())) {
        return orderItem;
      }
    }
    return null;
  }

  /**
   *
   * @return may return null if the cart is empty (when there is no cart in the WCS system for the current user)
   */
  @Override
  public String getExternalId() {
    WcCart wcCart = getDelegate();
    if (wcCart == null) {
      return null;
    }
    return wcCart.getBuyerDistinguishedName() + "-" + "cart"; //todo what to return here?
  }

  /**
   *
   * @return may return null if the cart is empty (when there is no cart in the WCS system for the current user)
   */
  @Override
  public String getExternalTechId() {
    WcCart wcCart = getDelegate();
    if (wcCart == null) {
      return null;
    }
    return wcCart.getBuyerId() + "-" + wcCart.getOrderId();
  }

  public class OrderItemImpl implements OrderItem {

    private WcOrderItem delegate;

    public OrderItemImpl(WcOrderItem delegate) {
      this.delegate = delegate;
    }

    public WcOrderItem getDelegate() {
      return delegate;
    }

    @Override
    public String getExternalId() {
      return getDelegate().getOrderItemId();
    }

    @Override
    public BigDecimal getQuantity() {
      return NumberUtils.createBigDecimal(getDelegate().getQuantity());
    }

    @Override
    public BigDecimal getUnitPrice() {
      return NumberUtils.createBigDecimal(getDelegate().getUnitPrice());
    }

    @Override
    public Product getProduct() {
      CommerceId productId = getCommerceIdProvider().formatProductTechId(getCatalogAlias(), getDelegate().getProductId());
      return getCatalogService().findProductById(productId, getContext());
    }

    @Override
    public BigDecimal getPrice() {
      return convertStringToBigDecimal(getDelegate().getOrderItemPrice());
    }
  }

  //todo copied from ProductImpl, move to some helper class
  private static BigDecimal convertStringToBigDecimal(String value) {
    if (NumberUtils.isNumber(value)) {
      return NumberUtils.createBigDecimal(value);
    }
    return null;
  }
}
