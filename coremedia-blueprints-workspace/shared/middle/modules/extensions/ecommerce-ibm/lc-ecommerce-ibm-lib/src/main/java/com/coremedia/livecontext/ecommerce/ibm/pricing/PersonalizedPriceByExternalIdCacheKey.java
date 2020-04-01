package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.WcCatalogWrapperService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;

public class PersonalizedPriceByExternalIdCacheKey extends AbstractCommerceCacheKey<WcPrice> {

  private WcCatalogWrapperService wrapperService;

  public PersonalizedPriceByExternalIdCacheKey(String id, @NonNull StoreContext storeContext, UserContext userContext,
                                               WcCatalogWrapperService wrapperService, CommerceCache commerceCache) {
    super(id, storeContext, userContext, CONFIG_KEY_DYNAMIC_PRICE, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public WcPrice computeValue(Cache cache) {
    return wrapperService.findDynamicProductPriceByExternalId(id, storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(WcPrice wcProductPrice) {
  }
}
