package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.CatalogName;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmService;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import com.coremedia.livecontext.ecommerce.search.SearchFacet;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.collections4.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.CATALOG;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.CATEGORY;
import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider.commerceId;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_8;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.map.LazyMap.lazyMap;

public class CatalogServiceImpl extends AbstractIbmService implements CatalogService {

  private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceImpl.class);

  private WcCatalogWrapperService catalogWrapperService;
  private StoreInfoService storeInfoService;
  private CommerceBeanFactory commerceBeanFactory;
  private CommerceCache commerceCache;
  private CatalogAliasTranslationService catalogAliasTranslationService;

  private String wcsAssetsUrl;

  private boolean useExternalIdForBeanCreation;

  public WcCatalogWrapperService getCatalogWrapperService() {
    return catalogWrapperService;
  }

  @Required
  public void setCatalogWrapperService(WcCatalogWrapperService catalogWrapperService) {
    this.catalogWrapperService = catalogWrapperService;
  }

  public StoreInfoService getStoreInfoService() {
    return storeInfoService;
  }

  public void setStoreInfoService(StoreInfoService storeInfoService) {
    this.storeInfoService = storeInfoService;
  }

  public CommerceBeanFactory getCommerceBeanFactory() {
    return commerceBeanFactory;
  }

  @Required
  public void setCommerceBeanFactory(CommerceBeanFactory commerceBeanFactory) {
    this.commerceBeanFactory = commerceBeanFactory;
  }

  @Required
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  public CommerceCache getCommerceCache() {
    return commerceCache;
  }

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setWcsAssetsUrl(String wcsAssetsUrl) {
    this.wcsAssetsUrl = wcsAssetsUrl;
  }

  public String getWcsAssetsUrl(@NonNull StoreContext storeContext) {
    return CommercePropertyHelper.replaceTokens(wcsAssetsUrl, storeContext);
  }

  public void setUseExternalIdForBeanCreation(boolean useExternalIdForBeanCreation) {
    this.useExternalIdForBeanCreation = useExternalIdForBeanCreation;
  }

  @PostConstruct
  void initialize() {
    initializeWcsAssetsUrl();
  }

  private void initializeWcsAssetsUrl() {
    if (wcsAssetsUrl == null) {
      return;
    }

    if (!wcsAssetsUrl.endsWith("/")) {
      this.wcsAssetsUrl = wcsAssetsUrl + "/";
    }

    validateUrlString(this.wcsAssetsUrl, "wcsAssetsUrl");
  }

  private static void validateUrlString(@NonNull String string, @NonNull String urlPropertyName) {
    // Validate format of URL, e.g. a path part starting with two slashes can
    // lead to broken images in the store front.
    try {
      URL url = new URL(string);
      String path = url.getPath();
      if (path.startsWith("//")) {
        LOG.warn("Invalid format of {}: URL's path part starts with '//'. URL is {}", urlPropertyName,
                url.toExternalForm());
      }
    } catch (MalformedURLException e) {
      throw new IllegalStateException(urlPropertyName + " is invalid.", e);
    }
  }

  @Override
  @Nullable
  public Product findProductById(@NonNull CommerceId id, @NonNull StoreContext storeContext) {
    UserContext userContext = CurrentUserContext.get();

    ProductCacheKey cacheKey = new ProductCacheKey(id, storeContext, userContext, catalogWrapperService, commerceCache);
    Map wcProductMap = commerceCache.get(cacheKey);

    CatalogAlias catalogAlias = id.getCatalogAlias();

    return createProductBeanFor(wcProductMap, catalogAlias, storeContext, false);
  }

  @Nullable
  @VisibleForTesting
  Product findProductByExternalTechId(@NonNull String externalTechId, @NonNull StoreContext storeContext) {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    CommerceId commerceId = commerceId(PRODUCT)
            .withCatalogAlias(catalogAlias)
            .withTechId(externalTechId)
            .build();

    return findProductById(commerceId, storeContext);
  }

  @Override
  @Nullable
  public Product findProductBySeoSegment(@NonNull String seoSegment, @NonNull StoreContext storeContext) {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    CommerceId commerceId = commerceId(PRODUCT)
            .withCatalogAlias(catalogAlias)
            .withSeo(seoSegment)
            .build();

    return findProductById(commerceId, storeContext);
  }

  @Override
  @Nullable
  public ProductVariant findProductVariantById(@NonNull CommerceId productId, @NonNull StoreContext storeContext) {
    return (ProductVariant) findProductById(productId, storeContext);
  }

  @Override
  @NonNull
  public List<Product> findProductsByCategory(@NonNull Category category) {
    if (category.isRoot()) {
      // The WCS has no root category, thus asking would lead to an error.
      return emptyList();
    }

    String id = category.getExternalTechId();
    CatalogAlias catalogAlias = category.getId().getCatalogAlias();
    StoreContext storeContext = category.getContext();
    UserContext userContext = CurrentUserContext.get();

    ProductsByCategoryCacheKey cacheKey = new ProductsByCategoryCacheKey(id, catalogAlias, storeContext, userContext,
            catalogWrapperService, commerceCache);
    List<Map<String, Object>> wcProductsMap = commerceCache.get(cacheKey);

    return createProductBeansFor(wcProductsMap, catalogAlias, storeContext);
  }

  @Override
  @Nullable
  public Category findCategoryById(@NonNull CommerceId commerceId, @NonNull StoreContext storeContext) {
    UserContext userContext = CurrentUserContext.get();

    if (CategoryImpl.isRootCategoryId(commerceId)) {
      return (Category) getCommerceBeanFactory().createBeanFor(commerceId, storeContext);
    }

    CategoryCacheKey cacheKey = new CategoryCacheKey(commerceId, storeContext, userContext, catalogWrapperService,
            commerceCache);
    Map<String, Object> wcCategory = commerceCache.get(cacheKey);

    CatalogAlias catalogAlias = commerceId.getCatalogAlias();

    return createCategoryBeanFor(wcCategory, catalogAlias, storeContext, false);
  }

  @Override
  @Nullable
  public Category findCategoryBySeoSegment(@NonNull String seoSegment, @NonNull StoreContext storeContext) {
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    CommerceId commerceId = commerceId(CATEGORY)
            .withCatalogAlias(catalogAlias)
            .withSeo(seoSegment)
            .build();

    return findCategoryById(commerceId, storeContext);
  }

  @Override
  @NonNull
  public Category findRootCategory(@NonNull CatalogAlias catalogAlias, @NonNull StoreContext storeContext) {
    CommerceId commerceId = commerceId(CATEGORY)
            .withCatalogAlias(catalogAlias)
            .withExternalId(CategoryImpl.ROOT_CATEGORY_ROLE_ID)
            .build();

    Category categoryById = findCategoryById(commerceId, storeContext);
    if (categoryById == null) {
      throw new NotFoundException("Cannot find root category for catalog " + catalogAlias + ".");
    }

    return categoryById;
  }

  @NonNull
  public List<Category> findTopCategories(@NonNull CatalogAlias catalogAlias, @NonNull StoreContext storeContext) {
    UserContext userContext = CurrentUserContext.get();

    TopCategoriesCacheKey cacheKey = new TopCategoriesCacheKey(catalogAlias, storeContext, userContext,
            catalogWrapperService, commerceCache);
    List<Map<String, Object>> wcCategories = commerceCache.get(cacheKey);

    return createCategoryBeansFor(wcCategories, catalogAlias, storeContext);
  }

  @Override
  @NonNull
  public List<Category> findSubCategories(@NonNull Category parentCategory) {
    StoreContext storeContext = parentCategory.getContext();
    CatalogAlias catalogAlias = parentCategory.getId().getCatalogAlias();

    if (parentCategory.isRoot()) {
      return findTopCategories(catalogAlias, storeContext);
    }

    String id = parentCategory.getExternalTechId();
    UserContext userContext = CurrentUserContext.get();

    SubCategoriesCacheKey cacheKey = new SubCategoriesCacheKey(id, catalogAlias, storeContext, userContext,
            catalogWrapperService, commerceCache);
    List<Map<String, Object>> wcCategories = commerceCache.get(cacheKey);

    return createCategoryBeansFor(wcCategories, catalogAlias, storeContext);
  }

  /**
   * Search for Products
   *
   * @param searchTerm   search keywords
   * @param searchParams map of search params:
   *                     <ul>
   *                     <li>pageNumber {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_PAGENUMBER}</li>
   *                     <li>pageSize {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_PAGESIZE}</li>
   *                     <li>categoryId {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_CATEGORYID}</li>
   *                     <li>orderBy {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_ORDERBY}</li>
   *                     <li>offset {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_OFFSET}</li>
   *                     <li>total {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_TOTAL}</li>
   *                     </ul>
   *                     In addition ibm rest specific parameters may be passed. See ibm rest api of the ProductViewHandler
   *                     for more details. See {@link WcCatalogWrapperService#validSearchParams} for the complete list of allowed parameters
   *                     to pass to the IBM ProductViewHandler.
   * @param storeContext
   * @return SearchResult containing the list of products
   * @throws CommerceException
   */
  @Override
  @NonNull
  public SearchResult<Product> searchProducts(@NonNull String searchTerm, @NonNull Map<String, String> searchParams,
                                              @NonNull StoreContext storeContext) {
    UserContext userContext = CurrentUserContext.get();

    SearchResult<Map<String, Object>> wcSearchResult = getCatalogWrapperService()
            .searchProducts(searchTerm, searchParams, storeContext, SearchType.SEARCH_TYPE_PRODUCTS, userContext);

    String catalogAliasStr = searchParams.get(CatalogService.SEARCH_PARAM_CATALOG_ALIAS);
    CatalogAlias catalogAlias = CatalogAlias.ofNullable(catalogAliasStr).orElseGet(storeContext::getCatalogAlias);

    List<Map<String, Object>> searchResult = wcSearchResult.getSearchResult();
    int startNumber = wcSearchResult.getPageNumber();

    searchResult = processOffset(searchParams, searchResult, startNumber);

    SearchResult<Product> result = new SearchResult<>();
    result.setSearchResult(createProductBeansFor(searchResult, catalogAlias, storeContext));
    result.setTotalCount(wcSearchResult.getTotalCount());
    result.setPageNumber(startNumber);
    result.setPageSize(wcSearchResult.getPageSize());
    result.setFacets(wcSearchResult.getFacets());
    return result;
  }

  @NonNull
  @Override
  public Map<String, List<SearchFacet>> getFacetsForProductSearch(@NonNull Category category,
                                                                  @NonNull StoreContext storeContext) {
    String categoryId = category.getExternalTechId();
    Map<String, String> searchParams = ImmutableMap.of(
            CatalogService.SEARCH_PARAM_CATEGORYID, categoryId,
            CatalogService.SEARCH_PARAM_PAGESIZE, "1",
            CatalogService.SEARCH_PARAM_PAGENUMBER, "1");

    SearchResult<Product> searchResult = searchProducts("*", searchParams, storeContext);

    return searchResult.getFacets().stream()
            .collect(toMap(
                    SearchFacet::getLabel,
                    searchFacet -> OfferPriceFormattingHelper.tryFormatOfferPrice(searchFacet).getChildFacets()
            ));
  }

  @Nullable
  private static List<Map<String, Object>> processOffset(@NonNull Map<String, String> searchParams,
                                                         @Nullable List<Map<String, Object>> searchResult,
                                                         int startNumber) {
    if (searchResult == null) {
      return null;
    }

    if (!searchParams.containsKey(CatalogService.SEARCH_PARAM_OFFSET)) {
      return searchResult;
    }

    String offsetSearchParam = searchParams.get(CatalogService.SEARCH_PARAM_OFFSET);
    int offset = offsetSearchParam != null ? Integer.parseInt(offsetSearchParam) - 1 : 0;

    String totalSearchParam = searchParams.get(CatalogService.SEARCH_PARAM_TOTAL);
    int total = totalSearchParam != null ? Integer.parseInt(totalSearchParam) : searchResult.size();

    if (startNumber != offset && offset < searchResult.size()) {
      return searchResult.subList(offset, searchResult.size());
    }

    if (total >= searchResult.size()) {
      return searchResult;
    }

    return searchResult.subList(0, total);
  }

  /**
   * Search for ProductVariants
   *
   * @param searchTerm   search keywords
   * @param searchParams map of search params:
   *                     <ul>
   *                     <li>pageNumber {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_PAGENUMBER}</li>
   *                     <li>pageSize {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_PAGESIZE}</li>
   *                     <li>categoryId {@link com.coremedia.livecontext.ecommerce.catalog.CatalogService#SEARCH_PARAM_CATEGORYID}</li>
   *                     </ul>
   *                     In addition ibm rest specific parameters may be passed. See ibm rest api of the ProductViewHandler
   *                     for more details. See {@link WcCatalogWrapperService#validSearchParams} for the complete list of allowed parameters
   *                     to pass to the IBM ProductViewHandler.
   * @param storeContext
   * @return SearchResult containing the list of product variants
   * @throws CommerceException
   */
  @Override
  @NonNull
  public SearchResult<ProductVariant> searchProductVariants(@NonNull String searchTerm,
                                                            @NonNull Map<String, String> searchParams,
                                                            @NonNull StoreContext storeContext) {
    UserContext userContext = CurrentUserContext.get();

    String catalogAliasStr = searchParams.get(CatalogService.SEARCH_PARAM_CATALOG_ALIAS);
    CatalogAlias catalogAlias = CatalogAlias.ofNullable(catalogAliasStr).orElseGet(storeContext::getCatalogAlias);

    List<ProductVariant> searchResultList = Collections.emptyList();

    Map<String, Object> wcProductMap = getCatalogWrapperService()
            .findProductByExternalId(searchTerm, catalogAlias, storeContext, userContext);

    if (wcProductMap != null) {
      Product product = createProductBeanFor(wcProductMap, catalogAlias, storeContext, false);
      if (product != null) {
        if (product.isVariant()) {
          searchResultList = Collections.singletonList((ProductVariant) product);
        } else {
          searchResultList = product.getVariants();
        }
      }
    }

    if (!searchResultList.isEmpty()) {
      SearchResult<ProductVariant> result = new SearchResult<>();
      result.setSearchResult(searchResultList);
      result.setTotalCount(searchResultList.size());
      result.setPageNumber(1);
      result.setPageSize(searchResultList.size());
      return result;
    }

    SearchResult<Map<String, Object>> wcSearchResult = getCatalogWrapperService()
            .searchProducts(searchTerm, searchParams, storeContext, SearchType.SEARCH_TYPE_PRODUCT_VARIANTS,
                    userContext);

    searchResultList = createProductBeansFor(wcSearchResult.getSearchResult(), catalogAlias, storeContext);

    SearchResult<ProductVariant> result = new SearchResult<>();
    result.setSearchResult(searchResultList);
    result.setTotalCount(wcSearchResult.getTotalCount());
    result.setPageNumber(wcSearchResult.getPageNumber());
    result.setPageSize(wcSearchResult.getPageSize());
    result.setFacets(wcSearchResult.getFacets());
    return result;
  }

  @NonNull
  public String getLanguageId(@Nullable Locale locale) {
    return CurrentStoreContext.find()
            .map(context -> context.getReplacements().get("lang.id"))
            .orElseGet(() -> getCatalogWrapperService().getLanguageId(locale));
  }

  @NonNull
  @Override
  public List<Catalog> getCatalogs(@NonNull StoreContext storeContext) {
    WcsVersion wcsVersion = StoreContextHelper.getWcsVersion(storeContext);

    Map<String, Object> responseMap;
    if (WCS_VERSION_7_8.lessThan(wcsVersion)) {
      CatalogsForStoreCacheKey cacheKey = new CatalogsForStoreCacheKey(storeContext, catalogWrapperService,
              commerceCache);
      responseMap = commerceCache.get(cacheKey);
    } else {
      responseMap = storeInfoService.getStoreInfos();
    }

    return createCatalogBeansFor(responseMap, storeContext);
  }

  @NonNull
  @Override
  public Optional<Catalog> getDefaultCatalog(@NonNull StoreContext storeContext) {
    List<Catalog> catalogs = getCatalogs(storeContext);
    return catalogs.stream()
            .filter(Catalog::isDefaultCatalog)
            .findFirst();
  }

  @Override
  @NonNull
  public Optional<Catalog> getCatalog(@NonNull CatalogId catalogId, @NonNull StoreContext storeContext) {
    List<Catalog> catalogs = getCatalogs(storeContext);
    return catalogs.stream()
            .filter(catalog -> catalog.getId().getExternalId().map(catalogId.value()::equals).orElse(false))
            .findFirst();
  }

  @NonNull
  @Override
  public Optional<Catalog> getCatalog(@NonNull CatalogAlias alias, @NonNull StoreContext storeContext) {
    CatalogName catalogName = catalogAliasTranslationService
            .getCatalogNameForAlias(alias, storeContext)
            .orElse(null);

    if (catalogName == null) {
      return Optional.empty();
    }

    List<Catalog> catalogs = getCatalogs(storeContext);
    Optional<Catalog> catalogOpt = catalogs.stream()
            .filter(catalog -> catalog.getName().equals(catalogName))
            .findFirst();

    if (!catalogOpt.isPresent()) {
      LOG.warn("Could not load Catalog for Alias \'{}\'", alias.value());
    }

    return catalogOpt;
  }

  @Nullable
  protected <T extends Product> T createProductBeanFor(@Nullable Map<String, Object> productWrapper,
                                                       @Nullable CatalogAlias catalogAlias,
                                                       @NonNull StoreContext context, boolean reloadById) {
    if (productWrapper == null) {
      return null;
    }

    CommerceIdProvider idProvider = getCommerceIdProvider();

    CommerceId commerceId;
    if ("ItemBean".equals(getStringValueForKey(productWrapper, "catalogEntryTypeCode"))) {
      commerceId = useExternalIdForBeanCreation
              ? idProvider.formatProductVariantId(catalogAlias, getStringValueForKey(productWrapper, "partNumber"))
              : idProvider.formatProductVariantTechId(catalogAlias, getStringValueForKey(productWrapper, "uniqueID"));
    } else {
      commerceId = useExternalIdForBeanCreation
              ? idProvider.formatProductId(catalogAlias, getStringValueForKey(productWrapper, "partNumber"))
              : idProvider.formatProductTechId(catalogAlias, getStringValueForKey(productWrapper, "uniqueID"));
    }

    ProductBase product = (ProductBase) commerceBeanFactory.createBeanFor(commerceId, context);

    // Register the product wrapper with the cache, it will optimize later
    // accesses (there is a good chance that the beans will be called
    // immediately after this call).
    // TODO: Currently we use it only for Studio calls, but check if we can do
    //       it for a CAE webapp as well.
    // (It probably requires that we are able to reload beans dynamically if
    // someone tries to read a property that is not available.)
    Transformer transformer = null;
    if (reloadById) {
      transformer = new ProductDelegateLoader(product);
    }
    product.setDelegate(asLazyMap(productWrapper, transformer));

    return (T) product;
  }

  @NonNull
  protected <T extends Product> List<T> createProductBeansFor(@Nullable List<Map<String, Object>> productWrappers,
                                                              @Nullable CatalogAlias catalogAlias,
                                                              @NonNull StoreContext context) {
    if (productWrappers == null || productWrappers.isEmpty()) {
      return emptyList();
    }

    List<T> result = productWrappers.stream()
            .map(productWrapper -> this.<T>createProductBeanFor(productWrapper, catalogAlias, context, true))
            .collect(toList());

    return unmodifiableList(result);
  }

  @Nullable
  protected Category createCategoryBeanFor(@Nullable Map<String, Object> categoryWrapper,
                                           @Nullable CatalogAlias catalogAlias, @NonNull StoreContext context,
                                           boolean reloadById) {
    if (categoryWrapper == null) {
      return null;
    }

    IbmCommerceIdProvider commerceIdProvider = getCommerceIdProvider();
    CommerceId commerceId = useExternalIdForBeanCreation
            ? commerceIdProvider.formatCategoryId(catalogAlias, getStringValueForKey(categoryWrapper, "identifier"))
            : commerceIdProvider.formatCategoryTechId(catalogAlias, getStringValueForKey(categoryWrapper, "uniqueID"));

    CategoryImpl category = (CategoryImpl) commerceBeanFactory.createBeanFor(commerceId, context);

    // Register the product wrapper with the cache, it will optimize later
    // accesses (there is a good chance that the beans will be called
    // immediately after this call).
    // TODO: currently we use it only for Studio calls, but check if we can do
    //       it for a CAE webapp as well.
    // (It probably requires that we are able to reload beans dynamically if
    // someone tries to read a property that is not available.)
    Transformer transformer = null;
    if (reloadById) {
      transformer = new CategoryDelegateLoader(category);
    }
    category.setDelegate(asLazyMap(categoryWrapper, transformer));

    return category;
  }

  @Nullable
  protected Catalog createCatalogBeanFor(@Nullable Map<String, Object> catalogWrapper, @NonNull StoreContext context) {
    if (catalogWrapper == null) {
      return null;
    }

    String catalogId = getStringValueForKey(catalogWrapper, "catalogId");

    // Use `catalogIdentifier` to resolve matching catalog alias, in order to
    // avoid recursion while creating catalog beans.
    String catalogIdentifier = getStringValueForKey(catalogWrapper, "catalogIdentifier");
    CatalogName catalogName = CatalogName.of(catalogIdentifier);
    CatalogAlias catalogAliasForName = catalogAliasTranslationService
            .getCatalogAliasForName(catalogName, context.getSiteId());

    CommerceId commerceId = commerceId(CATALOG)
            .withExternalId(catalogId)
            .withCatalogAlias(catalogAliasForName)
            .build();

    CatalogImpl catalog = (CatalogImpl) commerceBeanFactory.createBeanFor(commerceId, context);
    catalog.setDelegate(catalogWrapper);
    return catalog;
  }

  @Nullable
  public static Map<String, Object> asLazyMap(@Nullable Map<String, Object> map, @Nullable Transformer transformer) {
    if (map == null || transformer == null) {
      return map;
    }

    //noinspection unchecked
    return lazyMap(newHashMap(map), transformer);
  }

  @NonNull
  protected List<Category> createCategoryBeansFor(@Nullable List<Map<String, Object>> categoryWrappers,
                                                  @Nullable CatalogAlias catalogAlias, @NonNull StoreContext context) {
    if (categoryWrappers == null || categoryWrappers.isEmpty()) {
      return emptyList();
    }

    List<Category> result = categoryWrappers.stream()
            .map(categoryWrapper -> createCategoryBeanFor(categoryWrapper, catalogAlias, context, true))
            .collect(toList());

    return unmodifiableList(result);
  }

  @NonNull
  protected List<Catalog> createCatalogBeansFor(Map<String, Object> jsonResult, StoreContext context) {
    if (jsonResult == null || jsonResult.isEmpty()) {
      return emptyList();
    }

    List<Map<String, Object>> catalogs;

    WcsVersion wcsVersion = StoreContextHelper.getWcsVersion(context);
    if (WCS_VERSION_7_8.lessThan(wcsVersion)) {
      catalogs = (List<Map<String, Object>>) DataMapHelper.getList(jsonResult, "resultList");
    } else {
      String storeName = context.getStoreName();

      Map<String, Object> catalogsMap = (Map<String, Object>) DataMapHelper
              .getMap(jsonResult, "stores." + storeName + ".catalogs");

      String defaultCatalog = getStringValueForKey(jsonResult, "stores." + storeName + ".defaultCatalog");

      catalogs = new ArrayList<>();

      for (Map.Entry<String, Object> entry : catalogsMap.entrySet()) {
        Map<String, Object> catalogEntry = new HashMap<>();

        String catalogName = entry.getKey();
        catalogEntry.put("catalogIdentifier", catalogName);

        String catalogId = (String) entry.getValue();
        catalogEntry.put("catalogId", catalogId);

        if (catalogName.equals(defaultCatalog)) {
          catalogEntry.put("default", true);
        }

        catalogs.add(catalogEntry);
      }
    }

    List<Catalog> result = catalogs.stream()
            .map(catalogWrapper -> createCatalogBeanFor(catalogWrapper, context))
            .collect(toList());

    return unmodifiableList(result);
  }

  @Nullable
  private static String getStringValueForKey(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
