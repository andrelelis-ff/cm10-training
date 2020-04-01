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

public class TopCategoriesCacheKey extends AbstractCommerceCacheKey<List<Map<String, Object>>> {

  private WcCatalogWrapperService wrapperService;

  public TopCategoriesCacheKey(CatalogAlias catalogAlias, @NonNull StoreContext storeContext, UserContext userContext,
                               WcCatalogWrapperService wrapperService, CommerceCache commerceCache) {
    super("root", catalogAlias, storeContext, userContext, CONFIG_KEY_TOP_CATEGORIES, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public List<Map<String, Object>> computeValue(Cache cache) {
    return wrapperService.findTopCategories(catalogAlias, storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(List<Map<String, Object>> wcCategories) {
    Cache.dependencyOn(CategoryImpl.ROOT_CATEGORY_ROLE_ID);
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
