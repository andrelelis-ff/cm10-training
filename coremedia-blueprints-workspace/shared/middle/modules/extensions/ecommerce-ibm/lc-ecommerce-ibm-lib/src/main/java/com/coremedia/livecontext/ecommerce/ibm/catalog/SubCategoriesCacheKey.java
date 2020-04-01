package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class SubCategoriesCacheKey extends AbstractCommerceCacheKey<List<Map<String, Object>>> {

  private WcCatalogWrapperService wrapperService;

  public SubCategoriesCacheKey(String id, CatalogAlias catalog, @NonNull StoreContext storeContext,
                               UserContext userContext, WcCatalogWrapperService wrapperService,
                               CommerceCache commerceCache) {
    super(id, catalog, storeContext, userContext, CONFIG_KEY_SUB_CATEGORIES, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public List<Map<String, Object>> computeValue(Cache cache) {
    return wrapperService.findSubCategories(id, catalogAlias, storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(List<Map<String, Object>> wcCategories) {
    Cache.dependencyOn(this.id);
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
            storeContext.getWorkspaceId().map(WorkspaceId::value).orElse(null),
            toString(storeContext.getContractIds()),
            toString(storeContext.getContractIdsForPreview())
    );
  }
}
