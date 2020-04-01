package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractStoreContextProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContextBuilder;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@DefaultAnnotation(NonNull.class)
public class IbmStoreContextBuilder implements StoreContextBuilder {

  private StoreContextBuilderImpl builder;
  private final CommerceConnection connection;
  @Nullable
  private WcsVersion wcsVersion;
  private boolean dynamicPricingEnabled = false;

  private IbmStoreContextBuilder(StoreContextBuilderImpl builder, CommerceConnection connection) {
    this.builder = builder;
    this.connection = connection;
  }

  public static IbmStoreContextBuilder from(CommerceConnection connection, String siteId) {
    StoreContextBuilderImpl builder = StoreContextBuilderImpl.from(connection, siteId);
    return new IbmStoreContextBuilder(builder, connection);
  }

  @SuppressWarnings(
          // Suppress until connection can be obtained from store context.
          "findbugs:NP_NULL_PARAM_DEREF_NONVIRTUAL"
  )
  public static IbmStoreContextBuilder from(StoreContextImpl storeContext) {
    CommerceConnection connection = null;

    StoreContextBuilderImpl builder = StoreContextBuilderImpl.from(storeContext);
    return new IbmStoreContextBuilder(builder, connection);
  }

  @Override
  public IbmStoreContextBuilder withSiteId(String siteId) {
    builder.withSiteId(siteId);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withStoreId(String storeId) {
    builder.withStoreId(storeId);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withStoreName(String storeName) {
    builder.withStoreName(storeName);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withCatalogId(@Nullable CatalogId catalogId) {
    builder.withCatalogId(catalogId);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withCatalogAlias(CatalogAlias catalogAlias) {
    builder.withCatalogAlias(catalogAlias);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withCurrency(Currency currency) {
    builder.withCurrency(currency);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withLocale(Locale locale) {
    builder.withLocale(locale);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withTimeZoneId(@Nullable ZoneId timeZoneId) {
    builder.withTimeZoneId(timeZoneId);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withPreviewDate(@Nullable ZonedDateTime previewDate) {
    builder.withPreviewDate(previewDate);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withWorkspaceId(@Nullable WorkspaceId workspaceId) {
    builder.withWorkspaceId(workspaceId);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withUserSegments(@Nullable String userSegments) {
    builder.withUserSegments(userSegments);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withContractIds(List<String> contractIds) {
    builder.withContractIds(contractIds);
    return this;
  }

  @Override
  public IbmStoreContextBuilder withContractIdsForPreview(List<String> contractIdsForPreview) {
    builder.withContractIdsForPreview(contractIdsForPreview);
    return this;
  }

  public IbmStoreContextBuilder withReplacements(Map<String, String> replacements) {
    builder.withReplacements(replacements);
    return this;
  }

  public IbmStoreContextBuilder withWcsVersion(WcsVersion wcsVersion) {
    this.wcsVersion = wcsVersion;
    return this;
  }

  public IbmStoreContextBuilder withDynamicPricingEnabled(boolean dynamicPricingEnabled) {
    this.dynamicPricingEnabled = dynamicPricingEnabled;
    return this;
  }

  @Override
  public StoreContextImpl build() {
    StoreContextImpl storeContext = builder.build();

    if (wcsVersion != null) {
      storeContext.put(AbstractStoreContextProvider.CONFIG_KEY_WCS_VERSION, wcsVersion);
    }

    if (dynamicPricingEnabled) {
      storeContext.put("dynamicPricing.enabled", true);
    }

    return storeContext;
  }
}
