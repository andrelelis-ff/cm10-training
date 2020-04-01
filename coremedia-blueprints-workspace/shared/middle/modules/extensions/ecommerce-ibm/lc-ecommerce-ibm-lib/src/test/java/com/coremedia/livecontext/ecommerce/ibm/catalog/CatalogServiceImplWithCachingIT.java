package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({SwitchableHoverflyExtension.class, SpringExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_CatalogServiceImplWithCachingIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class CatalogServiceImplWithCachingIT extends IbmServiceTestBase {

  //Rest response values
  private static final String PRODUCT_CODE = "GFR033_3303";
  private static final String PRODUCT_SEO = "mangoes";
  private static final String SKU_CODE = "GFR033_330301";
  private static final String CATEGORY_CODE = "Fruit";
  private static final String CATEGORY_SEO = "medicine";

  @Inject
  WcRestConnector wcRestConnector;
  @Inject
  WcCatalogWrapperService catalogWrapperService;
  @Inject
  CatalogServiceImpl testling;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();
    testling.getCommerceCache().setEnabled(true);
    commerceCache.setEnabled(true);
  }

  @Test
  void testProductCachingIsActive() {
    testling.getCommerceCache().clear();
    Product product1 = getTestProduct(storeContext);
    assertEquals(PRODUCT_CODE, product1.getExternalId());
    Product product2 = getTestProduct(storeContext);
    assertEquals(PRODUCT_CODE, product2.getExternalId());
    assertEquals("product beans must be equal", product1, product2);
    assertNotSame("product beans must not be the same instance", product1, product2);

    Map<String, Object> productWrapper1 = (Map<String, Object>) getNotAccessibleMethodValue(product1, "getDelegate");
    Map<String, Object> productWrapper2 = (Map<String, Object>) getNotAccessibleMethodValue(product2, "getDelegate");
    assertSame("product delegates should be equal because of they are cached", productWrapper1, productWrapper2);
  }

  /**
   * Attention: This test runs to long hence it is not intended to run in the hoverfly mode
   * (that is used form the master pipeline). We should only run it when we test against the
   * real backend system (because it takes longer anyway).
   * To achieve this we test if the "hoverfly.ignoreTapes" Java property is set to "true".
   */
  @Test
  void testProductCacheEntryIsCorrectlyTimedOut() {
    if (useTapes()) {
      return;
    }

    CommerceCache commerceCache = testling.getCommerceCache();
    commerceCache.clear();

    // Reduce cache duration of products for testing.
    Map<String, Long> origCacheTimesInSeconds = commerceCache.getCacheTimesInSeconds();
    Map<String, Long> tempCacheTimesInSeconds = new HashMap<>(origCacheTimesInSeconds);
    tempCacheTimesInSeconds.put("Product", 3L);
    commerceCache.setCacheTimesInSeconds(tempCacheTimesInSeconds);

    try {
      Product product1 = getTestProduct(storeContext);
      assertEquals(PRODUCT_CODE, product1.getExternalId());
      Map<String, Object> productWrapper1 = (Map<String, Object>) getNotAccessibleMethodValue(product1, "getDelegate");
      // we assume a cache duration time of 30 seconds or lesser
      sleepForSeconds(5);
      Product product2 = getTestProduct(storeContext);
      Map<String, Object> productWrapper2 = (Map<String, Object>) getNotAccessibleMethodValue(product2, "getDelegate");
      assertEquals(PRODUCT_CODE, product2.getExternalId());
      assertNotSame("the first cache entry should be timed out in the meantime", productWrapper1, productWrapper2);
    } finally {
      commerceCache.setCacheTimesInSeconds(origCacheTimesInSeconds);
    }
  }

  @Test
  void testProductIsCached1() {
    testling.getCommerceCache().clear();
    accessProductByExternalId();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessProductByExternalId();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testProductIsCached2() {
    testling.getCommerceCache().clear();
    accessProductByExternalTechId();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessProductByExternalTechId();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testProductIsCached3() {
    testling.getCommerceCache().clear();
    accessProductBySeoSegment();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessProductBySeoSegment();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testProductVariantIsCached1() {
    testling.getCommerceCache().clear();
    accessProductVariantByExternalId();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessProductVariantByExternalId();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testProductVariantIsCached2() {
    testling.getCommerceCache().clear();
    accessProductVariantByExternalTechId();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessProductVariantByExternalTechId();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testCategoryIsCached1() {
    testling.getCommerceCache().clear();
    accessCategoryByExternalId();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessCategoryByExternalId();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testCategoryIsCached2() {
    testling.getCommerceCache().clear();
    accessCategoryByExternalTechId();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessCategoryByExternalTechId();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  @Test
  void testCategoryIsCached3() {
    testling.getCommerceCache().clear();
    accessCategoryBySeoSegment();
    WcRestConnector instrumentedConnector = instrumentConnector();
    accessCategoryBySeoSegment();
    verifyConnectorIsNotCalled(instrumentedConnector);
  }

  private void verifyConnectorIsNotCalled(WcRestConnector connector) {
    verify(connector, times(0)).callServiceInternal(
            any(WcRestServiceMethod.class),
            any(List.class),
            any(Map.class),
            any(Object.class),
            any(StoreContext.class),
            any(UserContext.class)
    );
  }

  private void accessProductByExternalId() {
    Product product = getTestProduct(storeContext);
    accessProduct(product);
  }

  private Product getTestProduct(StoreContext storeContext) {
    CommerceId commerceId = IbmCommerceIdProvider
            .commerceId(PRODUCT)
            .withExternalId(PRODUCT_CODE)
            .build();

    return testling.findProductById(commerceId, storeContext);
  }

  private void accessProductByExternalTechId() {
    Product product = getTestProduct(storeContext);
    String techId = product.getExternalTechId();
    Product product2 = testling.findProductByExternalTechId(techId, storeContext);
    accessProduct(product2);
  }

  private void accessProductBySeoSegment() {
    Product product = testling.findProductBySeoSegment(PRODUCT_SEO, storeContext);
    accessProduct(product);
  }

  private void accessProductVariantByExternalId() {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId productVariantId = ibmCommerceIdProvider.formatProductVariantId(catalogAlias, SKU_CODE);
    ProductVariant sku = testling.findProductVariantById(productVariantId, storeContext);
    accessProductVariant(sku);
  }

  private void accessProductVariantByExternalTechId() {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId productVariantId = ibmCommerceIdProvider.formatProductVariantId(catalogAlias, SKU_CODE);
    ProductVariant sku = testling.findProductVariantById(productVariantId, storeContext);
    String techId = sku.getExternalTechId();
    CommerceId productVariantTechId = ibmCommerceIdProvider.formatProductVariantTechId(catalogAlias, techId);
    ProductVariant sku2 = testling.findProductVariantById(productVariantTechId, storeContext);
    accessProductVariant(sku2);
  }

  private void accessProduct(Product product) {
    product.getName();
    Category category = product.getCategory();
    category.getName();
    List<ProductVariant> variants = product.getVariants();
    for (ProductVariant variant : variants) {
      variant.getName();
//      variant.getOfferPrice();
      variant.getParent().getName();
    }
//    product.getOfferPrice();
  }

  private void accessProductVariant(ProductVariant sku) {
    sku.getName();
    Category category = sku.getCategory();
    category.getName();
    List<ProductVariant> variants = sku.getVariants();
    for (ProductVariant variant : variants) {
      variant.getName();
//      variant.getOfferPrice();
      variant.getParent().getName();
    }
//    sku.getOfferPrice();
  }

  private void accessCategoryByExternalId() {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId categoryId = ibmCommerceIdProvider.formatCategoryId(catalogAlias, CATEGORY_CODE);
    Category category = testling.findCategoryById(categoryId, storeContext);
    accessCategory(category);
  }

  private void accessCategoryByExternalTechId() {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId categoryId = ibmCommerceIdProvider.formatCategoryId(catalogAlias, CATEGORY_CODE);
    Category category = testling.findCategoryById(categoryId, storeContext);
    String techId = category.getExternalTechId();
    CommerceId categoryTechId = ibmCommerceIdProvider.formatCategoryTechId(catalogAlias, techId);
    Category category2 = testling.findCategoryById(categoryTechId, storeContext);
    accessCategory(category2);
  }

  private void accessCategoryBySeoSegment() {
    Category category = testling.findCategoryBySeoSegment(CATEGORY_SEO, storeContext);
    accessCategory(category);
  }

  private void accessCategory(Category category) {
    category.getName();
    category.getBreadcrumb();
    List<Product> products = testling.findProductsByCategory(category);
    for (Product product : products) {
      product.getName();
    }
  }

  private WcRestConnector instrumentConnector() {
    WcRestConnector connector = spy(wcRestConnector);
    catalogWrapperService.setRestConnector(connector);
    return connector;
  }

  private Object getNotAccessibleMethodValue(Object object, String methodName) {
    try {
      Method method = object.getClass().getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method.invoke(object);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private void sleepForSeconds(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
