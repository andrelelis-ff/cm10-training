package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import org.springframework.http.HttpMethod;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;

/**
 * Wrapper service for person requests.
 */
public class WcPersonWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<Map, Map> FIND_PERSON_BY_SELF = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/person/@self", Map.class, Map.class)
          .secure(true)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  @Nullable
  public Map<String, Object> findPerson(UserContext userContext, @NonNull StoreContext storeContext) {
    List<String> variableValues = Collections.singletonList(getStoreId(storeContext));

    Map<String, String[]> parameters = buildParameterMap()
            .withCurrency(storeContext)
            .withLanguageId(storeContext)
            .withUserIdOrName(userContext)
            .build();

    //noinspection unchecked
    return getRestConnector().callService(FIND_PERSON_BY_SELF, variableValues, parameters, null, storeContext,
            userContext)
            .orElse(null);
  }
}
