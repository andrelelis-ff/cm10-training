package com.coremedia.blueprint.common.services.context;

import com.coremedia.blueprint.common.contentbeans.CMContext;

/**
 * Provides the content bean layer with access to the current {@link com.coremedia.blueprint.common.contentbeans.CMContext}.<br/>
 * Please note that considering the current {@link com.coremedia.blueprint.common.contentbeans.CMContext} in bean layer code is likely to result
 * in methods not cacheable by {@link com.coremedia.cache.CacheKey}s or dataviews.
 */
public interface CurrentContextService {

  /**
   * Returns the current context or null if there is no current context
   *
   * @return the current context or null if there is no current context
   */
  CMContext getContext();
}
