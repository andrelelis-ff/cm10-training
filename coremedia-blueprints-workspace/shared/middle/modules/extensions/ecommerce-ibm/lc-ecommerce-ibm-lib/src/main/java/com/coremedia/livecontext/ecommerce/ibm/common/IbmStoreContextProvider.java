package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractStoreContextProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionFinder;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.NoCommerceConnectionAvailable;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.SiteToStoreContextCacheKeyWithTimeout;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.CatalogName;
import com.coremedia.livecontext.ecommerce.common.CommerceConfigKeys;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.WORKSPACE_ID_NONE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@DefaultAnnotation(NonNull.class)
public class IbmStoreContextProvider extends AbstractStoreContextProvider {

  private static final Logger LOG = LoggerFactory.getLogger(IbmStoreContextProvider.class);

  private final CommerceConnectionFinder commerceConnectionFinder;

  @Nullable
  private StoreInfoService storeInfoService;

  public IbmStoreContextProvider(CommerceConnectionFinder commerceConnectionFinder) {
    this.commerceConnectionFinder = commerceConnectionFinder;
  }

  @Override
  protected Optional<StoreContext> getStoreContextFromCache(Site site) {
    return findInCache(new SiteToStoreContextCacheKeyWithTimeout(site, this, 30));
  }

  @Override
  protected Optional<StoreContext> internalCreateContext(Site site) {
    // Only create store context if settings are found for current site.
    return Optional.of(findRepositoryStoreConfig(site))
            .filter(config -> !config.isEmpty())
            .map(config -> buildContextFromRepositoryStoreConfig(site, config))
            // only expose valid store context
            .filter(StoreContextHelper::isValid);
  }

  private StoreContext buildContextFromRepositoryStoreConfig(Site site, Map<String, Object> repositoryStoreConfig) {
    Map<String, Object> targetConfig = new HashMap<>();

    findConfigId(repositoryStoreConfig)
            .map(this::readStoreConfigFromSpring)
            .ifPresent(targetConfig::putAll);

    updateStoreConfigFromRepository(repositoryStoreConfig, targetConfig);

    StoreContextValuesHolder valuesHolder = populateValuesHolder(targetConfig, site);

    updateStoreConfigFromDynamicStoreInfo(site.getName(), valuesHolder);

    return createStoreContext(valuesHolder, site);
  }

  private StoreContextValuesHolder populateValuesHolder(Map<String, Object> config, Site site) {
    CommerceConnection connection = commerceConnectionFinder.findConnection(site)
            .orElseThrow(() -> new NoCommerceConnectionAvailable(
                    String.format("Could not find commerce connection for site '%s'.", site)));

    StoreContextValuesHolder valuesHolder = new StoreContextValuesHolder(connection);

    valuesHolder.storeId = (String) config.get(CommerceConfigKeys.STORE_ID);
    valuesHolder.storeName = (String) config.get(CommerceConfigKeys.STORE_NAME);
    String catalogIdStr = (String) config.get(CommerceConfigKeys.CATALOG_ID);
    valuesHolder.catalogId = catalogIdStr != null ? CatalogId.of(catalogIdStr) : null;
    String catalogNameStr = (String) config.get(CommerceConfigKeys.CATALOG_NAME);
    valuesHolder.catalogName = catalogNameStr != null ? CatalogName.of(catalogNameStr) : null;
    String currencyStr = (String) config.get(CommerceConfigKeys.CURRENCY);
    valuesHolder.currency = currencyStr != null ? parseCurrency(currencyStr) : null;
    String workspaceIdStr = (String) config.get(CommerceConfigKeys.WORKSPACE_ID);
    valuesHolder.workspaceId = workspaceIdStr != null ? WorkspaceId.of(workspaceIdStr) : WORKSPACE_ID_NONE;
    valuesHolder.wcsVersion = (String) config.get(CONFIG_KEY_WCS_VERSION);
    valuesHolder.replacements = (Map<String, String>) config.get(CommerceConfigKeys.REPLACEMENTS);

    return valuesHolder;
  }

  protected void updateStoreConfigFromDynamicStoreInfo(String siteName, StoreContextValuesHolder valuesHolder) {
    if (storeInfoService == null || !storeInfoService.isAvailable()) {
      return;
    }

    String storeName = getStoreName(valuesHolder, siteName);

    if (valuesHolder.storeId == null) {
      valuesHolder.storeId = getStoreIdFromStoreInfo(storeName, siteName);
    }

    if (valuesHolder.catalogId == null) {
      valuesHolder.catalogId = getCatalogIdFromStoreInfo(storeName, valuesHolder.catalogName, siteName);
    }

    valuesHolder.timeZoneId = storeInfoService.getTimeZone().toZoneId();
    getWcsVersion(valuesHolder).ifPresent(wcsVersion -> valuesHolder.wcsVersion = wcsVersion);
  }

  private static String getStoreName(StoreContextValuesHolder valuesHolder, String siteName) {
    String storeName = valuesHolder.storeName;

    if (isBlank(storeName)) {
      throw new InvalidContextException("No store name found in config (site: " + siteName + ").");
    }

    return storeName;
  }

  private String getStoreIdFromStoreInfo(String storeName, String siteName) {
    return storeInfoService.getStoreId(storeName)
            .orElseThrow(() -> new InvalidContextException(
                    "No store id found for store '" + storeName + "' in WCS (site: " + siteName + ")."));
  }

  private CatalogId getCatalogIdFromStoreInfo(String storeName, CatalogName catalogName, String siteName) {
    if (catalogName != null) {
      return getCatalogId(storeName, catalogName, siteName);
    } else {
      return getDefaultCatalogId(storeName, siteName);
    }
  }

  private CatalogId getCatalogId(String storeName, CatalogName catalogName, String siteName) {
    return storeInfoService.getCatalogId(storeName, catalogName.value())
            .orElseThrow(() -> new InvalidContextException(
                    "No catalog '" + catalogName.value() + "' found in WCS (site: " + siteName + ")."));
  }

  private CatalogId getDefaultCatalogId(String storeName, String siteName) {
    return storeInfoService.getDefaultCatalogId(storeName)
            .orElseThrow(() -> new InvalidContextException(
                    "No default catalog id found for store '" + storeName + "' in WCS (site: " + siteName + ")."));
  }

  private static Currency parseCurrency(String currencyCode) {
    try {
      return Currency.getInstance(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new InvalidContextException(e);
    }
  }

  private Optional<String> getWcsVersion(StoreContextValuesHolder valuesHolder) {
    Optional<String> storeInfoWcsVersion = storeInfoService.getWcsVersion();

    if (!storeInfoWcsVersion.isPresent()) {
      LOG.info("No dynamic WCS version. Please update the StoreInfoHandler.");
      return Optional.empty();
    }

    String configWcsVersion = valuesHolder.wcsVersion;

    if (isNotBlank(configWcsVersion) && !storeInfoWcsVersion.get().equals(configWcsVersion)) {
      LOG.info("The configured WCS version '{}' is different from the version reading from the WCS system '{}'. " +
                      "Delete the configuration for \"livecontext.ibm.wcs.version\" to avoid this message. " +
                      "In the following we go with the version coming from the WCS system.",
              configWcsVersion, storeInfoWcsVersion);
    }

    return storeInfoWcsVersion;
  }

  private static StoreContext createStoreContext(StoreContextValuesHolder valuesHolder, Site site) {
    IbmStoreContextBuilder builder = IbmStoreContextBuilder.from(valuesHolder.connection, site.getId());

    if (valuesHolder.storeId != null) {
      if (isBlank(valuesHolder.storeId)) {
        throw new InvalidContextException("Store ID must not be blank.");
      }

      builder.withStoreId(valuesHolder.storeId);
    }

    if (valuesHolder.storeName != null) {
      if (isBlank(valuesHolder.storeName)) {
        throw new InvalidContextException("Store name must not be blank.");
      }

      builder.withStoreName(valuesHolder.storeName);
    }

    if (valuesHolder.catalogId != null) {
      builder.withCatalogId(valuesHolder.catalogId);
    }

    if (valuesHolder.currency != null) {
      builder.withCurrency(valuesHolder.currency);
    }

    builder
            .withLocale(site.getLocale())
            .withTimeZoneId(valuesHolder.timeZoneId)
            .withWorkspaceId(valuesHolder.workspaceId)
            .withReplacements(valuesHolder.replacements);

    if (valuesHolder.wcsVersion != null) {
      WcsVersion.fromVersionString(valuesHolder.wcsVersion)
              .ifPresent(builder::withWcsVersion);
    }

    return builder.build();
  }

  private static class StoreContextValuesHolder {

    private final CommerceConnection connection;

    @Nullable
    private String storeId;
    @Nullable
    private String storeName;
    @Nullable
    private CatalogId catalogId;
    @Nullable
    private CatalogName catalogName;
    @Nullable
    private Currency currency;
    @Nullable
    private ZoneId timeZoneId;
    @Nullable
    private WorkspaceId workspaceId;
    @Nullable
    private String wcsVersion;
    @Nullable
    private Map<String, String> replacements;

    private StoreContextValuesHolder(CommerceConnection connection) {
      this.connection = connection;
    }
  }

  public void setStoreInfoService(StoreInfoService storeInfoService) {
    this.storeInfoService = storeInfoService;
  }
}
