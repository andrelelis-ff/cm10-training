package com.coremedia.livecontext.ecommerce.ibm.contract;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class ContractImpl extends AbstractIbmCommerceBean implements Contract {

  private Map<String, Object> delegate;
  private WcContractWrapperService contractWrapperService;
  private static int DEFAULT_CATALOG_IDENTIFIER = 0;

  public Map<String, Object> getDelegate() {
    if (delegate == null) {
      UserContext userContext = CurrentUserContext.get();
      CommerceCache commerceCache = getCommerceCache();

      ContractCacheKey cacheKey = new ContractCacheKey(getId(), getContext(), userContext, getContractWrapperService(),
              commerceCache);

      delegate = commerceCache.find(cacheKey)
              .orElseThrow(() -> new NotFoundException(getId() + " (contract not found in catalog)"));
    }

    return delegate;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setDelegate(Object delegate) {
    this.delegate = (Map<String, Object>) delegate;
  }

  @Override
  public String getName() {
    return getStringValue(getDelegate(), "name");
  }

  @Override
  public String getDescription() {
    return getStringValue(getDelegate(), "name");
  }

  @Override
  public boolean isDefaultContract() {
    Integer usageType = DataMapHelper.findValue(getDelegate(), "usage", Integer.class).orElse(null);
    return Objects.equals(DEFAULT_CATALOG_IDENTIFIER, usageType);
  }

  @Override
  public String getExternalId() {
    return getStringValue(getDelegate(), "referenceNumber");
  }

  @Override
  public String getExternalTechId() {
    return getStringValue(getDelegate(), "referenceNumber");
  }

  public void setContractWrapperService(WcContractWrapperService contractWrapperService) {
    this.contractWrapperService = contractWrapperService;
  }

  public WcContractWrapperService getContractWrapperService() {
    return contractWrapperService;
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
