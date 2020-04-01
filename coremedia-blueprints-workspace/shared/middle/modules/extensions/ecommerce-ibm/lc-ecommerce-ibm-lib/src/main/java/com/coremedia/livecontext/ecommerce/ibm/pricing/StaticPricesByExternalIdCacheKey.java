package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.WcCatalogWrapperService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;

public class StaticPricesByExternalIdCacheKey extends AbstractCommerceCacheKey<WcPrices> {

  private WcCatalogWrapperService wrapperService;

  public StaticPricesByExternalIdCacheKey(String id, CatalogAlias catalog, @NonNull StoreContext storeContext,
                                          UserContext userContext, WcCatalogWrapperService wrapperService,
                                          CommerceCache commerceCache) {
    super(id, catalog, storeContext, userContext, CONFIG_KEY_STATIC_PRICES, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public WcPrices computeValue(Cache cache) {
    return wrapperService.findStaticProductPricesByExternalId(id, catalogAlias, storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(WcPrices wcProductPrices) {
    //TODO: should be dependent to the product
  }

  @Override
  protected String getCacheIdentifier() {
    return assembleCacheIdentifier(
            id,
            catalogAlias,
            configKey,
            storeContext.getSiteId(),
            storeContext.getStoreId(),
            storeContext.getLocale(),
            storeContext.getCurrency(),
            storeContext.getWorkspaceId().map(WorkspaceId::value).orElse(null),
            toString(storeContext.getContractIds()),
            toString(storeContext.getContractIdsForPreview())
    );
  }
}
