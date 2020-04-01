package com.coremedia.caas.commerce.model;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.caas.commerce.model.CommerceFacade;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchFacet;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommerceFacadeTest {

  private static final CatalogAlias CATALOG_ALIAS = CatalogAlias.of("catalog");
  private static final String CATEGORY_ID = "ibm:///catalog/category/Women Handbags";
  private static final String PRODUCT_ID = "ibm:///catalog/product/PC_TEA_POT";
  private static final String SITE_ID = "5678";

  private CommerceFacade commerceFacade;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private CommerceIdProvider commerceIdProvider;

  @Mock
  private CommerceBeanFactory commerceBeanFactory;

  @Mock
  private StoreContext storeContext;

  @Mock
  private CommerceId commerceId;

  @Mock
  private CatalogService catalogService;

  @Mock
  private Product product;

  @Mock
  private Category category;

  @Mock
  private Catalog catalog;

  @Mock
  private SearchResult<ProductVariant> productVariantSearchResult;

  @Mock
  private SearchResult<Product> productSearchResult;

  @Mock
  private ProductVariant productVariant;


  @Before
  public void init() {
    commerceFacade = new CommerceFacade(commerceConnectionInitializer, sitesService);

    String testSiteId = "5678";
    when(sitesService.getSite(testSiteId)).thenReturn(site);
    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(commerceConnection));
    when(commerceConnection.getIdProvider()).thenReturn(commerceIdProvider);
    when(commerceConnection.getCommerceBeanFactory()).thenReturn(commerceBeanFactory);
    when(commerceConnection.getStoreContext()).thenReturn(storeContext);
    when(commerceConnection.getCatalogService()).thenReturn(catalogService);
    when(storeContext.getCatalogAlias()).thenReturn(CATALOG_ALIAS);
  }

  @Test
  public void getProduct() {
    CommerceId commerceId = CommerceIdParserHelper.parseCommerceId(PRODUCT_ID).get();
    when(commerceBeanFactory.createBeanFor(commerceId, storeContext)).thenReturn(product);
    Product gotProduct = commerceFacade.getProduct(PRODUCT_ID, SITE_ID);
    assertSame(product, gotProduct);
  }

  @Test
  public void getProductByTechId() {
    when(commerceIdProvider.formatProductTechId(CATALOG_ALIAS, PRODUCT_ID)).thenReturn(commerceId);
    when(catalogService.findProductById(commerceId, storeContext)).thenReturn(product);

    Product product = commerceFacade.getProductByTechId(PRODUCT_ID, SITE_ID);

    assertNotNull(product);
    verify(catalogService).findProductById(commerceId, storeContext);
  }

  @Test
  public void getCatalog() {
    String catalogId = "ibm:///catalog/catalog/3074457345616676668";
    CommerceId commerceId = CommerceIdParserHelper.parseCommerceId(catalogId).get();
    when(commerceBeanFactory.createBeanFor(commerceId, storeContext)).thenReturn(catalog);
    Catalog gotCatalog = commerceFacade.getCatalog(catalogId, SITE_ID);
    assertSame(catalog, gotCatalog);
  }

  @Test
  public void getCatalogByAlias() {
    String catalogAlias = "testAlias";
    when(catalogService.getCatalog(CatalogAlias.of(catalogAlias), storeContext)).thenReturn(Optional.of(catalog));
    Catalog catalog = commerceFacade.getCatalogByAlias(catalogAlias, SITE_ID);

    assertNotNull(catalog);
    verify(catalogService).getCatalog(CatalogAlias.of(catalogAlias), storeContext);
  }

  @Test
  public void getDefaultCatalog() {
    when(catalogService.getDefaultCatalog(storeContext)).thenReturn(Optional.of(catalog));
    Catalog catalog = commerceFacade.getDefaultCatalog(SITE_ID);

    assertNotNull(catalog);
    verify(catalogService).getDefaultCatalog(storeContext);
  }

  @Test
  public void getCatalogs() {
    when(catalogService.getCatalogs(storeContext)).thenReturn(Collections.singletonList(catalog));
    List<Catalog> catalogs = commerceFacade.getCatalogs(SITE_ID);

    assertNotNull(catalogs);
    assertTrue(catalogs.size() > 0);
    verify(catalogService).getCatalogs(storeContext);
  }

  @Test
  public void getCategory() {
    CommerceId commerceId = CommerceIdParserHelper.parseCommerceId(CATEGORY_ID).get();
    when(commerceBeanFactory.createBeanFor(commerceId, storeContext)).thenReturn(category);

    Category gotCategory = commerceFacade.getCategory(CATEGORY_ID, SITE_ID);
    assertSame(category, gotCategory);
  }

  @Test
  public void findProductBySeoSegment() {
    String seoSegment = "testSegment";
    when(catalogService.findProductBySeoSegment(seoSegment, storeContext)).thenReturn(product);
    Product product = commerceFacade.findProductBySeoSegment(seoSegment, SITE_ID);

    assertNotNull(product);
    verify(catalogService).findProductBySeoSegment(seoSegment, storeContext);
  }

  @Test
  public void findProductVariantById() {
    String productVariantId = "ibm:///catalog/sku/catalog:master;techId:3074457345616680732";
    CommerceId commerceId = CommerceIdParserHelper.parseCommerceId(productVariantId).get();
    when(commerceBeanFactory.createBeanFor(commerceId, storeContext)).thenReturn(productVariant);
    Product gotProductVariant = commerceFacade.getProductVariant(productVariantId, SITE_ID);
    assertSame(productVariant, gotProductVariant);
  }

 @Test
  public void findCategoryBySeoSegment() {
    String seoSegment = "testSegment";
    when(catalogService.findCategoryBySeoSegment(seoSegment, storeContext)).thenReturn(category);
    Category category = commerceFacade.findCategoryBySeoSegment(seoSegment, SITE_ID);

    assertNotNull(category);
    verify(catalogService).findCategoryBySeoSegment(seoSegment, storeContext);
  }

  @Test
  public void searchProducts() {
    String searchTerm = "test";
    when(catalogService.searchProducts(searchTerm, Collections.emptyMap(), storeContext)).thenReturn(productSearchResult);
    SearchResult searchResult = commerceFacade.searchProducts(searchTerm, Collections.emptyMap(), SITE_ID);

    assertNotNull(searchResult);
    verify(catalogService).searchProducts(searchTerm, Collections.emptyMap(), storeContext);
  }

  @Test
  public void getFacetsForProductSearch() {
    String categoryId = "12321321";
    when(commerceIdProvider.formatCategoryId(storeContext.getCatalogAlias(), categoryId)).thenReturn(commerceId);
    when(catalogService.findCategoryById(commerceId, storeContext)).thenReturn(category);
    when(catalogService.getFacetsForProductSearch(category, storeContext)).thenReturn(Collections.singletonMap("test", Collections.emptyList()));
    Map<String, List<SearchFacet>> facetsForProductSearch = commerceFacade.getFacetsForProductSearch(categoryId, SITE_ID);

    assertNotNull(facetsForProductSearch);
    assertTrue(facetsForProductSearch.size() > 0);
    verify(catalogService).getFacetsForProductSearch(category, storeContext);
  }

  @Test
  public void searchProductVariants() {
    String searchTerm = "test";
    Map<String, String> searchParams = Collections.emptyMap();
    when(catalogService.searchProductVariants(searchTerm, searchParams, storeContext)).thenReturn(productVariantSearchResult);
    SearchResult searchResult = commerceFacade.searchProductVariants(searchTerm, searchParams, SITE_ID);

    assertNotNull(searchResult);
    verify(catalogService).searchProductVariants(searchTerm, searchParams, storeContext);
  }
}
