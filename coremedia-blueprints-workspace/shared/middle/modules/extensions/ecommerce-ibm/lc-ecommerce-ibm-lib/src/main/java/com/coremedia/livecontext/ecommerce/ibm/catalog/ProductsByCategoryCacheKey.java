package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class ProductsByCategoryCacheKey extends AbstractCommerceCacheKey<List<Map<String, Object>>> {

  private WcCatalogWrapperService wrapperService;

  public ProductsByCategoryCacheKey(String id, CatalogAlias catalog, @NonNull StoreContext storeContext,
                                    UserContext userContext, WcCatalogWrapperService wrapperService,
                                    CommerceCache commerceCache) {
    super(id, catalog, storeContext, userContext, CONFIG_KEY_PRODUCTS_BY_CATEGORY, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public List<Map<String, Object>> computeValue(Cache cache) {
    return wrapperService.findProductsByCategoryId(id, catalogAlias, storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(List<Map<String, Object>> wcProducts) {
    if (wcProducts == null || wcProducts.isEmpty()) {
      return;
    }

    Map<String, Object> firstWcProduct = wcProducts.get(0);
    if (DataMapHelper.getValueForKey(firstWcProduct, "parentCatalogGroupID[0]") != null) {
      Cache.dependencyOn(DataMapHelper.findString(firstWcProduct, "parentCatalogGroupID[0]").orElse(null));
    }
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
