package com.coremedia.livecontext.ecommerce.ibm.workspace;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static java.util.Collections.singletonList;

/**
 * A service that uses the rest connector to get workspaces from ibm wcs.
 */
public class WcWorkspaceWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<Map, Void> FIND_ALL_WORKSPACES = WcRestServiceMethod
          .builder(HttpMethod.POST, "store/{storeId}/workspaces/byall/Active", Void.class, Map.class)
          .secure(true)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  /**
   * Gets a list of all existing workspaces.
   *
   * @param storeContext the current store context
   * @param userContext  the current user context
   * @return Map
   */
  public Map<String, Object> findAllWorkspaces(StoreContext storeContext, UserContext userContext) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));
      Map<String, String[]> parameters = buildParameterMap()
              .withCurrency(storeContext)
              .withLanguageId(storeContext)
              .build();

      //noinspection unchecked
      return getRestConnector().callService(FIND_ALL_WORKSPACES, variableValues, parameters, null, storeContext,
              userContext)
              .orElse(null);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }
}
