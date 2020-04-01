package com.coremedia.ecommerce.studio.rest.cache;

import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.event.InvalidationEvent;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import com.coremedia.livecontext.ecommerce.p13n.Segment;
import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.Optional;

/**
 * Mapping of invalidation event types and commerce bean types to commerce bean classes for
 * Studio REST resource linking.
 */
class InvalidationEventTypeMapping {

  private static final Map<String, Class<? extends CommerceBean>> MAPPING = ImmutableMap.<String, Class<? extends CommerceBean>>builder()
          .put(InvalidationEvent.CATALOG_EVENT, Catalog.class)
          .put(InvalidationEvent.CATEGORY_EVENT, Category.class)
          .put(InvalidationEvent.MARKETING_SPOT_EVENT, MarketingSpot.class)
          .put(InvalidationEvent.PRODUCT_EVENT, Product.class)
          .put(InvalidationEvent.SKU_EVENT, ProductVariant.class)
          .put(InvalidationEvent.SEGMENT_EVENT, Segment.class)
          .build();

  @NonNull
  static Optional<Class<? extends CommerceBean>> get(@NonNull String key) {
    return Optional.ofNullable(MAPPING.get(key));
  }
}
