package com.coremedia.livecontext.ecommerce.ibm.inventory;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

public class AvailabilityByIdsCacheKey extends AbstractCommerceCacheKey<Map<String, Object>> {

  private WcAvailabilityWrapperService wrapperService;

  public AvailabilityByIdsCacheKey(String id, @NonNull StoreContext storeContext,
                                   WcAvailabilityWrapperService wrapperService, CommerceCache commerceCache) {
    super(id, storeContext, CONFIG_KEY_AVAILABILITY, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.getInventoryAvailability(id, storeContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> wcInventoryAvailabilities) {
    if (id != null && !id.isEmpty()) {
      String[] techIds = id.split(",");
      for (String techId : techIds) {
        Cache.dependencyOn(techId.trim());
      }
    }
  }
}
