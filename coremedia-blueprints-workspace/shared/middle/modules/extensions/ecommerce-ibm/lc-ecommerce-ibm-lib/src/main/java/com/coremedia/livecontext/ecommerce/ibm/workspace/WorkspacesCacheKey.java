package com.coremedia.livecontext.ecommerce.ibm.workspace;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

public class WorkspacesCacheKey extends AbstractCommerceCacheKey<Map> {

  public static final String DEPENDENCY_ALL_WORKSPACES = "invalidate-all-workspaces-event";

  private WcWorkspaceWrapperService wrapperService;

  public WorkspacesCacheKey(@NonNull StoreContext storeContext, UserContext userContext,
                            WcWorkspaceWrapperService wrapperService, CommerceCache commerceCache) {
    super("workspaces", storeContext, userContext, CONFIG_KEY_WORKSPACES, commerceCache);
    this.wrapperService = wrapperService;
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.findAllWorkspaces(storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(Map workspaces) {
    Cache.dependencyOn(DEPENDENCY_ALL_WORKSPACES);
  }

  @Override
  protected String getCacheIdentifier() {
    return assembleCacheIdentifier(
            id,
            configKey,
            storeContext.getSiteId(),
            storeContext.getStoreId(),
            storeContext.getLocale()
    );
  }
}
