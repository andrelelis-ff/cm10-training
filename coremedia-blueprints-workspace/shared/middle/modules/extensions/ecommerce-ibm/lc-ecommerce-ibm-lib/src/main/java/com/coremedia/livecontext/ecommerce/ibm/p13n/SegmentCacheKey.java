package com.coremedia.livecontext.ecommerce.ibm.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmDocumentCacheKey;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;

public class SegmentCacheKey extends AbstractIbmDocumentCacheKey<Map<String, Object>> {

  private WcSegmentWrapperService wrapperService;

  public SegmentCacheKey(@NonNull CommerceId id, @NonNull StoreContext storeContext, UserContext userContext,
                         WcSegmentWrapperService wrapperService, CommerceCache commerceCache) {
    super(id, storeContext, userContext, CONFIG_KEY_SEGMENT, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.findSegmentById(getCommerceId(), storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> segment) {
    Cache.dependencyOn(SegmentsCacheKey.DEPENDENCY_ALL_SEGMENTS);
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
