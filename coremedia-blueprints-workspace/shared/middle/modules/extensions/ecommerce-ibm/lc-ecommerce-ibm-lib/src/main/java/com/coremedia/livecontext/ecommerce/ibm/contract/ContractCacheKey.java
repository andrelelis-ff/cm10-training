package com.coremedia.livecontext.ecommerce.ibm.contract;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdUtils;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.InvalidIdException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmDocumentCacheKey;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

public class ContractCacheKey extends AbstractIbmDocumentCacheKey<Map<String, Object>> {

  public static final String DEPENDENCY_ALL_CONTRACTS = "invalidate-all-contracts-event";

  private WcContractWrapperService wrapperService;

  public ContractCacheKey(@NonNull CommerceId id, @NonNull StoreContext storeContext, UserContext userContext,
                          WcContractWrapperService wrapperService, CommerceCache commerceCache) {
    super(id, storeContext, userContext, CONFIG_KEY_CONTRACT, commerceCache);
    this.wrapperService = wrapperService;
    if (!BaseCommerceBeanType.CONTRACT.equals(id.getCommerceBeanType())) {
      throw new InvalidIdException(id + " is not a contract id.");
    }
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    String techId = CommerceIdUtils.getExternalIdOrThrow(getCommerceId());
    return wrapperService.findContractByTechId(techId, storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> contract) {
    Cache.dependencyOn(DEPENDENCY_ALL_CONTRACTS);
  }

  @Override
  protected String getCacheIdentifier() {
    return assembleCacheIdentifier(
            id,
            configKey,
            storeContext.getSiteId(),
            user,
            storeContext.getStoreId(),
            storeContext.getLocale(),
            storeContext.getWorkspaceId().map(WorkspaceId::value).orElse(null)
    );
  }
}
