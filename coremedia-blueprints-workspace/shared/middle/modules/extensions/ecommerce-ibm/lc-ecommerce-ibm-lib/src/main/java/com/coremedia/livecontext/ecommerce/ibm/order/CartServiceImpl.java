package com.coremedia.livecontext.ecommerce.ibm.order;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.order.Cart;
import com.coremedia.livecontext.ecommerce.order.CartService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.CART;
import static com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider.commerceId;

public class CartServiceImpl implements CartService {

  private WcCartWrapperService cartWrapperService;
  private CommerceBeanFactory commerceBeanFactory;

  @Override
  public Cart getCart(@NonNull StoreContext context) {
    WcCart wcCart = cartWrapperService.getCart(CurrentUserContext.get(), context);
    return createCartBeanFor(wcCart, context);
  }

  @Override
  public void deleteCartOrderItem(String orderItemId, @NonNull StoreContext context) {
    updateCartOrderItem(orderItemId, BigDecimal.ZERO, context);
  }

  @Override
  public void updateCartOrderItem(String orderItemId, BigDecimal newQuantity, @NonNull StoreContext context) {
    updateCart(Collections.singletonList(new OrderItemParam(orderItemId, newQuantity)), context);
  }

  @Override
  public void updateCart(Iterable<OrderItemParam> orderItems, @NonNull StoreContext context) {
    WcUpdateCartParam wcUpdateCartParam = new WcUpdateCartParam();
    List<WcUpdateCartParam.OrderItem> wcUpdateOrderItems = new ArrayList<>();
    for (OrderItemParam orderItemToUpdate : orderItems) {
      wcUpdateOrderItems.add(new WcUpdateCartParam.OrderItem(orderItemToUpdate.getExternalId(), orderItemToUpdate.getQuantity().toPlainString()));
    }
    wcUpdateCartParam.setOrderItem(wcUpdateOrderItems);
    cartWrapperService.updateCart(CurrentUserContext.get(), context, wcUpdateCartParam);
  }

  @Override
  public void addToCart(Iterable<OrderItemParam> orderItems, @NonNull StoreContext context) {
    WcAddToCartParam wcAddToCartParam = new WcAddToCartParam();
    List<WcAddToCartParam.OrderItem> wcAddToOrderItems = new ArrayList<>();
    for (OrderItemParam orderItem : orderItems) {
      wcAddToOrderItems.add(new WcAddToCartParam.OrderItem(orderItem.getExternalId(), orderItem.getQuantity().toPlainString()));
    }
    wcAddToCartParam.setOrderItem(wcAddToOrderItems);
    cartWrapperService.addToCart(CurrentUserContext.get(), context, wcAddToCartParam);
  }

  @Override
  public void cancelCart(@NonNull StoreContext context) {
    cartWrapperService.cancelCart(CurrentUserContext.get(), context);
  }

  private Cart createCartBeanFor(WcCart cartWrapper, StoreContext context) {
    if (cartWrapper == null) {
      // no wcs cart == empty cart
      return new CartImpl();
    }
    CommerceId commerceId = commerceId(CART).withExternalId(cartWrapper.getBuyerId()).build();
    Cart cart = (Cart) commerceBeanFactory.createBeanFor(commerceId, context);
    ((AbstractIbmCommerceBean) cart).setDelegate(cartWrapper);
    return cart;
  }

  public WcCartWrapperService getCartWrapperService() {
    return cartWrapperService;
  }

  @Required
  public void setCartWrapperService(WcCartWrapperService cartWrapperService) {
    this.cartWrapperService = cartWrapperService;
  }

  public CommerceBeanFactory getCommerceBeanFactory() {
    return commerceBeanFactory;
  }

  @Required
  public void setCommerceBeanFactory(CommerceBeanFactory commerceBeanFactory) {
    this.commerceBeanFactory = commerceBeanFactory;
  }
}
