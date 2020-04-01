package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogName;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;

public class CatalogImpl extends AbstractIbmCommerceBean implements Catalog {

  private Map<String, Object> delegate;

  protected Map<String, Object> getDelegate() {
    if (delegate == null) {
      throw new IllegalStateException("Should not happen");
    }
    return delegate;
  }

  @Override
  public void setDelegate(Object delegate) {
    this.delegate = (Map<String, Object>) delegate;
  }

  @Override
  public String getExternalId() {
    return DataMapHelper.findString(getDelegate(), "catalogId").orElse(null);
  }

  @Override
  public String getExternalTechId() {
    return getExternalId();
  }

  @Nullable
  @Override
  public CatalogName getName() {
    return DataMapHelper.findString(getDelegate(), "catalogIdentifier")
            .map(CatalogName::of)
            .orElse(null);
  }

  @Override
  public boolean isDefaultCatalog() {
    return DataMapHelper.findValue(getDelegate(), "default", Boolean.class).orElse(false);
  }

  @NonNull
  @Override
  public Category getRootCategory() {
    return getCatalogService().findRootCategory(getCatalogAlias(), getContext());
  }
}
