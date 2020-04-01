package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.id.IdScheme;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper.format;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({SwitchableHoverflyExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_CommerceIdSchemeIT.json"
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
class CommerceIdSchemeIT extends IbmServiceTestBase {

  private static final String PRODUCT = "AuroraWMDRS-1";
  private static final String CATEGORY = "Women";

  private static final String PRODUCT_ID = "ibm:///catalog/product/" + PRODUCT;
  private static final String SKU_ID_PREFIX = "ibm:///catalog/sku/";
  private static final String CATEGORY_ID = "ibm:///catalog/category/" + CATEGORY;

  @Inject
  private IdScheme commerceIdScheme;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @Test
  void testInvalidBeanId() {
    Object o = commerceIdScheme.parseId("ibm://catalog/bla/blub");
    assertEquals(o, IdScheme.CANNOT_HANDLE);
  }

  @Test
  void testProductId() {
    Product product = (Product) commerceIdScheme.parseId(PRODUCT_ID);
    assertNotNull("bean most not be null", product);
    assertEquals(PRODUCT, product.getExternalId());
    String id = commerceIdScheme.getId(product);
    assertEquals(PRODUCT_ID, id);
  }

  @Test
  void testProductNotFound() {
    Product product = (Product) commerceIdScheme.parseId("ibm:///catalog/product/blablub");
    assertThrows(NotFoundException.class, () -> assertEquals(PRODUCT, product.getExternalId()));
  }

  @Test
  void testProductVariantId() {
    Product product = (Product) commerceIdScheme.parseId(PRODUCT_ID);
    List<ProductVariant> variants = product.getVariants();
    ProductVariant sku = variants.get(0);
    String skuTechId = sku.getExternalTechId();
    String skuId = format(ibmCommerceIdProvider.formatProductVariantTechId(storeContext.getCatalogAlias(), skuTechId));
    ProductVariant sku2 = (ProductVariant) commerceIdScheme.parseId(skuId);
    assertNotNull("bean most not be null", sku2);
    assertEquals(sku, sku2);
    String id = commerceIdScheme.getId(sku);
    assertEquals(skuId, id);
  }

  @Test
  void testProductVariantNotFound() {
    ProductVariant sku = (ProductVariant) commerceIdScheme.parseId(SKU_ID_PREFIX + "blablub");
    assertThrows(NotFoundException.class, () -> assertEquals("blablub", sku.getExternalId()));
  }

  @Test
  void testCategoryId() {
    Category category = (Category) commerceIdScheme.parseId(CATEGORY_ID);
    assertNotNull("bean most not be null", category);
    assertEquals(CATEGORY, category.getExternalId());
    String id = commerceIdScheme.getId(category);
    assertEquals(CATEGORY_ID, id);
  }

  @Test
  void testCategoryNotFound() {
    Category category = (Category) commerceIdScheme.parseId("ibm:///catalog/category/blablub");
    assertThrows(NotFoundException.class, () -> assertEquals(CATEGORY, category.getExternalId()));
  }
}
