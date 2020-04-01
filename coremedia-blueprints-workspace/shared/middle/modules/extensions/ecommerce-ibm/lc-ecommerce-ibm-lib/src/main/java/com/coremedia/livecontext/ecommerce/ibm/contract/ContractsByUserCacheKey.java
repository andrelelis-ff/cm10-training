package com.coremedia.livecontext.ecommerce.ibm.contract;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;

public class ContractsByUserCacheKey extends AbstractCommerceCacheKey<Map<String, Object>> {

  private WcContractWrapperService wrapperService;

  public ContractsByUserCacheKey(@NonNull UserContext userContext, @NonNull StoreContext storeContext,
                                 @Nullable String organizationId, WcContractWrapperService wrapperService,
                                 CommerceCache commerceCache) {
    super(
            userContext.getUserId()
                    + "_" + userContext.getUserName()
                    + "_" + (organizationId != null ? organizationId : ""),
            storeContext, userContext, CONFIG_KEY_CONTRACTS_BY_USER, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.findContractsForUser(userContext, storeContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> stringObjectMap) {
    Cache.dependencyOn(ContractCacheKey.DEPENDENCY_ALL_CONTRACTS);
  }

  @Override
  protected String getCacheIdentifier() {
    return assembleCacheIdentifier(
            id,
            configKey,
            storeContext.getSiteId(),
            storeContext.getStoreId(),
            storeContext.getLocale(),
            storeContext.getCurrency(),
            storeContext.getWorkspaceId().map(WorkspaceId::value).orElse(null),
            toString(storeContext.getContractIds())
    );
  }
}
