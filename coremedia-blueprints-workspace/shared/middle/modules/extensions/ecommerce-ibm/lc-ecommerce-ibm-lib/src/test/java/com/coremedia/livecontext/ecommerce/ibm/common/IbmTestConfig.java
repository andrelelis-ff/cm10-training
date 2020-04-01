package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.lc.test.TestConfig;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class IbmTestConfig implements TestConfig {

  private static final String SITE_ID = "mySiteIndicator";
  private static final String STORE_ID = System.getProperty("lc.test.storeId", "10201");
  private static final String STORE_NAME = System.getProperty("lc.test.storeName", "AuroraESite");
  private static final String B2B_SITE_ID = "myB2BSiteIndicator";
  private static final String B2B_STORE_ID = System.getProperty("lc.test.storeId", "10303");
  private static final String B2B_STORE_ID_V80 = System.getProperty("lc.test.storeId", "715838085");
  private static final String B2B_STORE_NAME = System.getProperty("lc.test.storeName", "AuroraB2BESite");

  private static String CATALOG_NAME = System.getProperty("lc.test.catalogName", "Extended Sites Catalog Asset Store");
  private static CatalogId CATALOG_ID = CatalogId.of(System.getProperty("lc.test.catalogId", "10051"));

  private static CatalogId CATALOG_ID_B2C_V78 = CatalogId.of(System.getProperty("lc.test.catalogId", "10152"));
  private static CatalogId CATALOG_ID_B2B_V78 = CatalogId.of(System.getProperty("lc.test.catalogId", "10151"));
  private static String STORE_ID_V78 = System.getProperty("lc.test.storeId", "10301");

  private static CatalogId CATALOG_ID_B2C_V80 = CatalogId.of(System.getProperty("lc.test.catalogId", "3074457345616676719"));
  private static CatalogId CATALOG_ID_B2B_V80 = CatalogId.of(System.getProperty("lc.test.catalogId", "3074457345616676718"));
  private static String STORE_ID_V80 = System.getProperty("lc.test.storeId", "715838084");

  private static final Locale LOCALE = Locale.US;
  private static CatalogId CATALOG_ID_B2C_V90 = CatalogId.of(System.getProperty("lc.test.catalogId", "10502"));
  private static CatalogId CATALOG_ID_B2B_V90 = CatalogId.of(System.getProperty("lc.test.catalogId", "10501"));
  private static String STORE_ID_V90 = System.getProperty("lc.test.storeId", "1");
  private static final String B2B_STORE_ID_V90 = System.getProperty("lc.test.storeId", "2");

  private static final Currency CURRENCY = Currency.getInstance("USD");
  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of("4711");

  private static final String USER1_NAME = System.getProperty("lc.test.user1.name", "arover");
  private static final String USER1_ID = System.getProperty("lc.test.user1.id", "3");
  private static final String USER2_NAME = System.getProperty("lc.test.user2.name", "gstevens");
  private static final String USER2_ID = System.getProperty("lc.test.user2.id", "4");
  private static final String PREVIEW_USER_NAME = System.getProperty("lc.test.previewuser.name", "preview");

  private static final String USERSEGMENT1_ID = "8000000000000000551";
  private static final String USERSEGMENT2_ID = "8000000000000000554";
  private static final String USERSEGMENT1_ID_V80 = "8407790678950000502";
  private static final String USERSEGMENT2_ID_V80 = "8407790678950000505";
  private static final String USERSEGMENT1_ID_V90 = "8000000000000001001";
  private static final String USERSEGMENT2_ID_V90 = "8000000000000001004";

  private WcsVersion wcsVersion = WcsVersion.fromVersionString(System.getProperty("wcs.version", "8.0")).orElse(null);

  @Inject
  @Named("commerce:wcs1")
  private ObjectProvider<CommerceConnection> connectionProvider;

  @Override
  public CommerceConnection getCommerceConnection() {
    return requireNonNull(connectionProvider.getIfAvailable(), "Commerce connection commerce:wcs1 not available");
  }

  @NonNull
  @Override
  public StoreContextImpl getStoreContext(@NonNull CommerceConnection connection) {
    return getStoreContext(connection, CURRENCY);
  }

  @NonNull
  public StoreContextImpl getStoreContext(@NonNull CommerceConnection connection, @NonNull Currency currency) {
    IbmStoreContextBuilder builder = buildInitialStoreContext(connection, currency)
            .withWcsVersion(wcsVersion);

    return builder
            .withReplacements(assembleReplacements(builder.build()))
            .build();
  }

  @NonNull
  private IbmStoreContextBuilder buildInitialStoreContext(@NonNull CommerceConnection connection,
                                                          @NonNull Currency currency) {
    switch (wcsVersion) {
      case WCS_VERSION_9_0:
        return buildContext(connection, SITE_ID, STORE_ID_V90, STORE_NAME, CATALOG_ID_B2C_V90, LOCALE, currency);
      case WCS_VERSION_8_0:
        return buildContext(connection, SITE_ID, STORE_ID_V80, STORE_NAME, CATALOG_ID_B2C_V80, LOCALE, currency);
      case WCS_VERSION_7_8:
        return buildContext(connection, SITE_ID, STORE_ID_V78, STORE_NAME, CATALOG_ID_B2C_V78, LOCALE, currency);
      default:
        return buildContext(connection, SITE_ID, STORE_ID, STORE_NAME, CATALOG_ID, LOCALE, currency);
    }
  }

  @NonNull
  @Override
  public StoreContextImpl getGermanStoreContext(@NonNull CommerceConnection connection) {
    return IbmStoreContextBuilder
            .from(getStoreContext(connection))
            .withLocale(Locale.GERMAN)
            .build();
  }

  @NonNull
  public StoreContextImpl getB2BStoreContext(@NonNull CommerceConnection connection) {
    IbmStoreContextBuilder builder = buildInitialB2BStoreContext(connection)
            .withWcsVersion(wcsVersion)
            .withDynamicPricingEnabled(true);

    return builder
            .withReplacements(assembleReplacements(builder.build()))
            .build();
  }

  @NonNull
  private IbmStoreContextBuilder buildInitialB2BStoreContext(@NonNull CommerceConnection connection) {
    switch (wcsVersion) {
      case WCS_VERSION_9_0:
        return buildContext(connection, B2B_SITE_ID, B2B_STORE_ID_V90, STORE_NAME, CATALOG_ID_B2B_V90, LOCALE, CURRENCY);
      case WCS_VERSION_8_0:
        return buildContext(connection, B2B_SITE_ID, B2B_STORE_ID_V80, B2B_STORE_NAME, CATALOG_ID_B2B_V80, LOCALE, CURRENCY);
      case WCS_VERSION_7_8:
        return buildContext(connection, B2B_SITE_ID, B2B_STORE_ID, B2B_STORE_NAME, CATALOG_ID_B2B_V78, LOCALE, CURRENCY);
      default:
        return buildContext(connection, B2B_SITE_ID, STORE_ID, STORE_NAME, CATALOG_ID, LOCALE, CURRENCY);
    }
  }

  @NonNull
  private static Map<String, String> assembleReplacements(@NonNull StoreContext source) {
    Map<String, String> replacements = new HashMap<>();

    replacements.put("storeId", source.getStoreId());
    replacements.put("catalogId", source.getCatalogId().map(CatalogId::value).orElse(null));
    replacements.put("locale", source.getLocale().toString());

    return replacements;
  }

  @NonNull
  public StoreContextImpl getStoreContextWithWorkspace(@NonNull CommerceConnection connection) {
    return IbmStoreContextBuilder
            .from(getStoreContext(connection))
            .withWorkspaceId(WORKSPACE_ID)
            .build();
  }

  @NonNull
  private static IbmStoreContextBuilder buildContext(@NonNull CommerceConnection connection, @NonNull String siteId,
                                                     @NonNull String storeId, @NonNull String storeName,
                                                     @NonNull CatalogId catalogId, @NonNull Locale locale,
                                                     @NonNull Currency currency) {
    checkArgument(isNotBlank(storeId), "Store ID must not be blank.");
    checkArgument(isNotBlank(storeName), "Store name must not be blank.");

    return IbmStoreContextBuilder
            .from(connection, siteId)
            .withStoreId(storeId)
            .withStoreName(storeName)
            .withCatalogId(catalogId)
            .withLocale(locale)
            .withCurrency(currency);
  }

  @Override
  public String getCatalogName() {
    return CATALOG_NAME;
  }

  public String getUser1Name() {
    return USER1_NAME;
  }

  public String getUser1Id() {
    return USER1_ID;
  }

  public String getUser2Name() {
    return USER2_NAME;
  }

  public String getUser2Id() {
    return USER2_ID;
  }

  public String getPreviewUserName() {
    return PREVIEW_USER_NAME;
  }

  public String getUserSegment1Id() {
    return WCS_VERSION_8_0 == wcsVersion ? USERSEGMENT1_ID_V80 :
            (WCS_VERSION_9_0 == wcsVersion ? USERSEGMENT1_ID_V90 : USERSEGMENT1_ID);
  }

  public String getUserSegment2Id() {
    return WCS_VERSION_8_0 == wcsVersion ? USERSEGMENT2_ID_V80 :
            (WCS_VERSION_9_0 == wcsVersion ? USERSEGMENT2_ID_V90 : USERSEGMENT2_ID);
  }

  public WcsVersion getWcsVersion() {
    return wcsVersion;
  }

  public void setWcsVersion(@NonNull String wcsVersion) {
    this.wcsVersion = WcsVersion.fromVersionString(wcsVersion).orElse(null);
  }
}
