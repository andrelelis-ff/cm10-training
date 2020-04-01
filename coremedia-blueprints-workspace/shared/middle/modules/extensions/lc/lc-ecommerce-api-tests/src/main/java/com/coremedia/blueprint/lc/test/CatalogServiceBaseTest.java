package com.coremedia.blueprint.lc.test;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.AxisFilter;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchFacet;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "livecontext.sfcc.ocapi.shopBasePath=/s/{storeId}/dw/shop/")
public abstract class CatalogServiceBaseTest extends AbstractServiceTest {

  private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceBaseTest.class);

  @Inject
  CatalogService testling;

  @Value("${PRODUCT_CODE}")
  protected String PRODUCT_CODE;

  @Value("${PRODUCT_CODE_B2B}")
  protected String PRODUCT_CODE_B2B;

  @Value("${PRODUCT_NAME}")
  protected String PRODUCT_NAME;

  @Value("${PRODUCT_CODE_WITH_SLASH}")
  protected String PRODUCT_CODE_WITH_SLASH;

  @Value("${PRODUCT_SEO_SEGMENT}")
  protected String PRODUCT_SEO_SEGMENT;

  @Value("${CATEGORY_CODE}")
  protected String CATEGORY_CODE;

  @Value("${CATEGORY_CODE_B2B}")
  protected String CATEGORY_CODE_B2B;

  @Value("${CATEGORY_WITH_SLASH}")
  protected String CATEGORY_WITH_SLASH;

  @Value("${CATEGORY_SEO_SEGMENT}")
  protected String CATEGORY_SEO_SEGMENT;

  @Value("${CATEGORY_SEO_SEGMENT_DE}")
  protected String CATEGORY_SEO_SEGMENT_DE;

  @Value("${CATEGORY_NAME}")
  protected String CATEGORY_NAME;

  @Value("${PRODUCT_VARIANT_CODE}")
  protected String PRODUCT_VARIANT_CODE;

  @Value("${PRODUCT_VARIANT_WITH_SLASH}")
  protected String PRODUCT_VARIANT_WITH_SLASH;

  @Value("${SEARCH_TERM_1}")
  protected String SEARCH_TERM_1;

  @Value("${SEARCH_TERM_2}")
  protected String SEARCH_TERM_2;

  @Value("${SEARCH_TERM_3}")
  protected String SEARCH_TERM_3;

  @Value("${SEARCH_ORDER_BY_PRICE_ASC}")
  protected String SEARCH_ORDER_BY_PRICE_ASC;

  @Value("${SEARCH_ORDER_BY_PRICE_DESC}")
  protected String SEARCH_ORDER_BY_PRICE_DESC;

  @Value("${TOP_CATEGORY_NAME}")
  protected String TOP_CATEGORY_NAME;

  @Value("${TOP_CATEGORY_CODE}")
  protected String TOP_CATEGORY_CODE;

  @Value("${LEAF_CATEGORY_CODE}")
  protected String LEAF_CATEGORY_CODE;

  @Value("${FILTER_NAME}")
  protected String FILTER_NAME;

  @Value("${FILTER_VALUE}")
  protected String FILTER_VALUE;

  protected void testFindProductById() throws Exception {
    CommerceId productId = getIdProvider().formatProductId(null, PRODUCT_CODE);

    Product product = testling.findProductById(productId, storeContext);

    assertThat(product.getExternalId()).isEqualTo(PRODUCT_CODE);
    assertProduct(product);
    assertThat(product.getDefiningAttributes()).isNotEmpty();
  }

  protected void testFindB2BProductFromMultiCatalogs() throws Exception {
    CatalogAlias catalogAlias = CatalogAlias.of("b2b");
    CommerceId productId = getIdProvider().formatProductId(catalogAlias, PRODUCT_CODE_B2B);

    Product product = testling.findProductById(productId, storeContext);
    assertThat(product).isNotNull();

    assertThat(product.getExternalId()).isEqualTo(PRODUCT_CODE_B2B);
    Category category = product.getCategory();
    assertThat(category).isNotNull();
    Category parentCategory = category.getParent();
    assertThat(parentCategory).isNotNull();
    assertThat(parentCategory.isRoot()).isTrue();

    catalogAlias = CatalogAlias.of("master");
    productId = getIdProvider().formatProductId(catalogAlias, PRODUCT_CODE_B2B);
    product = testling.findProductById(productId, storeContext);
    assertThat(product.getExternalId()).isEqualTo(PRODUCT_CODE_B2B);
    category = product.getCategory();
    assertThat(category).isNotNull();
    assertCategory(category);
    parentCategory = category.getParent();
    assertThat(parentCategory).isNotNull();
    assertThat(parentCategory.isRoot()).isFalse();

    productId = getIdProvider().formatProductId(null, PRODUCT_CODE_B2B);
    product = testling.findProductById(productId, storeContext);
    assertThat(product).isNull();
  }

  protected void testFindProductByIdNotFound() throws Exception {
    CommerceId productId = getIdProvider().formatProductId(null, "blablablub");

    Product product = testling.findProductById(productId, storeContext);

    assertThat(product).isNull();
  }

  protected void testFindProductByIdWithSlash() {
    CommerceId productId = getIdProvider().formatProductId(null, PRODUCT_CODE_WITH_SLASH);

    Product product = testling.findProductById(productId, storeContext);

    assertThat(product).isNotNull();
    assertThat(product.getExternalId()).isEqualTo(PRODUCT_CODE_WITH_SLASH);
  }

  protected void testFindProductBySeoSegment() throws Exception {
    Product product = testling.findProductBySeoSegment(PRODUCT_SEO_SEGMENT, storeContext);

    assertThat(product.getSeoSegment()).isEqualTo(PRODUCT_SEO_SEGMENT);
    assertProduct(product);
  }

  protected void testFindProductBySeoSegmentIsNull() throws Exception {
    Product product = testling.findProductBySeoSegment("blablablub", storeContext);

    assertThat(product).isNull();
  }

  protected void testFindProductVariantById() throws Exception {
    CommerceId productVariantId = getIdProvider().formatProductVariantId(null, PRODUCT_VARIANT_CODE);

    ProductVariant productVariant = testling.findProductVariantById(productVariantId, storeContext);
    assertThat(productVariant).isNotNull();
    assertThat(productVariant.getExternalId()).isEqualTo(PRODUCT_VARIANT_CODE);
    assertProductVariant(productVariant);

    assertThat(productVariant.getDefiningAttributes()).isNotEmpty();
//    assertThat(productVariant.getDescribingAttributes()).isNotEmpty();

    Product product = productVariant.getParent();
    assertProduct(product);
  }

  protected void testFindProductVariantByIdWithSlash() throws Exception {
    CommerceId productVariantId = getIdProvider().formatProductVariantId(null, PRODUCT_VARIANT_WITH_SLASH);

    ProductVariant productVariant = testling.findProductVariantById(productVariantId, storeContext);

    assertThat(productVariant).isNotNull();
    assertThat(productVariant.getExternalId()).isEqualTo(PRODUCT_VARIANT_WITH_SLASH);
  }

  protected void testFindProductsByCategory() throws Exception {
    CommerceId productId = getIdProvider().formatProductId(null, PRODUCT_CODE);

    Product product = testling.findProductById(productId, storeContext);
    assertThat(product).isNotNull();

    Category category = product.getCategory();
    List<Product> productsByCategory = testling.findProductsByCategory(category);
    assertThat(productsByCategory).isNotEmpty();
    assertProduct(productsByCategory.get(0));
    assertThat(productsByCategory.get(0).getCategory()).isEqualTo(category);
  }

  protected void testFindProductsByCategoryIsEmpty() throws Exception {
    Category rootCategory = testling.findRootCategory(DEFAULT_CATALOG_ALIAS, storeContext); // does not have any products
    List<Product> products = testling.findProductsByCategory(rootCategory);

    assertThat(products).isEmpty();
  }

  protected void testFindProductsByCategoryIsRoot(String rootCategoryId) {
    CommerceId commerceId = getIdProvider().formatCategoryId(null, rootCategoryId);

    Category rootCategory = testling.findCategoryById(commerceId, storeContext);
    List<Product> products = testling.findProductsByCategory(rootCategory);

    assertThat(products).isEmpty();
  }

  protected void testFindTopCategories() throws Exception {
    List<Category> topCategories = testling.findTopCategories(DEFAULT_CATALOG_ALIAS, storeContext);
    String topCategoryDisplayNamesStr = topCategories.stream().map(Category::getDisplayName)
            .map(displayName -> '"' + displayName + '"')
            .collect(Collectors.joining(", ", "[", "]"));
    LOG.debug("Top categories: {}", topCategoryDisplayNamesStr);
    assertThat(topCategories).isNotEmpty();

    Category category = topCategories.get(0);
    assertCategory(category);

    Category categoryParent = category.getParent();
    assertThat(categoryParent).isNotNull();
    assertThat(categoryParent.isRoot()).isTrue();
  }

  protected void testFindRootCategory(String rootCategoryId) {
    CommerceId commerceId = getIdProvider().formatCategoryId(null, rootCategoryId);
    Category rootCategory = testling.findCategoryById(commerceId, storeContext);

    assertThat(rootCategory).isNotNull();
    assertThat(rootCategory.isRoot()).isTrue();
    assertThat(rootCategory.getChildren()).isNotEmpty();
    assertThat(rootCategory.getBreadcrumb()).isEmpty();
  }

  protected void testFindSubCategories() throws Exception {
    Category category = findAndAssertCategory(TOP_CATEGORY_NAME, null, storeContext);

    List<Category> subCategories = testling.findSubCategories(category);
    assertThat(subCategories).isNotEmpty();

    Category firstSubCategory = subCategories.get(0);
    assertCategory(firstSubCategory);
    assertThat(firstSubCategory.getBreadcrumb()).hasSize(2);
  }

  protected void testFindSubCategoriesIsEmpty() throws Exception {
    CommerceId categoryId = getIdProvider().formatCategoryId(null, LEAF_CATEGORY_CODE);

    Category leafCategory = testling.findCategoryById(categoryId, storeContext);
    List<Category> subCategories = testling.findSubCategories(leafCategory);

    assertThat(subCategories).isEmpty();
  }

  protected void testFindCategoryById() throws Exception {
    CommerceId commerceId = getIdProvider().formatCategoryId(null, CATEGORY_CODE);
    Category category = testling.findCategoryById(commerceId, storeContext);

    assertCategory(category);
  }

  protected void testFindB2BCategoryFromMultiCatalogs() throws Exception {
    CatalogAlias catalogAlias = CatalogAlias.of("b2b");

    CommerceId commerceId = getIdProvider().formatCategoryId(catalogAlias, CATEGORY_CODE_B2B);

    Category category = testling.findCategoryById(commerceId, storeContext);
    assertCategory(category);
    Category parentCategory = category.getParent();
    assertThat(parentCategory).isNotNull();
    assertThat(parentCategory.isRoot()).isTrue();

    catalogAlias = CatalogAlias.of("master");
    commerceId = getIdProvider().formatCategoryId(catalogAlias, CATEGORY_CODE_B2B);
    category = testling.findCategoryById(commerceId, storeContext);
    assertCategory(category);
    parentCategory = category.getParent();
    assertThat(parentCategory).isNotNull();
    assertThat(parentCategory.isRoot()).isFalse();

    commerceId = getIdProvider().formatCategoryId(null, CATEGORY_CODE_B2B);
    category = testling.findCategoryById(commerceId, storeContext);
    assertThat(category).isNull();
  }

  protected void testFindCategoryByIdWithSlash() {
    CommerceId commerceId = getIdProvider().formatCategoryId(null, CATEGORY_WITH_SLASH);
    Category category = testling.findCategoryById(commerceId, storeContext);

    assertThat(category).isNotNull();
  }

  protected void testFindCategoryByIdIsNull() {
    CommerceId commerceId = getIdProvider().formatCategoryId(null, "balablablub");
    Category category = testling.findCategoryById(commerceId, storeContext);

    assertThat(category).isNull();
  }

  protected void testFindCategoryBySeoSegment() throws Exception {
    Category category = testling.findCategoryBySeoSegment(CATEGORY_SEO_SEGMENT, storeContext);

    assertCategory(category);
  }

  protected void testFindGermanCategoryBySeoSegment() throws Exception {
    Locale germanLocale = Locale.GERMANY;
    StoreContext germanStoreContext = StoreContextBuilderImpl.from((StoreContextImpl) storeContext)
            .withLocale(germanLocale)
            .build();

    Category category = testling.findCategoryBySeoSegment("kleider", germanStoreContext);

    assertThat(category).isNotNull();
    assertThat(category.getLocale()).isEqualTo(germanLocale);
    assertThat(category.getParent().getLocale()).isEqualTo(germanLocale);
  }

  protected void testFindCategoryBySeoSegmentIsNull() throws Exception {
    Category category = testling.findCategoryBySeoSegment("blablablub", storeContext);

    assertThat(category).isNull();
  }

  protected void testSearchProducts() throws Exception {
    SearchResult<Product> searchResult = testling.searchProducts(SEARCH_TERM_1, emptyMap(), storeContext);
    assertThat(searchResult).isNotNull();
    assertThat(searchResult.getSearchResult()).isNotEmpty();

    // search product below category
    CommerceId commerceId = getIdProvider().formatCategoryId(null, LEAF_CATEGORY_CODE);
    Category category = testling.findCategoryById(commerceId, storeContext);
    Map<String, String> searchParams = new HashMap<>();
    assertThat(category).isNotNull();
    searchParams.put(CatalogService.SEARCH_PARAM_CATEGORYID, category.getExternalTechId());
    searchParams.put(CatalogService.SEARCH_PARAM_TOTAL, "7");
    SearchResult<Product> searchResultByCategory = testling.searchProducts(SEARCH_TERM_1, searchParams, storeContext);
    assertThat(searchResultByCategory).isNotNull();
    assertThat(searchResultByCategory.getSearchResult().size()).isGreaterThanOrEqualTo(3);
    assertThat(searchResultByCategory.getTotalCount()).isLessThan(searchResult.getTotalCount());

    // search product with paging
    Map<String, String> pagingParams = new HashMap<>();
    pagingParams.put(CatalogService.SEARCH_PARAM_PAGESIZE, "10");
    pagingParams.put(CatalogService.SEARCH_PARAM_PAGENUMBER, "1");
    SearchResult<Product> searchResultPaging = testling.searchProducts(SEARCH_TERM_1, pagingParams, storeContext);
    assertThat(searchResultPaging).isNotNull();
    Product product1 = searchResultPaging.getSearchResult().get(9);

    pagingParams.put(CatalogService.SEARCH_PARAM_PAGESIZE, "9");
    pagingParams.put(CatalogService.SEARCH_PARAM_PAGENUMBER, "2");
    SearchResult<Product> searchResultPaging2 = testling.searchProducts(SEARCH_TERM_1, pagingParams, storeContext);
    Product product2 = searchResultPaging2.getSearchResult().get(0);
    assertThat(searchResultPaging2).isNotNull();
    assertThat(product1.getId()).isEqualTo(product2.getId());

    // search product with invalid param
    Map<String, String> ignoredParam = new HashMap<>();
    ignoredParam.put("blub", "10");
    ignoredParam.put(CatalogService.SEARCH_PARAM_PAGESIZE, "10");
    ignoredParam.put(CatalogService.SEARCH_PARAM_PAGENUMBER, "1");
    SearchResult<Product> searchResultIgnoredParam = testling.searchProducts(SEARCH_TERM_1, ignoredParam, storeContext);
    assertThat(searchResultIgnoredParam).isNotNull();

    // search product no hits
    SearchResult<Product> searchResultEmpty = testling.searchProducts("schnasndasn", emptyMap(), storeContext);
    assertThat(searchResultEmpty).isNotNull();
    assertThat(searchResultEmpty.getSearchResult()).isEmpty();

    // search product multiple words
    SearchResult<Product> searchResultMultipleWords = testling.searchProducts(SEARCH_TERM_1 + " " + SEARCH_TERM_2, emptyMap(), storeContext);
    assertThat(searchResultMultipleWords).isNotNull();
    assertThat(searchResultMultipleWords.getSearchResult()).isNotEmpty();

    // search product multiple words no hit
    SearchResult<Product> searchResultMultipleWords2 = testling.searchProducts(SEARCH_TERM_2 + " schnasndasn", emptyMap(), storeContext);
    assertThat(searchResultMultipleWords2).isNotNull();
    assertThat(searchResultMultipleWords2.getSearchResult()).isEmpty();
  }

  protected void testSearchProductsWithOffset() throws Exception {
    int start = 3;
    int total = 5;
    Map<String, String> searchParams = new HashMap<>();
    SearchResult<Product> searchProducts = testling.searchProducts(SEARCH_TERM_3, searchParams, storeContext);
    assertThat(searchProducts).isNotNull();
    List<Product> originalProducts = searchProducts.getSearchResult();
    assertThat(originalProducts).isNotNull();
    Product firstProduct = originalProducts.get(start - 1);
    Product lastProduct = originalProducts.get(start + total - 2);

    searchParams.put(CatalogService.SEARCH_PARAM_OFFSET, Integer.toString(start));
    searchParams.put(CatalogService.SEARCH_PARAM_TOTAL, Integer.toString(total));
    SearchResult<Product> searchProductsWithOffset = testling.searchProducts(SEARCH_TERM_3, searchParams, storeContext);
    assertThat(searchProductsWithOffset).isNotNull();
    assertThat(searchProductsWithOffset.getTotalCount()).isEqualTo(originalProducts.size());
    List<Product> limitedProducts = searchProductsWithOffset.getSearchResult();
    assertThat(limitedProducts).hasSize(total);
    assertThat(firstProduct.getName()).isEqualTo(limitedProducts.get(0).getName());
    assertThat(lastProduct.getName()).isEqualTo(limitedProducts.get(total - 1).getName());

    start = 30;
    total = 3;
    firstProduct = originalProducts.get(start - 1);
    lastProduct = originalProducts.get(start + total - 2);
    searchParams.put(CatalogService.SEARCH_PARAM_OFFSET, Integer.toString(start));
    searchParams.put(CatalogService.SEARCH_PARAM_TOTAL, Integer.toString(total));
    searchProductsWithOffset = testling.searchProducts(SEARCH_TERM_3, searchParams, storeContext);
    assertThat(searchProductsWithOffset).isNotNull();
    assertThat(searchProductsWithOffset.getTotalCount()).isEqualTo(originalProducts.size());
    limitedProducts = searchProductsWithOffset.getSearchResult();
    assertThat(limitedProducts).hasSize(total);
    assertThat(firstProduct.getName()).isEqualTo(limitedProducts.get(0).getName());
    assertThat(lastProduct.getName()).isEqualTo(limitedProducts.get(total - 1).getName());

    start = 1000;
    total = 3;
    searchParams.put(CatalogService.SEARCH_PARAM_OFFSET, Integer.toString(start));
    searchParams.put(CatalogService.SEARCH_PARAM_TOTAL, Integer.toString(total));
    searchProductsWithOffset = testling.searchProducts(SEARCH_TERM_3, searchParams, storeContext);
    assertThat(searchProductsWithOffset).isNotNull();
    assertThat(searchProductsWithOffset.getTotalCount()).isEqualTo(originalProducts.size());
    limitedProducts = searchProductsWithOffset.getSearchResult();
    assertThat(limitedProducts).isEmpty();
  }

  protected void testGetFacetSearchProducts() throws Exception {
    CommerceId categoryId = getIdProvider().formatCategoryId(null, TOP_CATEGORY_CODE);
    Category category = testling.findCategoryById(categoryId, storeContext);
    Map<String, List<SearchFacet>> facetProducts = testling.getFacetsForProductSearch(category, storeContext);
    assertThat(facetProducts).isNotNull();
    int total = facetProducts.size();
    assertThat(total).isGreaterThan(0);

    categoryId = getIdProvider().formatCategoryId(null, LEAF_CATEGORY_CODE);
    category = testling.findCategoryById(categoryId, storeContext);
    facetProducts = testling.getFacetsForProductSearch(category, storeContext);
    assertThat(facetProducts).isNotNull();
    assertThat(facetProducts.size()).isGreaterThan(0);
  }

  @SuppressWarnings("ConstantConditions")
  @NonNull
  protected Category findAndAssertCategory(@NonNull String name, @Nullable Category parent,
                                           @NonNull StoreContext storeContext) {
    List<Category> topCategories = parent == null
            ? testling.findTopCategories(DEFAULT_CATALOG_ALIAS, storeContext)
            : testling.findSubCategories(parent);
    assertThat(topCategories).isNotEmpty();

    Optional<Category> anyTopCategory = topCategories.stream()
            .filter(category -> name.equals(category.getName()))
            .findAny();
    assertThat(anyTopCategory).as("Category '%s' not found", name).isPresent();

    return anyTopCategory.orElse(null);
  }

  protected void testSearchProductVariants() throws Exception {
    SearchResult<ProductVariant> searchResult1 = testling.searchProductVariants(SEARCH_TERM_1 + " " + SEARCH_TERM_2,
            emptyMap(), storeContext);
    assertThat(searchResult1).isNotNull();

    List<ProductVariant> searchResult1ProductVariants = searchResult1.getSearchResult();
    assertThat(searchResult1ProductVariants).isNotEmpty();
    assertThat(checkIfClassIsContained(searchResult1ProductVariants, "ProductVariantImpl")).isTrue();
    assertThat(!checkIfClassIsContained(searchResult1ProductVariants, "ProductImpl")).isTrue();

    // search product variants by parent part number

    SearchResult<ProductVariant> searchResult2 = testling.searchProductVariants(PRODUCT_CODE, emptyMap(), storeContext);
    assertThat(searchResult2).isNotNull();

    List<ProductVariant> searchResult2ProductVariants = searchResult2.getSearchResult();

    CommerceId productId = getIdProvider().formatProductId(null, PRODUCT_CODE);
    Product product = testling.findProductById(productId, storeContext);

    assertThat(product).isNotNull();

    List<ProductVariant> variants = product.getVariants();

    assertThat(variants)
            .as("Variants must not be empty (product variants of a product)")
            .isNotEmpty();

    assertThat(searchResult2ProductVariants)
            .as("Search result must have the same size as when asking the product directly (search product variants by parent part number)")
            .hasSize(variants.size());

    assertThat(checkIfClassIsContained(searchResult2ProductVariants, "ProductVariantImpl")).isTrue();
    assertThat(!checkIfClassIsContained(searchResult2ProductVariants, "ProductImpl")).isTrue();
  }

  protected void testGetDefaultCatalog() {
    Optional<Catalog> defaultCatalog = testling.getDefaultCatalog(storeContext);
    assertThat(defaultCatalog.isPresent()).isTrue();
    assertThat(defaultCatalog.get().getName().value()).containsIgnoringCase("Extended Sites Catalog Asset Store Consumer Direct");
    assertThat(defaultCatalog.get().isDefaultCatalog()).isTrue();
  }

  protected void testGetCatalogs() {
    List<Catalog> catalogs = testling.getCatalogs(storeContext);
    assertThat(catalogs).isNotEmpty();
    assertThat(catalogs.get(0).getName().value()).containsIgnoringCase("Extended Sites Catalog Asset Store");
  }

  protected void testSearchFacetsProducts(String query, Map<String, String> searchParams) {
    searchParams.put(CatalogService.SEARCH_PARAM_TOTAL, "1");
    searchParams.put(CatalogService.SEARCH_PARAM_PAGENUMBER, "1");
    searchParams.put(CatalogService.SEARCH_PARAM_PAGESIZE, "1");

    SearchResult<Product> searchResult = testling.searchProducts(query, searchParams, storeContext);
    assertThat(searchResult).isNotNull();

    List<SearchFacet> facets = searchResult.getFacets();
    assertThat(facets).isNotEmpty();

    SearchFacet aFacet = facets.get(0);
    aFacet = aFacet.getChildFacets().isEmpty() ? aFacet : aFacet.getChildFacets().get(0);
    assertSearchFacet(aFacet);
  }

  protected void assertSearchFacet(SearchFacet aFacet) {
    assertThat(aFacet.getChildFacets()).isNotNull();
    assertThat(aFacet.getLabel()).isNotNull();
    assertThat(aFacet.getQuery()).isNotNull();
    assertThat(aFacet.getExtendedData()).isNotNull();
  }

  protected void testWithStoreContext() {
    assertThat(storeContext.getLocale()).isNotEqualTo(Locale.GERMAN);

    StoreContext germanStoreContext = testConfig.getGermanStoreContext(connection);

    CommerceId productId = getIdProvider().formatProductId(storeContext.getCatalogAlias(), PRODUCT_CODE);

    Product product = testling.findProductById(productId, germanStoreContext);

    assertThat(product.getLocale()).isEqualTo(Locale.GERMAN);
  }

  protected void assertCategory(Category category) {
    assertThat(category).isNotNull();
    assertThat(category.getExternalId()).isNotEmpty();
//    assertThat(category.getExternalId()).isEqualTo(CATEGORY_CODE);
//    assertThat(category.getName()).isEqualTo(CATEGORY_NAME);
    assertThat(category.getName()).isNotEmpty();
    assertThat(category.getParent()).isNotNull();
//    assertThat(category.getSeoSegment()).isEqualTo(CATEGORY_SEO_SEGMENT);
    assertThat(category.getDefaultImageUrl()).isNotNull();
    assertThat(category.getThumbnailUrl()).isNotNull();
    assertThat(category.getShortDescription()).isNotNull(); // TODO: test more properties
    assertThat(category.getChildren()).isNotNull();
    assertThat(category.getProducts()).isNotNull();

    List<Category> categoryBreadcrumb = category.getBreadcrumb();
    assertThat(categoryBreadcrumb).isNotEmpty();
    assertThat(categoryBreadcrumb.get(categoryBreadcrumb.size() - 1)).isEqualTo(category);
    assertThat(category.getLocale()).isEqualTo(storeContext.getLocale());
  }

  protected void assertProductVariant(ProductVariant productVariant) {
    assertThat(productVariant).isNotNull();

    Product parentProduct = productVariant.getParent();
    assertThat(parentProduct).isNotNull();
    assertThat(productVariant.getName()).isNotNull();
//    assertThat(productVariant.getSeoSegment()).isEqualTo("travel-laptop-cla022-220301");
    assertThat(productVariant.getDefaultImageUrl()).endsWith(".jpg");
    assertThat(productVariant.getThumbnailUrl()).endsWith(".jpg");
    // TODO: add tests for describing and definig attributes
    assertThat(productVariant.getOfferPrice()).isNotNull();
  }

  protected CommerceIdProvider getIdProvider() {
    return connection.getIdProvider();
  }

  protected void assertProduct(Product product) {
    assertThat(product).isNotNull();
    assertThat(product.getName()).isNotEmpty();
    assertThat(product.getDefaultImageUrl()).endsWith(".jpg");
    assertThat(product.getThumbnailUrl()).endsWith(".jpg");
    assertThat(product.getCategory()).isNotNull();

    // test attributes
    assertThat(product.getDefiningAttributes()).isNotEmpty();

    // TODO: add test for describing attibutes

    // test variants
    List<ProductVariant> variants = product.getVariants();
    assertThat(variants).isNotEmpty();

    // test axis filter
    List<String> variantAxisNames = product.getVariantAxisNames();
    if (!variantAxisNames.isEmpty()) {
      List<ProductVariant> filteredVariants = product.getVariants(AxisFilter.onAnyValue(variantAxisNames.get(0)));
      assertThat(variants.size()).isGreaterThanOrEqualTo(filteredVariants.size());
    }
  }

  protected boolean checkIfClassIsContained(Collection<?> items, String containedClassType) {
    return items.stream().anyMatch(item -> item != null && item.getClass().getSimpleName().equals(containedClassType));
  }
}
