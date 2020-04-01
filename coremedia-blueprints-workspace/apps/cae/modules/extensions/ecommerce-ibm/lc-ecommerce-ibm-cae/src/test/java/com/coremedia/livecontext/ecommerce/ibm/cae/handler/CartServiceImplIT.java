package com.coremedia.livecontext.ecommerce.ibm.cae.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.ibm.cae.storefront.StoreFrontConfiguration;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.order.CartServiceImpl;
import com.coremedia.livecontext.ecommerce.order.Cart;
import com.coremedia.livecontext.ecommerce.order.CartService;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = {
        IbmServiceTestBase.LocalConfig.class,
        StoreFrontConfiguration.class,
})
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
class CartServiceImplIT extends IbmServiceTestBase {

  private static final String SKU_CODE = "CLA022_220301";
  private static final String PASSWORD = "passw0rd";

  @Inject
  private CartServiceImpl testling;

  @Inject
  private CatalogServiceImpl catalogService;
  @Inject
  private UserSessionService userSessionService;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @SuppressWarnings("unused")
  @MockBean
  private WcsUrlProvider wcsUrlProvider;

  @Test
  void testGetCart() {
    if (useTapes()) {
      return;
    }

    if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
      return;
    }

    prefillCart();

    testLoginUser();

    Cart cart = testling.getCart(storeContext);
    assertNotNull(cart);
    assertEquals("1.0", cart.getOrderItems().get(0).getQuantity().toPlainString());

    //test update cart
    Cart.OrderItem orderItem = cart.getOrderItems().get(0);
    List<CartService.OrderItemParam> updateParams = new ArrayList<>();
    updateParams.add(new CartService.OrderItemParam(
            orderItem.getExternalId(),
            orderItem.getQuantity().add(BigDecimal.ONE)));
    testling.updateCart(updateParams, storeContext);
    cart = testling.getCart(storeContext);
    assertEquals("2.0", cart.getOrderItems().get(0).getQuantity().toPlainString());
    testling.cancelCart(storeContext);
  }

  private void testLoginUser() {
    CurrentUserContext.set(CurrentUserContext.get());

    HttpServletRequest request = new MockHttpServletRequest();
    HttpServletResponse response = new MockHttpServletResponse();

    boolean loginUserSuccess = userSessionService.loginUser(request, response, testConfig.getUser1Name(), PASSWORD);
    assertTrue(loginUserSuccess);
  }

  @Test
  void testUpdateCartWithAnonymousUser() {
    StoreContext storeContext = testConfig.getStoreContext(connection);

    if (useTapes() || StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
      return;
    }

    CurrentUserContext.set(CurrentUserContext.get());

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean guestIdentitySuccess = userSessionService.ensureGuestIdentity(request, response);
    assertTrue(guestIdentitySuccess);

    testling.cancelCart(storeContext);
    Cart cart = testling.getCart(storeContext);
    assertNotNull(cart);
    assertTrue(cart.getOrderItems().isEmpty());

    String orderItemId = getOrderItemId(SKU_CODE);
    assertNotNull(orderItemId);

    List<CartService.OrderItemParam> addToParams = new ArrayList<>();
    addToParams.add(new CartService.OrderItemParam(
            orderItemId,
            BigDecimal.ONE));
    testling.addToCart(addToParams, storeContext);
    cart = testling.getCart(storeContext);
    assertNotNull(cart);

    Cart.OrderItem orderItem = cart.getOrderItems().get(0);
    assertEquals("1.0", orderItem.getQuantity().toPlainString());

    List<CartService.OrderItemParam> updateParams = new ArrayList<>();
    updateParams.add(new CartService.OrderItemParam(
            orderItem.getExternalId(),
            orderItem.getQuantity().add(BigDecimal.ONE)));
    testling.updateCart(updateParams, storeContext);
    cart = testling.getCart(storeContext);
    orderItem = cart.getOrderItems().get(0);
    assertEquals("2.0", orderItem.getQuantity().toPlainString());

    testling.cancelCart(storeContext);
  }

  private void prefillCart() {
    testLoginUser();

    Cart cart = testling.getCart(storeContext);
    assertNotNull(cart);

    String orderItemId = getOrderItemId(SKU_CODE);
    assertNotNull(orderItemId);

    List<CartService.OrderItemParam> updateParams = new ArrayList<>();
    updateParams.add(new CartService.OrderItemParam(
            orderItemId,
            BigDecimal.ONE));
    testling.addToCart(updateParams, storeContext);
    cart = testling.getCart(storeContext);
    assertNotNull(cart);
    assertEquals("1.0", cart.getOrderItems().get(0).getQuantity().toPlainString());
  }

  private String getOrderItemId(String id) {
    CommerceId productVariantId = ibmCommerceIdProvider.formatProductVariantId(storeContext.getCatalogAlias(), id);
    Product product = catalogService.findProductById(productVariantId, storeContext);
    if (product != null) {
      return product.getExternalTechId();
    }

    return null;
  }
}
