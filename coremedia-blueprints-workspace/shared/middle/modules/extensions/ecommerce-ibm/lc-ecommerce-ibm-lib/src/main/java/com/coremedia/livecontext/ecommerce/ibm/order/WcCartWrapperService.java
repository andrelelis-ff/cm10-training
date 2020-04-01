package com.coremedia.livecontext.ecommerce.ibm.order;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import org.springframework.http.HttpMethod;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static java.util.Collections.singletonList;

/**
 * A service that uses the catalog getRestConnector() to get cart wrappers.
 */
public class WcCartWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<WcCart, Void> GET_CART = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/cart/@self", (Class<Void>) null, WcCart.class)
          .userCookiesSupport(true)
          .build();

  private static final WcRestServiceMethod<Void, WcUpdateCartParam> UPDATE_CART = WcRestServiceMethod
          .builder(HttpMethod.PUT, "store/{storeId}/cart/@self", WcUpdateCartParam.class, Void.class)
          .userCookiesSupport(true)
          .build();

  private static final WcRestServiceMethod<Void, Void> CANCEL_CART = WcRestServiceMethod
          .builder(HttpMethod.DELETE, "store/{storeId}/cart/@self", (Class<Void>) null, Void.class)
          .userCookiesSupport(true)
          .build();

  private static final WcRestServiceMethod<Void, WcAddToCartParam> ADD_TO_CART = WcRestServiceMethod
          .builder(HttpMethod.POST, "store/{storeId}/cart", WcAddToCartParam.class, Void.class)
          .userCookiesSupport(true)
          .build();

  @Nullable
  public WcCart getCart(UserContext userContext, @NonNull StoreContext storeContext) {
    try {
      Integer userId = UserContextHelper.getForUserId(userContext);

      if (UserContextHelper.isAnonymousId(userId)) {
        return null;
      }

      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = getOptionalParameters(storeContext, userContext);

      return getRestConnector().callService(GET_CART, variableValues, optionalParameters, null, storeContext,
              userContext)
              .orElse(null);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  public void updateCart(UserContext userContext, @NonNull StoreContext storeContext, WcUpdateCartParam updateCartParam) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = getOptionalParameters(storeContext, userContext);

      getRestConnector().callService(UPDATE_CART, variableValues, optionalParameters, updateCartParam, storeContext,
              userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  public void addToCart(UserContext userContext, @NonNull StoreContext storeContext, WcAddToCartParam addToCartParam) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = getOptionalParameters(storeContext, userContext);

      getRestConnector().callService(ADD_TO_CART, variableValues, optionalParameters, addToCartParam, storeContext,
              userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  public void cancelCart(UserContext userContext, @NonNull StoreContext storeContext) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = getOptionalParameters(storeContext, userContext);

      getRestConnector().callService(CANCEL_CART, variableValues, optionalParameters, null, storeContext, userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  @NonNull
  private Map<String, String[]> getOptionalParameters(@NonNull StoreContext storeContext,
                                                      @Nullable UserContext userContext) {
    return buildParameterMap()
            .withCurrency(storeContext)
            .withLanguageId(storeContext)
            .withUserIdOrName(userContext)
            .build();
  }
}
