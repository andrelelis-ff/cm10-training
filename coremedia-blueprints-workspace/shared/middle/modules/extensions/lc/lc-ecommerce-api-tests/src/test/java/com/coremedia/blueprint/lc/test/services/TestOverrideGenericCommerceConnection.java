package com.coremedia.blueprint.lc.test.services;

import com.coremedia.blueprint.base.livecontext.client.common.GenericCommerceConnection;
import com.coremedia.blueprint.base.livecontext.client.common.RequiresGenericCommerceConnection;
import com.coremedia.blueprint.base.livecontext.client.data.DataCatalogService;
import com.coremedia.blueprint.base.livecontext.client.data.DataCategoryService;
import com.coremedia.blueprint.base.livecontext.client.data.DataCookieUserService;
import com.coremedia.blueprint.base.livecontext.client.data.DataLinkService;
import com.coremedia.blueprint.base.livecontext.client.data.DataMarketingSpotService;
import com.coremedia.blueprint.base.livecontext.client.data.DataPriceService;
import com.coremedia.blueprint.base.livecontext.client.data.DataProductService;
import com.coremedia.blueprint.base.livecontext.client.data.DataSearchService;
import com.coremedia.blueprint.base.livecontext.client.data.DataSegmentService;
import com.coremedia.blueprint.base.livecontext.client.data.DataStoreService;
import com.coremedia.blueprint.base.livecontext.client.data.Metadata;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.livecontext.ecommerce.common.ForVendor;

/**
 * Example implementation of a custom commerce connection for the generic client.
 */
@ForVendor("test")
class TestOverrideGenericCommerceConnection extends BaseCommerceConnection
        implements GenericCommerceConnection, RequiresGenericCommerceConnection {

  private GenericCommerceConnection delegate;

  @Override
  public void setGenericCommerceConnection(GenericCommerceConnection genericCommerceConnection) {
    delegate = genericCommerceConnection;
  }

  @Override
  public DataMarketingSpotService getDataMarketingSpotService() {
    return delegate.getDataMarketingSpotService();
  }

  @Override
  public DataCatalogService getDataCatalogService() {
    return delegate.getDataCatalogService();
  }

  @Override
  public DataSearchService getDataSearchService() {
    return delegate.getDataSearchService();
  }

  @Override
  public DataLinkService getDataLinkService() {
    return delegate.getDataLinkService();
  }

  @Override
  public DataCategoryService getDataCategoryService() {
    return delegate.getDataCategoryService();
  }

  @Override
  public DataProductService getDataProductService() {
    return delegate.getDataProductService();
  }

  @Override
  public DataStoreService getDataStoreService() {
    return delegate.getDataStoreService();
  }

  @Override
  public DataSegmentService getDataSegmentService() {
    return delegate.getDataSegmentService();
  }

  @Override
  public DataPriceService getDataPriceService() {
    return delegate.getDataPriceService();
  }

  @Override
  public DataCookieUserService getDataCookieUserService() {
    return delegate.getDataCookieUserService();
  }

  @Override
  public Metadata getMetadata() {
    return delegate.getMetadata();
  }
}
