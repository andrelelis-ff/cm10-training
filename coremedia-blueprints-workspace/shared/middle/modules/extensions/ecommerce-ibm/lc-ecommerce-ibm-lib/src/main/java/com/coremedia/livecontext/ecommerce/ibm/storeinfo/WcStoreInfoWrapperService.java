package com.coremedia.livecontext.ecommerce.ibm.storeinfo;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * A service that uses the getRestConnector() to get all store infos in wcs.
 */
class WcStoreInfoWrapperService extends AbstractWcWrapperService {

  private static final Logger LOG = LoggerFactory.getLogger(WcStoreInfoWrapperService.class);

  private static final WcRestServiceMethod<Map, Void> GET_STORE_INFO = WcRestServiceMethod
          .builder(HttpMethod.GET, "coremedia/storeinfo", Void.class, Map.class)
          .previewSupport(true)
          .build();

  @NonNull
  Map<String, Object> getStoreInfos() {
    //noinspection unchecked
    try {
      return getRestConnector().callServiceInternal(GET_STORE_INFO, emptyList(), emptyMap(), null, null, null)
              .orElseGet(Collections::emptyMap);
    } catch (CommerceException e) {
      LOG.warn("ignoring exception while retrieving WCS storeinfo: {}", e.getMessage());
      return emptyMap();
    }
  }
}
