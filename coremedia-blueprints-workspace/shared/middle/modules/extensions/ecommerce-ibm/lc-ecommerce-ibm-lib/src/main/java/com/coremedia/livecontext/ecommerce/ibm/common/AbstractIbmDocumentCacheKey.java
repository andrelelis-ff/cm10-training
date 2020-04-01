package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper.format;

public abstract class AbstractIbmDocumentCacheKey<T> extends AbstractCommerceCacheKey<T> {

  private final CommerceId commerceId;

  public AbstractIbmDocumentCacheKey(@NonNull CommerceId id, @NonNull StoreContext storeContext,
                                     UserContext userContext, String configKey, CommerceCache commerceCache) {
    super(format(id), storeContext, userContext, configKey, commerceCache);
    commerceId = id;
  }

  protected CommerceId getCommerceId() {
    return commerceId;
  }
}
