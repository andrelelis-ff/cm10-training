package com.coremedia.livecontext.ecommerce.ibm.cae.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.order.Cart;
import com.coremedia.livecontext.ecommerce.order.CartService;
import com.coremedia.livecontext.ecommerce.order.CartService.OrderItemParam;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.ContentTypes.CONTENT_TYPE_JSON;
import static com.coremedia.blueprint.base.links.UriConstants.RequestParameters.TARGETVIEW_PARAMETER;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.base.links.UriConstants.Views.VIEW_FRAGMENT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * Handler for Commerce carts.
 */
@Link
@RequestMapping
public class CartHandler extends LiveContextPageHandlerBase {

  protected static final String URI_PREFIX = "cart";

  /**
   * URI pattern, for URIs like "/service/cart/shopName"
   */
  public static final String URI_PATTERN =
          '/' + PREFIX_SERVICE +
                  '/' + URI_PREFIX +
                  "/{" + SEGMENT_ROOT + '}';

  /**
   * URI pattern, for URIs like "/dynamic/fragment/cart/shopName"
   */
  public static final String DYNAMIC_URI_PATTERN =
          '/' + PREFIX_DYNAMIC +
                  '/' + SEGMENTS_FRAGMENT +
                  '/' + URI_PREFIX +
                  "/{" + SEGMENT_ROOT + '}';

  private static final String PARAM_ACTION = "action";

  @VisibleForTesting
  static final String ACTION_REMOVE_ORDER_ITEM = "removeOrderItem";
  private static final String ORDER_ITEM_ID = "orderItemId";

  private static final String ACTION_ADD_ORDER_ITEM = "addOrderItem";
  private static final String EXTERNAL_TECH_ID = "externalTechId";

  private WcsUrlProvider checkoutRedirectUrlProvider;

  private LinkFormatter linkFormatter;

  @Substitution("cart")
  public Cart getCart() {
    return new LazyCart();
  }

  // --- Handlers ------------------------------------------------------------------------------------------------------

  @GetMapping(value = URI_PATTERN)
  public View handleRequest(@PathVariable(SEGMENT_ROOT) String context, HttpServletRequest request,
                            HttpServletResponse response) {
    StoreContext storeContext = CurrentStoreContext.find().orElse(null);
    if (storeContext == null) {
      return null;
    }

    Map<String, Object> params = new HashMap<>();
    params.put(URL_PROVIDER_IS_STUDIO_PREVIEW, isStudioPreview(request));

    String checkoutUrl = checkoutRedirectUrlProvider.provideValue(params, request, storeContext)
            .map(UriComponents::toString)
            .orElse(null);
    if (checkoutUrl == null) {
      return null;
    }

    String redirectUrl = applyLinkTransformers(checkoutUrl, request, response);

    if (redirectUrl.startsWith("//")) {
      String scheme = request.getScheme();
      redirectUrl = scheme + ":" + redirectUrl;
    }

    return new RedirectView(redirectUrl);
  }

  private String applyLinkTransformers(@NonNull String source, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    String result = source;
    List<LinkTransformer> transformers = linkFormatter.getTransformers();
    for (LinkTransformer transformer : transformers) {
      result = transformer.transform(result, null, null, request, response, true);
    }
    return result;
  }

  @GetMapping(value = DYNAMIC_URI_PATTERN)
  public ModelAndView handleFragmentRequest(@PathVariable(SEGMENT_ROOT) String context,
                                            @RequestParam(value = TARGETVIEW_PARAMETER, required = false) String view) {
    // If no context is available: return "not found".

    Navigation navigation = getNavigation(context);
    if (navigation == null) {
      return HandlerHelper.notFound();
    }

    Cart cart = resolveCart();
    if (cart == null) {
      return HandlerHelper.notFound();
    }

    // Add navigationContext as navigationContext request param.
    ModelAndView modelWithView = HandlerHelper.createModelWithView(cart, view);
    NavigationLinkSupport.setNavigation(modelWithView, navigation);
    return modelWithView;
  }

  @PostMapping(value = DYNAMIC_URI_PATTERN, produces = {CONTENT_TYPE_JSON})
  @ResponseBody
  public Object handleAjaxRequest(@RequestParam(value = PARAM_ACTION, required = true) String action,
                                  HttpServletRequest request, HttpServletResponse response) {
    switch (action) {
      case ACTION_REMOVE_ORDER_ITEM:
        return handleRemoveOrderItem(request);
      case ACTION_ADD_ORDER_ITEM:
        return handleAddOrderItem(request, response);
      default:
        throw new NotFoundException("Unsupported action: " + action);
    }
  }

  @NonNull
  private Object handleRemoveOrderItem(@NonNull HttpServletRequest request) {
    Cart cart = resolveCart();
    if (cart == null) {
      return emptyMap();
    }

    String orderItemId = request.getParameter(ORDER_ITEM_ID);

    if (!orderItemExist(cart, orderItemId)) {
      throw new NotFoundException("Cannot remove order item with ID '" + orderItemId + "' from cart.");
    }

    deleteCartOrderItem(orderItemId);

    return emptyMap();
  }

  @NonNull
  private Object handleAddOrderItem(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    boolean guestIdentityEnsured = getUserSessionService()
            .map(userSessionService -> userSessionService.ensureGuestIdentity(request, response))
            .orElse(false);

    if (!guestIdentityEnsured) {
      throw new NotFoundException("Cannot switch to guest state");
    }

    String externalTechId = request.getParameter(EXTERNAL_TECH_ID);
    addCartOrderItem(externalTechId);

    return emptyMap();
  }

  private void deleteCartOrderItem(String orderItemId) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection commerceConnection = storeContext.getConnection();

    CartService cartService = getCartService(commerceConnection);

    cartService.deleteCartOrderItem(orderItemId, storeContext);
  }

  private void addCartOrderItem(String orderItemId) {
    BigDecimal quantity = BigDecimal.valueOf(1);
    OrderItemParam orderItem = new OrderItemParam(orderItemId, quantity);

    List<OrderItemParam> orderItems = singletonList(orderItem);

    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection commerceConnection = storeContext.getConnection();

    CartService cartService = getCartService(commerceConnection);

    cartService.addToCart(orderItems, storeContext);
  }

  private static boolean orderItemExist(@NonNull Cart cart, @Nullable String orderItemId) {
    return orderItemId != null && cart.findOrderItemById(orderItemId) != null;
  }

  @NonNull
  private static Optional<UserSessionService> getUserSessionService() {
    return CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .flatMap(CommerceConnection::getUserSessionService);
  }

  @NonNull
  private static CartService getCartService(@NonNull CommerceConnection connection) {
    return connection.getCartService()
            .orElseThrow(() -> new IllegalStateException("Cart service is not available."));
  }

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  public static class NotFoundException extends RuntimeException {

    public NotFoundException(String msg) {
      super(msg);
    }
  }

  // --- LinkSchemes ---------------------------------------------------------------------------------------------------

  /**
   * Builds a generic action link for a {@link CMAction}.
   */
  @SuppressWarnings({"TypeMayBeWeakened", "UnusedParameters"})
  @Link(type = Cart.class, uri = URI_PATTERN)
  @NonNull
  public UriComponents buildLink(Cart cart, UriTemplate uriPattern, Map<String, Object> linkParameters,
                                 HttpServletRequest request) {
    return buildLinkInternal(uriPattern, linkParameters);
  }

  @Link(type = Cart.class, view = VIEW_FRAGMENT, uri = DYNAMIC_URI_PATTERN)
  @NonNull
  public UriComponents buildFragmentLink(Cart cart, UriTemplate uriPattern, Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    return buildLinkInternal(uriPattern, linkParameters);
  }

  @Link(type = Cart.class, view = "ajax", uri = DYNAMIC_URI_PATTERN)
  @NonNull
  public UriComponents buildDeleteCartOderItemLink(Cart cart, UriTemplate uriPattern,
                                                   Map<String, Object> linkParameters, HttpServletRequest request) {
    return buildLinkInternal(uriPattern, linkParameters);
  }

  @NonNull
  private UriComponents buildLinkInternal(@NonNull UriTemplate uriPattern, @NonNull Map<String, Object> linkParameters) {
    Navigation context = getContextHelper().currentSiteContext();
    String firstNavigationPathSegment = getPathSegments(context).get(0);

    Map<String, String> uriVariables = ImmutableMap.of(SEGMENT_ROOT, firstNavigationPathSegment);

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(uriPattern.toString());
    uriBuilder = addLinkParametersAsQueryParameters(uriBuilder, linkParameters);
    return uriBuilder.buildAndExpand(uriVariables);
  }

  @Required
  public void setCheckoutRedirectUrlProvider(WcsUrlProvider checkoutRedirectUrlProvider) {
    this.checkoutRedirectUrlProvider = checkoutRedirectUrlProvider;
  }

  @Nullable
  private static Cart resolveCart() {
    StoreContext storeContext = CurrentStoreContext.get();

    return storeContext.getConnection()
            .getCartService()
            .map(cartService -> cartService.getCart(storeContext))
            .orElse(null);
  }

  @VisibleForTesting
  boolean isStudioPreview(@NonNull HttpServletRequest request) {
    return isStudioPreviewRequest(request);
  }

  //====================================================================================================================

  /**
   * This class fetches the actual cart from the cart service only if some methods are actually used. This saves a cart
   * fetch round trip to the commerce backend if the cart is only needed for link building.
   */
  private class LazyCart implements Cart {

    private Cart delegate;

    public Cart getDelegate() {
      if (delegate == null) {
        delegate = resolveCart();
      }
      return delegate;
    }

    @Override
    @NonNull
    public CommerceId getId() {
      return getDelegate().getId();
    }

    @Override
    @NonNull
    public CommerceId getReference() {
      return getId();
    }

    @Override
    public StoreContext getContext() {
      return getDelegate().getContext();
    }

    @Override
    public Locale getLocale() {
      return getDelegate().getLocale();
    }

    @Override
    public List<OrderItem> getOrderItems() {
      return getDelegate().getOrderItems();
    }

    @Override
    public BigDecimal getTotalQuantity() {
      return getDelegate().getTotalQuantity();
    }

    @Override
    public OrderItem findOrderItemById(String orderItemId) {
      return getDelegate().findOrderItemById(orderItemId);
    }

    @Override
    public String getExternalId() {
      return getDelegate().getExternalId();
    }

    @Override
    public String getExternalTechId() {
      return getDelegate().getExternalTechId();
    }

    @NonNull
    @Override
    public Map<String, Object> getCustomAttributes() {
      return getDelegate().getCustomAttributes();
    }

    @Nullable
    @Override
    public <T> T getCustomAttribute(@NonNull String key, @NonNull Class<T> expectedType) {
      return getDelegate().getCustomAttribute(key, expectedType);
    }

    @Override
    public void load() {
      getDelegate();
    }
  }
}
