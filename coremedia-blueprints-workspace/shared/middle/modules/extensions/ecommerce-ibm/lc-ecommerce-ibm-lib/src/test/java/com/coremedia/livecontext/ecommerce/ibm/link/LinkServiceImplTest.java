package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class LinkServiceImplTest {

  private static final String HTTP_ASSET_URL = "http://url/to/storefront";
  private static final String HTTPS_ASSET_URL = "https://url/to/storefront";
  private static final String SCHEMELESS_ASSET_URL = "//url/to/storefront";
  private static final String VALID_PREFIX_WITHOUT_SLASHES = "ExtendedCatalog/perfectchef";
  private static final String VALID_PREFIX_LEADING_SLASH = "/ExtendedCatalog/perfectchef";
  private static final String VALID_PREFIX_TRAILED_SLASH = "ExtendedCatalog/perfectchef/";
  private static final String VALID_PREFIX_TRAILED_AND_LEADING_SLASH = "/ExtendedCatalog/perfectchef/";

  private static final String PRODUCT_IMAGE_SEGMENT = "product/url";
  private static final String PRODUCT_IMAGE_SEGMENT_WITH_LEADING_SLASH
          = VALID_PREFIX_TRAILED_AND_LEADING_SLASH + PRODUCT_IMAGE_SEGMENT; // happens when search-based REST handlers are used

  private static final String STORE_ID = "10001";
  private static final CatalogId CATALOG_ID = CatalogId.of("catalog");

  private LinkServiceImpl testling = new LinkServiceImpl();
  private StoreContextImpl storeContext;

  @BeforeEach
  void setUp() {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);

    storeContext = IbmStoreContextBuilder
            .from(commerceConnection, "any-site-id")
            .withStoreId(STORE_ID)
            .withCatalogId(CATALOG_ID)
            .build();

    CurrentStoreContext.set(storeContext);

    testling.setCmsHost("localhost");
    testling.setCommercePreviewUrl("//preview/url");
  }

  @AfterEach
  void teardown() {
    CurrentStoreContext.remove();
  }

  @Test
  void whenHostIsEmpty_IllegalArgumentExceptionIsExpected() {
    testling.setCommerceProductionUrl("");
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    assertThrows(IllegalStateException.class, () -> testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext));
  }

  @Test
  void whenHostIsBlank_IllegalArgumentExceptionIsExpected() {
    testling.setCommerceProductionUrl("    ");
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    assertThrows(IllegalStateException.class, () -> testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext));
  }

  @Test
  void whenAPathPrefixShouldBeAppendedButIsNotGiven_AnIllegalStateExceptionMustBeThrown() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(null);

    assertThrows(IllegalStateException.class, () -> testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext, true));
  }

  //TEST CHECK LOGIC - Without path prefix
  @Test
  void whenAValidHostAndImageUrlIsGiven_AValidURLMustBeBuild() {
    testling.setCommerceProductionUrl(HTTP_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext);

    assertThat(actual).contains("http://url/to/storefront/product/url");
  }

  @Test
  void whenAnInvalidProductImageUrlIsGiven_theUrlIsNull() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    Optional<String> actual = testling.getImageUrl("  ", storeContext);

    assertThat(actual).isNotPresent();
  }

  @Test
  void whenAHttpAssetUrlIsGiven_AHttpUrlMustBeBuild() {
    testling.setCommerceProductionUrl(HTTP_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext);

    assertThat(actual).contains("http://url/to/storefront/product/url");
  }

  @Test
  void whenAHttpsAssetUrlIsGiven_AHttpsUrlMustBeBuild() {
    testling.setCommerceProductionUrl(HTTPS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext);

    assertThat(actual).contains("https://url/to/storefront/product/url");
  }

  @Test
  void whenASchemelessAssetUrlIsGiven_ASchemelessUrlMustBeBuild() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext);

    assertThat(actual).contains("//url/to/storefront/product/url");
  }

  @Test
  void whenAnAbsoluteAssetUrlIsGiven_ASchemelessURLMustBeReturned() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);

    Optional<String> actual = testling.getImageUrl("http://url/to/storefront/product/url", storeContext);

    assertThat(actual).contains("//url/to/storefront/product/url");
  }

  //TEST CHECK LOGIC - With path prefix
  @Test
  void whenAPathPrefixShouldBeAppendedAndIsGiven_TheURLMustContainThePathPrefix() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_WITHOUT_SLASHES);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext, true);

    assertThat(actual).contains("//url/to/storefront/ExtendedCatalog/perfectchef/product/url");
  }

  @Test
  void whenALeadingSlashPathPrefixShouldBeAppendedAndIsGiven_TheURLMustContainThePathPrefix() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_LEADING_SLASH);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext, true);

    assertThat(actual).contains("//url/to/storefront/ExtendedCatalog/perfectchef/product/url");
  }

  @Test
  void whenAServerRelativePathIsGiven_TheURLMustNotContainThePathPrefix() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_LEADING_SLASH);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT_WITH_LEADING_SLASH, storeContext, true);

    assertThat(actual).contains("//url/to/storefront/ExtendedCatalog/perfectchef/product/url");
  }

  @Test
  void whenATrailedSlashPathPrefixShouldBeAppendedAndIsGiven_TheURLMustContainThePathPrefix() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_TRAILED_SLASH);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext, true);

    assertThat(actual).contains("//url/to/storefront/ExtendedCatalog/perfectchef/product/url");
  }

  @Test
  void whenASlashesPathPrefixShouldBeAppendedAndIsGiven_TheURLMustContainThePathPrefix() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_TRAILED_AND_LEADING_SLASH);

    Optional<String> actual = testling.getImageUrl(PRODUCT_IMAGE_SEGMENT, storeContext, true);

    assertThat(actual).contains("//url/to/storefront/ExtendedCatalog/perfectchef/product/url");
  }

  @Test
  void testReplaceToken() {
    testling.setCommerceProductionUrl(SCHEMELESS_ASSET_URL);
    testling.setCatalogPathPrefix(VALID_PREFIX_TRAILED_AND_LEADING_SLASH);

    Optional<String> actual = testling.getImageUrl("//[cmsHost]/[storeId]/[catalogId]", storeContext, true);

    assertThat(actual).contains("//localhost/" + STORE_ID + "/" + CATALOG_ID.value());
  }
}
