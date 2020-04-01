package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for BOD REST interface.
 */
@ExtendWith({SwitchableHoverflyExtension.class, SpringExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_CatalogServiceImplBodBasedIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = {IbmServiceTestBase.LocalConfig.class, CatalogServiceImplBodBasedIT.LocalConfig.class})
public class CatalogServiceImplBodBasedIT extends IbmCatalogServiceBaseTest {

  private static final String ROOT_CATEGORY_ID = "ROOT";

  @Configuration
  @ImportResource(
          value = {
                  "classpath:/com.coremedia.livecontext.ecommerce.ibm.service/test-commerce-services-bod-customizers.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(XmlRepoConfiguration.class)
  @Profile(IbmServiceTestBase.LocalConfig.PROFILE)
  public static class LocalConfig {
  }

  @Test
  @Override
  public void testFindCategoryById() throws Exception {
    super.testFindCategoryById();
  }

  @Test
  @Override
  public void testFindProductByIdNotFound() throws Exception {
    super.testFindProductByIdNotFound();
  }

  @Test
  @Override
  public void testFindProductByExternalTechId() throws Exception {
    super.testFindProductByExternalTechId();
  }

  @Test
  @Override
  public void testFindProductByExternalTechIdIsNull() throws Exception {
    super.testFindProductByExternalTechIdIsNull();
  }

  @Test
  @Override
  public void testFindProductVariantById() throws Exception {
    super.testFindProductVariantById();
  }

  @Test
  @Override
  public void testFindProductVariantByExternalTechId() throws Exception {
    super.testFindProductVariantByExternalTechId();
  }

  @Test
  @Override
  public void testFindProductBySeoSegment() throws Exception {
    super.testFindProductBySeoSegment();
  }

  @Test
  @Override
  public void testFindProductBySeoSegmentIsNull() throws Exception {
    super.testFindProductBySeoSegmentIsNull();
  }

  @Test
  @Override
  public void testFindProductsByCategory() throws Exception {
    super.testFindProductsByCategory();
  }

  @Test
  @Override
  public void testFindProductsByCategoryIsEmpty() throws Exception {
    super.testFindProductsByCategoryIsEmpty();
  }

  @Test
  void testFindProductsByCategoryIsRoot() {
    testFindProductsByCategoryIsRoot(ROOT_CATEGORY_ID);
  }

  @Test
  @Override
  public void testSearchProducts() throws Exception {
    super.testSearchProducts();
  }

  @Test
  @Override
  public void testSearchProductVariants() throws Exception {
    super.testSearchProductVariants();
  }

  @Test
  @Override
  public void testFindTopCategories() throws Exception {
    super.testFindTopCategories();
  }

  @Test
  @Override
  public void testFindSubCategories() throws Exception {
    super.testFindSubCategories();
  }

  @Test
  @Override
  public void testFindSubCategoriesIsEmpty() throws Exception {
    super.testFindSubCategoriesIsEmpty();
  }

  @Test
  @Override
  public void testFindCategoryByExternalTechId() throws Exception {
    super.testFindCategoryByExternalTechId();
  }

  @Test
  @Override
  public void testFindCategoryByExternalTechIdIsNull() throws Exception {
    super.testFindCategoryByExternalTechIdIsNull();
  }

  @Test
  @Override
  public void testFindCategoryBySeoSegment() throws Exception {
    super.testFindCategoryBySeoSegment();
  }

  @Test
  @Override
  public void testFindCategoryBySeoSegmentIsNull() throws Exception {
    super.testFindCategoryBySeoSegmentIsNull();
  }

  @Test
  @Override
  public void testFindCategoryByIdIsNull() {
    super.testFindCategoryByIdIsNull();
  }

  @Test
  @Override
  public void testWithStoreContext() {
    super.testWithStoreContext();
  }

  @Test
  @Override
  public void testFindProductById() throws Exception {
    super.testFindProductById();
  }

  @Test
  @Override
  public void testFindProductVariantByExternalIdWithContractSupport() throws Exception {
    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }
    super.testFindProductVariantByExternalIdWithContractSupport();
  }

  @Test
  @Override
  public void testFindProductMultiSEOByExternalTechId() throws Exception {
    super.testFindProductMultiSEOByExternalTechId();
  }

  @Test
  void testFindRootCategory() {
    testFindRootCategory(ROOT_CATEGORY_ID);
  }

  @Test
  @Override
  public void testFindCategoryMultiSEOByExternalTechId() {
    CommerceId categoryId = getIdProvider().formatCategoryId(storeContext.getCatalogAlias(), CATEGORY1_WITH_MULTI_SEO);
    Category category = testling.findCategoryById(categoryId, storeContext);
    String seoSegment = category.getSeoSegment();
    assertNotNull(seoSegment);
    assertFalse(seoSegment.contains(";"));
  }

  @Test
  @Override
  public void testFindGermanCategoryBySeoSegment() throws Exception {
    super.testFindGermanCategoryBySeoSegment();
  }
}
