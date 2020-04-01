package com.coremedia.livecontext.ecommerce.ibm.p13n;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

public class SegmentsByUserCacheKey extends AbstractCommerceCacheKey<Map<String, Object>> {

  private WcSegmentWrapperService wrapperService;

  public SegmentsByUserCacheKey(@NonNull StoreContext storeContext, UserContext userContext,
                                WcSegmentWrapperService wrapperService, CommerceCache commerceCache) {
    super("segmentsByUser", storeContext, userContext, CONFIG_KEY_SEGMENTS_BY_USER, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.findSegmentsByUser(storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> segments) {
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
            storeContext.getWorkspaceId().map(WorkspaceId::value).orElse(null),
            storeContext.getPreviewDate().orElse(null)
    );
  }
}
