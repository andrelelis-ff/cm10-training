package com.coremedia.livecontext.ecommerce.ibm.inventory;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import org.springframework.http.HttpMethod;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

/**
 * A service that uses the getRestConnector() to get inventory wrappers by certain search queries.
 */
public class WcAvailabilityWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<Map, Void> GET_AVAILABILITY_FOR_PRODUCT_VARIANTS = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/inventoryavailability/{productVariantList}", Void.class, Map.class)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  @NonNull
  public Map<String, Object> getInventoryAvailability(String skuIds, StoreContext storeContext) {
    if (skuIds == null || skuIds.isEmpty()) {
      return emptyMap();
    }

    List<String> variableValues = asList(getStoreId(storeContext), skuIds);

    Map<String, String[]> optionalParameters = buildParameterMap()
            .withLanguageId(storeContext)
            .build();

    return (Map<String, Object>) getRestConnector()
            .callService(GET_AVAILABILITY_FOR_PRODUCT_VARIANTS, variableValues, optionalParameters, null, storeContext,
                    null)
            .orElseGet(Collections::emptyMap);
  }
}
