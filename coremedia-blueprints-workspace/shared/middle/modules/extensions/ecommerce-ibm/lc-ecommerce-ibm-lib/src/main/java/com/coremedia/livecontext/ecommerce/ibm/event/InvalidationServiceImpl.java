package com.coremedia.livecontext.ecommerce.ibm.event;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.event.InvalidationEvent;
import com.coremedia.livecontext.ecommerce.event.InvalidationPropagator;
import com.coremedia.livecontext.ecommerce.event.InvalidationService;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CategoryImpl;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmService;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.event.InvalidationEvent.CATEGORY_EVENT;
import static com.coremedia.livecontext.ecommerce.event.InvalidationEvent.CLEAR_ALL_EVENT;
import static com.coremedia.livecontext.ecommerce.event.InvalidationEvent.MARKETING_SPOT_EVENT;
import static com.coremedia.livecontext.ecommerce.event.InvalidationEvent.PRODUCT_EVENT;
import static com.coremedia.livecontext.ecommerce.event.InvalidationEvent.SEGMENT_EVENT;
import static com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper.findString;
import static com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper.findValue;
import static com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper.getList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Service bean for polling invalidations from WCS.
 * All invalidation events are propagated to a list of {@link InvalidationPropagator}
 */
class InvalidationServiceImpl extends AbstractIbmService implements InvalidationService {

  private static final Logger LOG = LoggerFactory.getLogger(InvalidationServiceImpl.class);

  private static final String CONTENT_IDENTIFIER_CLEAR_ALL = "clearall";
  static final String CONTENT_IDENTIFIER_PRODUCT = "ProductDisplay";
  static final String CONTENT_IDENTIFIER_CATEGORY = "CategoryDisplay";
  static final String CONTENT_IDENTIFIER_MARKETING_SPOT = "espot";
  static final String CONTENT_IDENTIFIER_SEGMENT = "segment";
  static final String CONTENT_IDENTIFIER_TOP_CATEGORY = "TopCategoryDisplay";

  private final WcInvalidationWrapperService wcInvalidationWrapperService;

  private long maxWaitInMilliseconds;

  private long chunkSize;

  InvalidationServiceImpl(@NonNull WcInvalidationWrapperService wcInvalidationWrapperService,
                          long maxWaitInMilliseconds, long chunkSize) {
    this.wcInvalidationWrapperService = wcInvalidationWrapperService;
    this.maxWaitInMilliseconds = maxWaitInMilliseconds;
    this.chunkSize = chunkSize;
  }

  @Override
  @NonNull
  public List<InvalidationEvent> getInvalidations(long timestamp, @NonNull StoreContext storeContext) {
    try {
      CurrentStoreContext.set(storeContext.getConnection().getStoreContext());
      return getInvalidationsInternal(timestamp, storeContext);
    } finally {
      CurrentStoreContext.remove();
    }
  }

  private List<InvalidationEvent> getInvalidationsInternal(long timestamp, @NonNull StoreContext storeContext) {
    // the invalidation thread would never be healthy again if a network error had occurred previously
    StoreContextHelper.setCommerceSystemIsUnavailable(storeContext, false);

    // in some cases (when the commerce system wasn't available in the initial run) the store context
    // can be incomplete (missing values because they couldn't be resolved via StoreInfoHandler)
    if (!StoreContextHelper.isValid(storeContext)) {
      storeContext = restoreStoreContext(storeContext);
    }

    Map<String, Object> cacheInvalidations = wcInvalidationWrapperService.getCacheInvalidations(
            timestamp, maxWaitInMilliseconds, chunkSize, storeContext);
    List<Map<String, Object>> invalidations = getList(cacheInvalidations, "invalidations");
    if (invalidations.isEmpty()) {
      return emptyList();
    }

    long lastInvalidationTimestamp = findValue(cacheInvalidations, "lastInvalidation", Long.class).orElse(-1L);

    // the list to return using API classes
    List<InvalidationEvent> eventList = invalidations.stream()
            .filter(Objects::nonNull)
            .map(invalidation -> convertEvent(invalidation, lastInvalidationTimestamp))
            .collect(toList());

    // Because the ibm commerce system does not know anything about a root category it will never
    // signal a root category change. Therefore a root category change should always be included if any category
    // is changed. Actually it would only be necessary for a top level category but for this test we would have
    // to create a full category bean which seems a bit to heavy.

    if (eventList.stream().anyMatch(event -> CATEGORY_EVENT.equals(event.getContentType()))) {
      eventList.add(new InvalidationEvent(CategoryImpl.ROOT_CATEGORY_ROLE_ID, CATEGORY_EVENT, lastInvalidationTimestamp));
    }

    return eventList;
  }

  @NonNull
  private static StoreContext restoreStoreContext(@NonNull StoreContext storeContext) {
    String siteId = storeContext.getSiteId();
    LOG.debug("Invalid store context found for site: {}, trying to restore...", siteId);

    try {
      CommerceConnection commerceConnection = CurrentStoreContext.find()
              .map(StoreContext::getConnection)
              .orElse(null);
      if (commerceConnection == null) {
        LOG.warn("Store context restore was not successful, commerce connection is null for site {}", siteId);
        return storeContext;
      }

      StoreContextProvider storeContextProvider = commerceConnection.getStoreContextProvider();
      StoreContext storeContextNew = storeContextProvider.findContextBySiteId(siteId)
              .orElseThrow(() -> new InvalidContextException("No store context found for site ID '" + siteId + "'."));

      StoreContextHelper.validateContext(storeContextNew);
      CurrentStoreContext.set(storeContextNew);
      LOG.debug("Store context restored: {}", storeContextNew);

      // Store context won't be `null` here as `CurrentStoreContext.set`
      // would have raised an exception.
      //noinspection ConstantConditions
      return storeContextNew;
    } catch (InvalidContextException e) {
      LOG.debug("Restore was not successful: {}", e.getMessage());
      return storeContext;
    }
  }

  @NonNull
  @Override
  public String getServiceEndpointId(@NonNull StoreContext storeContext) {
    return Optional.ofNullable(wcInvalidationWrapperService.getRestConnector())
            .map(restConnector -> restConnector.getServiceEndpoint(storeContext))
            .orElse("unknown");
  }

  @NonNull
  InvalidationEvent convertEvent(@NonNull Map<String, Object> event, long lastTimestamp) {
    String techId = getStringValueForKey(event, "techId");
    String contentType = getStringValueForKey(event, "contentType");
    long timestamp = findString(event, "timestamp")
            .map(Long::parseLong)
            .orElse(lastTimestamp);

    if (contentType != null) {
      switch (contentType) {
        case CONTENT_IDENTIFIER_CLEAR_ALL:
          return new InvalidationEvent(techId, CLEAR_ALL_EVENT, timestamp);
        case CONTENT_IDENTIFIER_PRODUCT:
          return new InvalidationEvent(techId, PRODUCT_EVENT, timestamp);
        case CONTENT_IDENTIFIER_CATEGORY: // same as top category
        case CONTENT_IDENTIFIER_TOP_CATEGORY:
          return new InvalidationEvent(techId, CATEGORY_EVENT, timestamp);
        case CONTENT_IDENTIFIER_MARKETING_SPOT:
          return new InvalidationEvent(techId, MARKETING_SPOT_EVENT, timestamp);
        case CONTENT_IDENTIFIER_SEGMENT:
          return new InvalidationEvent(techId, SEGMENT_EVENT, timestamp);
      }
    }

    return new InvalidationEvent(techId, null, timestamp);
  }

  @Nullable
  private static String getStringValueForKey(@NonNull Map<String, Object> map, @NonNull String key) {
    return findString(map, key).orElse(null);
  }
}
