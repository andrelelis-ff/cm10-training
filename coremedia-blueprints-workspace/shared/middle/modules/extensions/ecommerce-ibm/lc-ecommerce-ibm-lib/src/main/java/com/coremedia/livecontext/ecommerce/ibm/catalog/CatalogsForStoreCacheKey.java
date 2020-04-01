package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

public class CatalogsForStoreCacheKey extends AbstractCommerceCacheKey<Map<String, Object>> {

  private WcCatalogWrapperService wrapperService;

  CatalogsForStoreCacheKey(@NonNull StoreContext storeContext, WcCatalogWrapperService wrapperService,
                           CommerceCache commerceCache) {
    super("availableCatalogs", storeContext, null, CONFIG_KEY_CATALOGS_FOR_STORE, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.getAvailableCatalogs(storeContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> stringObjectMap) {
    //nothing to do
  }

  @Override
  protected String getCacheIdentifier() {
    return assembleCacheIdentifier(
            id,
            configKey,
            storeContext.getStoreId()
    );
  }
}
