package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Caches preview token requests. CacheKey duration configuration also impacts WcPreviewTokenParam#tokenLife.
 */
public class PreviewTokenCacheKey extends AbstractCommerceCacheKey<WcPreviewToken> {

  private WcPreviewTokenParam previewTokenParam;
  private WcPreviewTokenWrapperService wrapperService;

  public PreviewTokenCacheKey(WcPreviewTokenParam previewTokenParam, @NonNull StoreContext storeContext,
                              WcPreviewTokenWrapperService wrapperService, CommerceCache commerceCache) {
    super("previewToken", storeContext, null, CONFIG_KEY_PREVIEW_TOKEN, commerceCache);
    this.wrapperService = wrapperService;
    this.previewTokenParam = previewTokenParam;
  }

  @Override
  public WcPreviewToken computeValue(Cache cache) {
    return wrapperService.getPreviewToken(previewTokenParam, storeContext)
            .orElse(null);
  }

  @Override
  public void addExplicitDependency(WcPreviewToken wcPreviewToken) {
  }
}
