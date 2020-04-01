package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.lc.test.CatalogServiceBaseTest;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.AxisFilter;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductAttribute;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.contract.ContractService;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmTestConfig;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_8;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public abstract class IbmCatalogServiceBaseTest extends CatalogServiceBaseTest {

  protected static final String BEAN_NAME_CATALOG_SERVICE = "catalogService";

  public static final String IBM_TEST_URL = "shop-ref.ecommerce.coremedia.com";

  private static final String PRODUCT_VARIANT_CODE_B2B = "FB041_410101";

  private static final String PRODUCT1_WITH_MULTI_SEO = "PC_WALL_CLOCK";
  private static final String PRODUCT2_WITH_MULTI_SEO = "PC_CANDELABRA";
  private static final String PRODUCT3_WITH_MULTI_SEO = "PC_FRUITBOWL";

  protected static final String CATEGORY1_WITH_MULTI_SEO = "PC_Magazines";
  private static final String CATEGORY2_WITH_MULTI_SEO = "PC_ToEat";
  private static final String CATEGORY3_WITH_MULTI_SEO = "PC_Glasses";

  @Inject
  @Named(BEAN_NAME_CATALOG_SERVICE)
  protected CatalogServiceImpl testling;

  @Inject
  protected ContractService contractService;

  @Inject
  protected IbmTestConfig testConfig;

  @Inject
  protected StoreInfoService storeInfoService;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @MockBean
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @BeforeEach
  @Override
  public void setup() {
    doAnswer(invocationOnMock -> Optional.of(connection))
            .when(commerceConnectionInitializer).findConnectionForSite(any(Site.class));

    storeInfoService.getWcsVersion().ifPresent(testConfig::setWcsVersion);

    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      WcRestConnector restConnector = testling.getCatalogWrapperService().getRestConnector();
      String searchServiceSslEndpoint = restConnector.getSearchServiceSslEndpoint(storeContext).replace("previewresources", "resources");
      restConnector.setSearchServiceSslEndpoint(searchServiceSslEndpoint);
    }
    super.setup();
  }

  protected void testFindProductVariantByExternalIdWithContractSupport() throws Exception {
    StoreContextImpl storeContext = testConfig.getB2BStoreContext(connection);

    if (useTapes() || StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_8)) {
      return;
    }

    connection.setInitialStoreContext(storeContext);

    CommerceId commerceId = ibmCommerceIdProvider.formatProductVariantId(storeContext.getCatalogAlias(),
            PRODUCT_VARIANT_CODE_B2B);

    resetUserContext();

    ProductVariant productVariantWithoutContract = testling.findProductVariantById(commerceId, storeContext);
    assertNotNull(productVariantWithoutContract);

    BigDecimal offerPriceWithoutContract = productVariantWithoutContract.getOfferPrice();
    assertNotNull(offerPriceWithoutContract);

    // Add contract IDs to store context.
    storeContext = prepareContextsForContractBasedPreview(storeContext);

    ProductVariant productVariantWithContract = testling.findProductVariantById(commerceId, storeContext);
    BigDecimal offerPriceWithContract = productVariantWithContract.getOfferPrice();
    assertNotNull(offerPriceWithContract);

    assertTrue("Contract price for product should be lower",
            offerPriceWithoutContract.floatValue() > offerPriceWithContract.floatValue());
  }

  protected void testFindProductByExternalTechId() throws Exception {
    Product product1 = getTestProductByExternalId(PRODUCT_CODE);

    Product product2 = testling.findProductByExternalTechId(product1.getExternalTechId(), storeContext);
    assertProduct(product2);
  }

  private Product getTestProductByExternalId(String externalId) {
    CommerceId commerceId = IbmCommerceIdProvider
            .commerceId(PRODUCT)
            .withExternalId(externalId)
            .build();

    return testling.findProductById(commerceId, storeContext);
  }

  protected void testFindProductByExternalTechIdIsNull() throws Exception {
    Product product = testling.findProductByExternalTechId("blablablub", storeContext);
    assertNull(product);
  }

  protected void testFindProductMultiSEOByExternalTechId() throws Exception {
    String lowerCaseStoreName = storeContext.getStoreName().toLowerCase();

    Product product = getTestProductByExternalId(PRODUCT1_WITH_MULTI_SEO);
    assertNotNull(product.getSeoSegment());
    assertTrue(product.getSeoSegment().contains(lowerCaseStoreName));

    product = getTestProductByExternalId(PRODUCT2_WITH_MULTI_SEO);
    assertNotNull(product.getSeoSegment());
    assertTrue(product.getSeoSegment().contains(lowerCaseStoreName));

    product = getTestProductByExternalId(PRODUCT3_WITH_MULTI_SEO);
    assertNotNull(product.getSeoSegment());
    assertFalse(product.getSeoSegment().contains(lowerCaseStoreName));
  }

  protected void testFindProductVariantByExternalTechId() throws Exception {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId productVariantId = ibmCommerceIdProvider.formatProductVariantId(catalogAlias, PRODUCT_VARIANT_CODE);
    ProductVariant productVariant = testling.findProductVariantById(productVariantId, storeContext);
    assertEquals(PRODUCT_VARIANT_CODE, productVariant.getExternalId());

    String techId = productVariant.getExternalTechId();
    CommerceId productVariantTechId = ibmCommerceIdProvider.formatProductVariantTechId(catalogAlias, techId);
    ProductVariant productVariant2 = testling.findProductVariantById(productVariantTechId, storeContext);
    assertEquals(productVariant, productVariant2);
    assertProductVariant(productVariant2);
  }

  protected void testFindTopCategoriesWithContractSupport() throws Exception {
    StoreContextImpl storeContext = testConfig.getB2BStoreContext(connection);

    if (useTapes() || StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_8)) {
      return;
    }

    connection.setInitialStoreContext(storeContext);

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    List<Category> topCategories = testling.findTopCategories(catalogAlias, storeContext);
    int topCategoriesCount = topCategories.size();

    storeContext = prepareContextsForContractBasedPreview(storeContext);

    List<Category> topCategoriesContract = testling.findTopCategories(catalogAlias, storeContext);
    int topCategoriesContractCount = topCategoriesContract.size();

    assertTrue("Contract filter for b2b topcategories not working", topCategoriesCount > topCategoriesContractCount);
  }

  protected void testFindCategoryByExternalTechId() throws Exception {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId categoryId = ibmCommerceIdProvider.formatCategoryId(catalogAlias, CATEGORY_CODE);
    Category category1 = testling.findCategoryById(categoryId, storeContext);

    CommerceId categoryTechId = ibmCommerceIdProvider.formatCategoryTechId(catalogAlias, category1.getExternalTechId());
    Category category2 = testling.findCategoryById(categoryTechId, storeContext);
    assertCategory(category2);
  }

  protected void testFindCategoryMultiSEOByExternalTechId() throws Exception {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    String lowerCaseStoreName = storeContext.getStoreName().toLowerCase();

    CommerceId categoryId = ibmCommerceIdProvider.formatCategoryId(catalogAlias, CATEGORY1_WITH_MULTI_SEO);
    Category category = testling.findCategoryById(categoryId, storeContext);
    assertNotNull(category.getSeoSegment());
    assertTrue(category.getSeoSegment().contains(lowerCaseStoreName));

    CommerceId categoryId1 = ibmCommerceIdProvider.formatCategoryId(catalogAlias, CATEGORY2_WITH_MULTI_SEO);
    category = testling.findCategoryById(categoryId1, storeContext);
    assertNotNull(category.getSeoSegment());
    assertTrue(category.getSeoSegment().contains(lowerCaseStoreName));

    CommerceId categoryId2 = ibmCommerceIdProvider.formatCategoryId(catalogAlias, CATEGORY3_WITH_MULTI_SEO);
    category = testling.findCategoryById(categoryId2, storeContext);
    assertNotNull(category.getSeoSegment());
    assertFalse(category.getSeoSegment().contains(lowerCaseStoreName));
  }

  protected void testFindCategoryByExternalTechIdIsNull() throws Exception {
    CommerceId categoryTechId = ibmCommerceIdProvider.formatCategoryTechId(storeContext.getCatalogAlias(), "blablablub");
    Category category = testling.findCategoryById(categoryTechId, storeContext);
    assertNull(category);
  }

  private void resetUserContext() {
    UserContext userContext = UserContext.builder().build();
    CurrentUserContext.set(userContext);
  }

  private StoreContextImpl prepareContextsForContractBasedPreview(@NonNull StoreContextImpl storeContext) {
    UserContext userContext = UserContext.builder().withUserName(testConfig.getPreviewUserName()).build();
    CurrentUserContext.set(userContext);

    Collection<Contract> contracts = contractService.findContractIdsForUser(userContext, storeContext);
    assertNotNull(contracts);

    Iterator<Contract> iterator = contracts.iterator();
    Contract contract = null;
    while (iterator.hasNext()) {
      contract = iterator.next();
      String contractName = contract.getName();
      if (contractName != null && contractName.contains("Applicances Expert")) {
        break;
      }
    }
    assertNotNull(contract);

    List<String> contractIdsForPreview = singletonList(contract.getExternalTechId());
    return IbmStoreContextBuilder
            .from(storeContext)
            .withContractIdsForPreview(contractIdsForPreview)
            .build();
  }

  @Override
  protected void assertProduct(Product product) {
    super.assertProduct(product);

    // Test attributes.
    List<ProductAttribute> definingAttributes = product.getDefiningAttributes();
    assertNotNull(definingAttributes);
    assertThat(definingAttributes.isEmpty(), is(false));

    List<ProductAttribute> describingAttributes = product.getDescribingAttributes();
    assertNotNull(describingAttributes);
    assertThat(describingAttributes.isEmpty(), is(false));

    // Test variants.
    List<ProductVariant> variants = product.getVariants();
    assertNotNull(variants);
    assertThat(variants.isEmpty(), is(false));

    // Test axis filter.
    List<String> variantAxisNames = product.getVariantAxisNames();
    if (!variantAxisNames.isEmpty()) {
      List<ProductVariant> filteredVariants = product.getVariants(AxisFilter.onAnyValue(variantAxisNames.get(0)));
      assertTrue(variants.size() >= filteredVariants.size());
    }
  }

  @Override
  protected void assertProductVariant(ProductVariant productVariant) {
    super.assertProductVariant(productVariant);

    List<ProductAttribute> describingAttributes = productVariant.getDescribingAttributes();
    assertThat(describingAttributes.isEmpty(), is(false));

    List<ProductAttribute> definingAttributes = productVariant.getDefiningAttributes();
    assertThat(definingAttributes.isEmpty(), is(false));
  }
}
