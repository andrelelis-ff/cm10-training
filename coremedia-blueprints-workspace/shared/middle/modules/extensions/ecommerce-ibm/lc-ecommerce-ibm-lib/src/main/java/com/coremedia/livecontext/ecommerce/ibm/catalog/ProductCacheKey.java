package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.InvalidIdException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmDocumentCacheKey;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;
import java.util.Objects;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.SKU;

class ProductCacheKey extends AbstractIbmDocumentCacheKey<Map<String, Object>> {

  private static final String UNIQUE_ID = "uniqueID";

  private WcCatalogWrapperService wrapperService;

  ProductCacheKey(@NonNull CommerceId id, @NonNull StoreContext storeContext, UserContext userContext,
                  WcCatalogWrapperService wrapperService, CommerceCache commerceCache) {
    super(id, storeContext, userContext, CONFIG_KEY_PRODUCT, commerceCache);
    this.wrapperService = wrapperService;

    CommerceBeanType commerceBeanType = id.getCommerceBeanType();
    if (!PRODUCT.equals(commerceBeanType) && !SKU.equals(commerceBeanType)) {
      throw new InvalidIdException(id + " is neither a product nor sku id.");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    if (!super.equals(o)) {
      return false;
    }

    ProductCacheKey that = (ProductCacheKey) o;
    return Objects.equals(wrapperService, that.wrapperService);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), wrapperService);
  }

  @Override
  public Map<String, Object> computeValue(Cache cache) {
    return wrapperService.findProductById(getCommerceId(), storeContext, userContext);
  }

  @Override
  public void addExplicitDependency(Map<String, Object> wcProduct) {
    if (wcProduct != null && wcProduct.containsKey(UNIQUE_ID)) {
      Cache.dependencyOn(DataMapHelper.findString(wcProduct, UNIQUE_ID).orElse(null));
    }
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
            toString(storeContext.getContractIds()),
            toString(storeContext.getContractIdsForPreview())
    );
  }
}
