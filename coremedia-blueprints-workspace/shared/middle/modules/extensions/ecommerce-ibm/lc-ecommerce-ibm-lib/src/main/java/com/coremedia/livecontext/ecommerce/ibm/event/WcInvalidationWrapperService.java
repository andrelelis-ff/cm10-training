package com.coremedia.livecontext.ecommerce.ibm.event;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import org.springframework.http.HttpMethod;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

/**
 * A service that uses the getRestConnector() to poll invalidation events.
 */
class WcInvalidationWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<Map, Void> GET_CACHE_INVALIDATION = WcRestServiceMethod
          .builder(HttpMethod.GET, "coremedia/cacheinvalidation/{startTimestamp}?maxWait={maxWait}&chunkSize={chunkSize}", Void.class, Map.class)
          .build();

  @NonNull
  Map<String, Object> getCacheInvalidations(long lastExecutionTimeStamp,
                                            long maxWaitInMilliseconds,
                                            long chunkSize,
                                            @NonNull StoreContext storeContext) {
    List<String> variableValues = asList(
            String.valueOf(lastExecutionTimeStamp),
            String.valueOf(maxWaitInMilliseconds),
            String.valueOf(chunkSize));

    //noinspection unchecked
    return (Map<String, Object>) getRestConnector()
            .callService(GET_CACHE_INVALIDATION, variableValues, emptyMap(), null, storeContext, null)
            .orElseThrow(() -> new CommerceException("Could not poll cache invalidations from commerce system"));
  }
}
