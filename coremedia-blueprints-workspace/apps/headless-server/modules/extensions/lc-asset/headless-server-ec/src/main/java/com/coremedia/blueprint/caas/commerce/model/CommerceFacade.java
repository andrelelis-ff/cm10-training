package com.coremedia.blueprint.caas.commerce.model;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchFacet;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A simple facade for commerce queries by site id.
 */
@DefaultAnnotation(NonNull.class)
public class CommerceFacade {

  private static final Logger LOG = LoggerFactory.getLogger(CommerceFacade.class);

  private CommerceConnectionInitializer commerceConnectionInitializer;
  private final SitesService sitesService;

  public CommerceFacade(CommerceConnectionInitializer commerceConnectionInitializer, SitesService sitesService) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
    this.sitesService = sitesService;
  }

  @Nullable
  public Product getProduct(String externalId, String siteId) {
    return parseId(externalId, siteId, Product.class);
  }

  @Nullable
  public Product getProductByTechId(String techId, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();
    CommerceId productCommerceId = connection.getIdProvider().formatProductTechId(storeContext.getCatalogAlias(), techId);
    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService.findProductById(productCommerceId, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve product for techId " + techId, e);
      return null;
    }

  }

  @Nullable
  public Catalog getCatalog(String catalogId, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    if (catalogId == null) {
      return getDefaultCatalog(siteId);
    }
    return parseId(catalogId, siteId, Catalog.class);
  }

  @Nullable
  public List<Catalog> getCatalogs(String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }

    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService.getCatalogs(connection.getStoreContext());
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve catalogs for siteId " + siteId, e);
      return null;
    }
  }

  @Nullable
  public Category getCategory(String categoryId, String siteId) {
    return parseId(categoryId, siteId, Category.class);
  }

  @Nullable
  public Product findProductBySeoSegment(String seoSegment, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();
    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService.findProductBySeoSegment(seoSegment, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve product for seoSegment " + seoSegment, e);
      return null;
    }
  }

  @SuppressWarnings("unused")
  @Nullable
  public ProductVariant getProductVariant(String productVariantId, String siteId) {
    return parseId(productVariantId, siteId, ProductVariant.class);
  }

  @Nullable
  public Category findCategoryBySeoSegment(String seoSegment, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();

    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService.findCategoryBySeoSegment(seoSegment, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve category by seoSegment " + seoSegment, e);
      return null;
    }
  }

  @Nullable
  public SearchResult<Product> searchProducts(String searchTerm, Map<String, String> searchParams, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();

    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService.searchProducts(searchTerm, searchParams, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not search products with searchTerm " + searchTerm, e);
      return null;
    }
  }

  @Nullable
  public Map<String, List<SearchFacet>> getFacetsForProductSearch(String categoryId, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();
    CommerceIdProvider idProvider = connection.getIdProvider();
    CommerceId categoryCommerceId = idProvider.formatCategoryId(storeContext.getCatalogAlias(), categoryId);

    try {
      CatalogService catalogService = connection.getCatalogService();
      Category category = catalogService.findCategoryById(categoryCommerceId, storeContext);
      if (category == null) {
        return null;
      }
      return catalogService.getFacetsForProductSearch(category, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve facets for categoryId " + categoryId, e);
      return null;
    }
  }

  @Nullable
  public SearchResult<ProductVariant> searchProductVariants(String searchTerm, Map<String, String> searchParams, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();

    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService.searchProductVariants(searchTerm, searchParams, storeContext);
    } catch (CommerceException e) {
      LOG.warn("Could not search product variants with searchTerm " + searchTerm, e);
      return null;
    }
  }

  @Nullable
  public Catalog getCatalogByAlias(String catalogAlias, String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();

    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService
              .getCatalog(CatalogAlias.of(catalogAlias), storeContext)
              .orElse(null);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve catalog for catalogAlias " + catalogAlias, e);
      return null;
    }
  }

  @Nullable
  public Catalog getDefaultCatalog(String siteId) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    StoreContext storeContext = connection.getStoreContext();

    try {
      CatalogService catalogService = connection.getCatalogService();
      return catalogService
              .getDefaultCatalog(storeContext)
              .orElse(null);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve default catalog", e);
      return null;
    }
  }

  @Nullable
  private CommerceConnection getCommerceConnection(String siteId) {
    try {
      Site site = sitesService.getSite(siteId);
      CommerceConnection connection = commerceConnectionInitializer.findConnectionForSite(site).orElse(null);

      if (connection == null) {
        LOG.warn("Cannot find commerce connection for siteId {}", siteId);
        return null;
      }
      return connection;
    } catch (CommerceException e) {
      LOG.warn("Cannot find commerce connection for siteId " + siteId, e);
      return null;
    }
  }

  @Nullable
  public String getExternalId(CommerceBean commerceBean) {
    return CommerceIdFormatterHelper.format(commerceBean.getId());
  }

  @Nullable
  private CommerceBean parseId(String id, CommerceConnection commerceConnection) {
    Optional<CommerceId> commerceIdOptional = CommerceIdParserHelper.parseCommerceId(id);
    if (commerceIdOptional.isEmpty()) {
      LOG.debug("unknown id: '{}'", id);
      return null;
    }

    CommerceId commerceId = commerceIdOptional.get();
    StoreContext storeContext = commerceConnection.getStoreContext();

    return commerceConnection.getCommerceBeanFactory().createBeanFor(commerceId, storeContext);
  }

  @Nullable
  private <T extends CommerceBean> T parseId(String id, String siteId, Class<T> expectedType) {
    CommerceConnection connection = getCommerceConnection(siteId);
    if (connection == null) {
      return null;
    }
    CommerceBean bean = parseId(id, connection);
    if (bean == null || !expectedType.isAssignableFrom(bean.getClass())) {
      return null;
    }
    try {
      bean.load();
      //noinspection unchecked
      return (T) bean;
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve product for id " + id, e);
      return null;
    }
  }
}
