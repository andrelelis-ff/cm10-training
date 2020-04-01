package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion;
import com.coremedia.livecontext.ecommerce.search.SearchFacet;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Search REST interface.
 */
@ExtendWith({SwitchableHoverflyExtension.class, SpringExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_CatalogServiceImplSearchBasedIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
public class CatalogServiceImplSearchBasedIT extends IbmCatalogServiceBaseTest {

  private static final String ROOT_CATEGORY_ID = "ROOT";

  @Test
  @Override
  public void testFindProductById() throws Exception {
    super.testFindProductById();
  }

  @Test
  @Override
  public void testFindB2BProductFromMultiCatalogs() throws Exception {
    super.testFindB2BProductFromMultiCatalogs();
  }

  @Test
  @Override
  public void testFindCategoryById() throws Exception {
    super.testFindCategoryById();
  }

  @Test
  @Override
  public void testFindB2BCategoryFromMultiCatalogs() throws Exception {
    super.testFindB2BCategoryFromMultiCatalogs();
  }

  @Test
  @Override
  public void testFindProductByIdWithSlash() {
    if (testConfig.getWcsVersion().lessThan(WCS_VERSION_8_0)) {
      return;
    }
    super.testFindProductByIdWithSlash();
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
  public void testFindProductMultiSEOByExternalTechId() throws Exception {
    super.testFindProductMultiSEOByExternalTechId();
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
  public void testFindProductVariantByExternalIdWithContractSupport() throws Exception {
    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }
    super.testFindProductVariantByExternalIdWithContractSupport();
  }

  @Test
  @Override
  public void testFindProductVariantByIdWithSlash() throws Exception {
    if (testConfig.getWcsVersion().lessThan(WCS_VERSION_8_0)) {
      return;
    }
    super.testFindProductVariantByIdWithSlash();
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
  public void testSortedSearchProducts() throws Exception {
    WcsVersion wcsVersion = testConfig.getWcsVersion();

    // Tests moved here from base class
    CommerceId categoryId1 = getIdProvider().formatCategoryId(null, LEAF_CATEGORY_CODE);
    Category category1 = this.testling.findCategoryById(categoryId1, storeContext);
    Map<String, String> searchParams1 = new HashMap<>();
    searchParams1.put(CatalogService.SEARCH_PARAM_CATEGORYID, category1.getExternalTechId());
    searchParams1.put(CatalogService.SEARCH_PARAM_ORDERBY, SEARCH_ORDER_BY_PRICE_ASC);
    SearchResult<Product> searchProducts1 = this.testling.searchProducts(SEARCH_TERM_1, searchParams1, storeContext);
    assertThat(searchProducts1).isNotNull();
    int total1 = searchProducts1.getTotalCount();
    List<Product> products1 = searchProducts1.getSearchResult();
    int counter1 = 1;
    while (counter1 < total1) {
      Product previousProduct1 = products1.get(counter1 - 1);
      Product currentProduct1 = products1.get(counter1);
      if (WcsVersion.WCS_VERSION_8_0.lessThan(wcsVersion)) {
        BigDecimal previousProductOfferPrice = previousProduct1.getOfferPrice();
        BigDecimal currentProductOfferPrice = currentProduct1.getOfferPrice();
        assertThat(previousProductOfferPrice).isLessThanOrEqualTo(currentProductOfferPrice);
      } else {
        BigDecimal previousProductListPrice = previousProduct1.getListPrice();
        BigDecimal currentProductListPrice = currentProduct1.getListPrice();
        assertThat(previousProductListPrice).isLessThanOrEqualTo(currentProductListPrice);
      }
      counter1++;
    }

    searchParams1 = new HashMap<>();
    searchParams1.put(CatalogService.SEARCH_PARAM_CATEGORYID, category1.getExternalTechId());
    searchParams1.put(CatalogService.SEARCH_PARAM_ORDERBY, SEARCH_ORDER_BY_PRICE_DESC);
    searchProducts1 = this.testling.searchProducts(SEARCH_TERM_1, searchParams1, storeContext);
    assertThat(searchProducts1).isNotNull();
    total1 = searchProducts1.getTotalCount();
    products1 = searchProducts1.getSearchResult();
    counter1 = 1;
    while (counter1 < total1) {
      Product previousProduct1 = products1.get(counter1 - 1);
      Product currentProduct1 = products1.get(counter1);
      if (WcsVersion.WCS_VERSION_8_0.lessThan(wcsVersion)) {
        BigDecimal previousProductOfferPrice = previousProduct1.getOfferPrice();
        BigDecimal currentProductOfferPrice = currentProduct1.getOfferPrice();
        assertThat(previousProductOfferPrice).isGreaterThanOrEqualTo(currentProductOfferPrice);
      } else {
        BigDecimal previousProductListPrice = previousProduct1.getListPrice();
        BigDecimal currentProductListPrice = currentProduct1.getListPrice();
        assertThat(previousProductListPrice).isGreaterThanOrEqualTo(currentProductListPrice);
      }
      counter1++;
    }

    //additional tests
    CommerceId categoryId = getIdProvider().formatCategoryId(null, LEAF_CATEGORY_CODE);
    Category category = testling.findCategoryById(categoryId, storeContext);
    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(CatalogService.SEARCH_PARAM_CATEGORYID, category.getExternalTechId());
    searchParams.put(CatalogService.SEARCH_PARAM_ORDERBY, "ORDER_BY_TYPE_CATEGORY_ASC");
    SearchResult<Product> searchProducts = testling.searchProducts(SEARCH_TERM_1, searchParams, storeContext);
    assertThat(searchProducts).isNotNull();
    int total = searchProducts.getTotalCount();
    List<Product> products = searchProducts.getSearchResult();
    int counter = 1;
    while (counter < total) {
      Product previousProduct = products.get(counter - 1);
      Product currentProduct = products.get(counter);
      String previousProductName = previousProduct.getName();
      String currentProductName = currentProduct.getName();
      assertThat(previousProductName.compareTo(currentProductName) < 0).isTrue();
      counter++;
    }
  }

  @Test
  @Override
  public void testSearchProductsWithOffset() throws Exception {
    super.testSearchProductsWithOffset();
  }

  @Test
  @Override
  public void testGetFacetSearchProducts() throws Exception {
    super.testGetFacetSearchProducts();
  }

  @Test
  void testSearchProductsWithFacet() {
    CommerceId categoryId = getIdProvider().formatCategoryId(null, TOP_CATEGORY_CODE);
    Category category = testling.findCategoryById(categoryId, storeContext);
    Map<String, String> searchParams = new HashMap<>();
    String facet = testConfig.getWcsVersion().lessThan(WCS_VERSION_9_0)
            ? "price_USD:({200 300} 300)"
            : "OfferPrice_USD:(price_USD_10005:([200+TO+300]))";

    searchParams.put(CatalogService.SEARCH_PARAM_FACET, facet);
    searchParams.put(CatalogService.SEARCH_PARAM_CATEGORYID, category.getExternalTechId());

    searchParams.put(CatalogService.SEARCH_PARAM_TOTAL, "1");
    searchParams.put(CatalogService.SEARCH_PARAM_PAGENUMBER, "1");
    searchParams.put(CatalogService.SEARCH_PARAM_PAGESIZE, "1");

    SearchResult<Product> searchProducts = testling.searchProducts("*", searchParams, storeContext);
    assertThat(searchProducts).isNotNull();
    assertThat(searchProducts.getTotalCount()).isEqualTo(2);
    List<Product> searchResult = searchProducts.getSearchResult();
    assertThat(searchResult).isNotNull();
    for (Product product : searchResult) {
      assertThat(product.getOfferPrice().floatValue())
              .isGreaterThanOrEqualTo(200)
              .isLessThanOrEqualTo(300);
    }

    searchParams = new HashMap<>();
    facet = "mfName_ntk_cs%3A%22Adelee+Plus%22";
    searchParams.put(CatalogService.SEARCH_PARAM_FACET, facet);
    searchParams.put(CatalogService.SEARCH_PARAM_CATEGORYID, category.getExternalTechId());
    searchProducts = testling.searchProducts("*", searchParams, storeContext);
    assertThat(searchProducts).isNotNull();
    assertThat(searchProducts.getTotalCount()).isEqualTo(7);
    searchResult = searchProducts.getSearchResult();
    assertThat(searchResult).isNotNull();
  }

  @Test
  @Override
  public void testSearchProductVariants() throws Exception {
    super.testSearchProductVariants();
  }

  @Test
  @Override
  public void testGetCatalogs() {
    super.testGetCatalogs();
  }

  @Test
  @Override
  public void testGetDefaultCatalog() {
    super.testGetDefaultCatalog();
  }

  @Test
  void testSearchFacetsProductVariants() {
    SearchResult<ProductVariant> searchResult = testling.searchProductVariants(SEARCH_TERM_1 + " " + SEARCH_TERM_2, emptyMap(), storeContext);
    assertNotNull(searchResult);

    List<SearchFacet> facets = searchResult.getFacets();
    assertNotNull(facets);
    assertTrue(!facets.isEmpty());
    assertSearchFacet(facets.get(0));
  }

  @Test
  void testSearchFacetsProducts() {
    super.testSearchFacetsProducts(SEARCH_TERM_1, new HashMap<>());
  }

  @Test
  @Override
  public void testFindTopCategories() throws Exception {
    super.testFindTopCategories();
  }

  @Test
  @Override
  public void testFindTopCategoriesWithContractSupport() throws Exception {
    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }
    super.testFindTopCategoriesWithContractSupport();
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
  public void testFindCategoryMultiSEOByExternalTechId() throws Exception {
    super.testFindCategoryMultiSEOByExternalTechId();
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
  public void testFindGermanCategoryBySeoSegment() throws Exception {
    super.testFindGermanCategoryBySeoSegment();
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
  public void testFindRootCategory() {
    testFindRootCategory(ROOT_CATEGORY_ID);
  }

  @Test
  @Override
  public void testFindCategoryByIdWithSlash() {
    if (testConfig.getWcsVersion().lessThan(WCS_VERSION_8_0)) {
      return;
    }
    super.testFindCategoryByIdWithSlash();
  }

  @Test
  @Override
  public void testWithStoreContext() {
    super.testWithStoreContext();
  }
}
