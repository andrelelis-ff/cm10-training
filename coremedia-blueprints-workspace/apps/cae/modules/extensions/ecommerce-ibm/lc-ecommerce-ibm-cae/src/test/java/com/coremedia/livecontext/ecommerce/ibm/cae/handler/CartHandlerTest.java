package com.coremedia.livecontext.ecommerce.ibm.cae.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.order.Cart;
import com.coremedia.livecontext.ecommerce.order.CartService;
import com.coremedia.objectserver.web.HttpError;
import com.coremedia.objectserver.web.links.LinkFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartHandlerTest {

  private static final String CONTEXT_NAME = "anyChannelName";

  @InjectMocks
  private CartHandler testling;

  @Mock
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  @Mock
  private WcsUrlProvider checkoutRedirectPropertyProvider;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @SuppressWarnings("unused") // required by cart handler
  @Spy
  private LinkFormatter linkFormatter;

  @Mock
  private CartService cartService;

  private StoreContext storeContext;

  @Before
  public void beforeEachTest() {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);

    storeContext = IbmStoreContextBuilder.from(commerceConnection, "any-site-id").build();

    when(commerceConnection.getCartService()).thenReturn(Optional.of(cartService));

    CurrentStoreContext.set(storeContext);
  }

  @After
  public void tearDown() {
    CurrentStoreContext.remove();
  }

  @Test
  public void testHandleRequestNoContextFound() {
    UriComponentsBuilder testUrlBuilder = UriComponentsBuilder.fromUriString("checkoutUrl");
    UriComponents testUrl = testUrlBuilder.build();
    when(checkoutRedirectPropertyProvider.provideValue(any(Map.class), any(), any())).thenReturn(Optional.of(testUrl));

    View modelAndView = testling.handleRequest(CONTEXT_NAME, mock(HttpServletRequest.class),
            mock(HttpServletResponse.class));

    assertTrue(modelAndView instanceof RedirectView);
    assertEquals(((RedirectView) modelAndView).getUrl(), testUrl.toString());
  }

  @Test
  public void testHandleFragmentRequest() {
    Navigation context = mock(Navigation.class);

    configureContext(context);

    Cart resolvedCart = mock(Cart.class);
    configureResolveCart(resolvedCart);

    String viewName = "viewName";

    ModelAndView modelAndView = testling.handleFragmentRequest(CONTEXT_NAME, viewName);

    checkCartServiceIsUsedCorrectly();

    checkModelContainsCartAndNavigation(resolvedCart, context, modelAndView);

    checkViewName(viewName, modelAndView);
  }

  @Test
  public void testHandleFragmentRequestNoContext() {
    configureContext(null);
    String viewName = "viewName";
    ModelAndView modelAndView = testling.handleFragmentRequest(CONTEXT_NAME, viewName);
    checkSelfIsHttpError(modelAndView);
  }

  @Test
  public void testHandleAjaxRequestDeleteOrderItem() {
    String orderItemId = "12";
    configureRequestParameter(request, "orderItemId", orderItemId);
    Cart cart = mock(Cart.class);
    configureResolveCart(cart);
    Cart.OrderItem orderItem = mock(Cart.OrderItem.class);
    configureCartFindOrderItem(cart, orderItemId, orderItem);

    Object result = testling.handleAjaxRequest(CartHandler.ACTION_REMOVE_ORDER_ITEM, request, response);

    checkCartServiceIsUsedCorrectly();
    verifyCartDeleteOrderItem(orderItemId);
    assertEquals(Collections.emptyMap(), result);
  }

  //Is this really the expected behavior?!
  //Action was found but has invalid parameters...
  @Test(expected = CartHandler.NotFoundException.class)
  public void testHandleAjaxRequestNoOrderItemId() {
    String orderItemId = null;
    configureRequestParameter(request, "orderItemId", orderItemId);
    Cart cart = mock(Cart.class);
    configureResolveCart(cart);
    Cart.OrderItem orderItem = mock(Cart.OrderItem.class);
    configureCartFindOrderItem(cart, orderItemId, orderItem);

    testling.handleAjaxRequest(CartHandler.ACTION_REMOVE_ORDER_ITEM, request, response);

    checkCartServiceIsUsedCorrectly();
  }

  //Is this really the expected behavior?!
  //Action was found but has invalid parameters...
  @Test(expected = CartHandler.NotFoundException.class)
  public void testHandleAjaxRequestNoOrderItem() {
    String orderItemId = "12";
    configureRequestParameter(request, "orderItemId", orderItemId);
    Cart cart = mock(Cart.class);
    configureResolveCart(cart);
    Cart.OrderItem orderItem = null;
    configureCartFindOrderItem(cart, orderItemId, orderItem);

    testling.handleAjaxRequest(CartHandler.ACTION_REMOVE_ORDER_ITEM, request, response);

    checkCartServiceIsUsedCorrectly();
  }

  @Test(expected = CartHandler.NotFoundException.class)
  public void testHandleAjaxRequestUnsupportedAction() {
    testling.handleAjaxRequest("AnyInvalidAction", mock(HttpServletRequest.class), response);
    checkCartServiceIsUsedCorrectly();
  }

  //Mock Configurations

  private void configureCartFindOrderItem(Cart cart, String orderItemId, Cart.OrderItem orderItem) {
    when(cart.findOrderItemById(orderItemId)).thenReturn(orderItem);
  }

  private void configureRequestParameter(HttpServletRequest request, String parameterKey, String parameterValue) {
    when(request.getParameter(parameterKey)).thenReturn(parameterValue);
  }

  private void configureResolveCart(Cart cart) {
    when(cartService.getCart(any(StoreContext.class))).thenReturn(cart);
  }

  private void configureContext(Navigation navigation) {
    when(navigationSegmentsUriHelper.parsePath(ArgumentMatchers.eq(Collections.singletonList(CONTEXT_NAME))))
            .thenReturn(navigation);
  }

  //Checks and Verifies...

  private void checkViewName(String viewName, ModelAndView modelAndView) {
    String actualViewName = modelAndView.getViewName();
    assertEquals(viewName, actualViewName);
  }

  private void checkModelContainsCartAndNavigation(Cart expectedCart, Navigation context, ModelAndView modelAndView) {
    Map<String, Object> model = modelAndView.getModel();
    Object self = model.get("self");
    assertTrue(self instanceof Cart);
    assertSame(expectedCart, self);
    Object navigation = model.get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION);
    assertSame(context, navigation);
  }

  private void checkCartServiceIsUsedCorrectly() {
    verify(cartService, times(1)).getCart(storeContext);
  }

  private void checkSelfIsHttpError(ModelAndView modelAndView) {
    assertTrue(modelAndView.getModel().get("self") instanceof HttpError);
  }

  private void verifyCartDeleteOrderItem(String orderItemId) {
    verify(cartService, times(1)).deleteCartOrderItem(orderItemId, storeContext);
  }
}
