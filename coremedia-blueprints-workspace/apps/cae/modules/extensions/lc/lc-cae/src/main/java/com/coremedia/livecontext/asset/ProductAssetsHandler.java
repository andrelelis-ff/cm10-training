package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cap.common.CapObjectDestroyedException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteDestroyedException;
import com.coremedia.livecontext.ecommerce.catalog.AxisFilter;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.catalog.VariantFilter;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.coremedia.blueprint.base.links.UriConstants.ContentTypes.CONTENT_TYPE_HTML;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static java.lang.String.format;

/**
 * Handle dynamic product asset requests.
 */
@Link
@RequestMapping
public class ProductAssetsHandler extends PageHandlerBase {

  private static final Logger LOG = LoggerFactory.getLogger(ProductAssetsHandler.class);

  public static final String URI_PREFIX = "productassets";

  private static final String SEGMENT_SITE = "site";
  private static final String SEGMENT_VIEW = "view";
  private static final String SEGMENT_CATALOG_ALIAS = "catalogAlias";
  private static final String SEGMENT_CATEGORY_ID = "categoryId";
  private static final String SEGMENT_PRODUCT_ID = "productId";
  private static final String SEGMENT_ORIENTATION = "orientation";
  private static final String SEGMENT_TYPES = "types";
  private static final String REQUEST_PARAM_SKU_ID = "catEntryId";
  private static final String REQUEST_PARAM_ATTRIBUTES = "attributes";
  private static final String VIEW_NAME = "asDynaAssets";

  private boolean useStableIds = false;

  /**
   * URI pattern, for URIs like "/dynamic/fragment/productassets/apparelhomepage/asDynaAssets/catalog/221600/111159_black/portrait/all"
   */
  public static final String DYNAMIC_URI_PATTERN = '/' + PREFIX_DYNAMIC +
          '/' + SEGMENTS_FRAGMENT +
          '/' + URI_PREFIX +
          "/{" + SEGMENT_SITE + '}' +
          "/{" + SEGMENT_VIEW + '}' +
          "/{" + SEGMENT_CATALOG_ALIAS + '}' +
          "/{" + SEGMENT_CATEGORY_ID + '}' +
          "/{" + SEGMENT_PRODUCT_ID + '}' +
          "/{" + SEGMENT_ORIENTATION + '}' +
          "/{" + SEGMENT_TYPES + '}';
  private static final String DEFAULT_ORIENTATION = "portrait";
  private static final String DEFAULT_SEGMENT_TYPES = "all";

  @SuppressWarnings("squid:S3752") // multiple request methods allowed by intention, commit states "otherwise the CrossDomainEnabler will not work"
  @RequestMapping(value = DYNAMIC_URI_PATTERN, produces = CONTENT_TYPE_HTML, method = {RequestMethod.GET, RequestMethod.POST})
  public ModelAndView handleFragment(
          @PathVariable(SEGMENT_SITE) String siteName,
          @PathVariable(SEGMENT_VIEW) String view,
          @PathVariable(SEGMENT_CATALOG_ALIAS) String catalogAlias,
          @PathVariable(SEGMENT_CATEGORY_ID) String categoryId,
          @PathVariable(SEGMENT_PRODUCT_ID) String productId,
          @PathVariable(SEGMENT_ORIENTATION) String orientation,
          @PathVariable(SEGMENT_TYPES) String types,
          @RequestParam(value = REQUEST_PARAM_SKU_ID, required = false) String skuId,
          @RequestParam(value = REQUEST_PARAM_ATTRIBUTES, required = false) String attributes,
          HttpServletRequest request,
          HttpServletResponse response) {
    StoreContext storeContext = CurrentStoreContext.find().orElse(null);

    if (storeContext == null) {
      LOG.warn("Commerce connection not properly initialized.");
      return HandlerHelper.notFound();
    }

    if (StringUtils.isBlank(productId)) {
      LOG.warn("Cannot handle request because productId is null.");
      return HandlerHelper.notFound();
    }

    Site site = getSiteByName(siteName);
    if (site == null) {
      return HandlerHelper.notFound();
    }

    Product self = findProduct(productId, skuId, CatalogAlias.of(catalogAlias), attributes, storeContext);
    if (self == null) {
      return HandlerHelper.notFound();
    }

    //strange, the "produces" annotation value does not work, so we set the response mime type manually
    response.setContentType(CONTENT_TYPE_HTML);

    ModelAndView modelWithView = HandlerHelper.createModelWithView(self, view);
    if (StringUtils.isNotBlank(orientation)) {
      modelWithView.addObject(SEGMENT_ORIENTATION, orientation);
    }
    if (StringUtils.isNotBlank(types)) {
      modelWithView.addObject(SEGMENT_TYPES, types);
    }

    Content rootChannel = site.getSiteRootDocument();
    CMNavigation navigation = getContentBeanFactory().createBeanFor(rootChannel, CMChannel.class);
    Page page = asPage(navigation, navigation, UserVariantHelper.getUser(request));
    addPageModel(modelWithView, page);

    return modelWithView;
  }

  // called (1) to create fragment link and (2) to create link as needed by Product.asDynaAssets.ftl
  @Link(type = Product.class, uri = DYNAMIC_URI_PATTERN, view = {"fragment", "asAssets"})
  public UriComponents buildFragmentLink(Product product, UriTemplate uriPattern, Map<String, Object> linkParameters,
                                         HttpServletRequest request) {
    if (product == null) {
      return null;
    }

    StoreContext storeContext = CurrentStoreContext.find().orElse(null);

    if (storeContext == null) {
      LOG.warn("Commerce connection not properly initialized.");
      return null;
    }

    String siteId = storeContext.getSiteId();
    Site site = getSitesService().getSite(siteId);
    if (site == null) {
      return null;
    }

    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);

    Content rootChannel = site.getSiteRootDocument();
    String categoryId = getCategoryExternalTechId(product);
    String orientation = findAttribute(request, SEGMENT_ORIENTATION).orElse(DEFAULT_ORIENTATION);
    String types = findAttribute(request, SEGMENT_TYPES).orElse(DEFAULT_SEGMENT_TYPES);

    Map<String, String> paramMap = new HashMap<>();
    paramMap.put(SEGMENT_CATEGORY_ID, categoryId);
    paramMap.put(SEGMENT_PRODUCT_ID, product.getExternalTechId());
    paramMap.put(SEGMENT_ORIENTATION, orientation);
    paramMap.put(SEGMENT_TYPES, types);
    paramMap.put(SEGMENT_VIEW, VIEW_NAME);
    paramMap.put(SEGMENT_CATALOG_ALIAS, product.getId().getCatalogAlias().value());
    if (rootChannel != null) {
      String vanityName = urlPathFormattingHelper.getVanityName(rootChannel);
      paramMap.put(SEGMENT_SITE, vanityName);
    }

    return result.buildAndExpand(paramMap);
  }

  @NonNull
  private static Optional<String> findAttribute(@NonNull HttpServletRequest request, @NonNull String name) {
    Object attribute = request.getAttribute(name);

    return Optional.ofNullable(attribute)
            .filter(String.class::isInstance)
            .map(String.class::cast);
  }

  private static String getCategoryExternalTechId(@NonNull Product product) {
    Category category = product.getCategory();
    return category.getExternalTechId();
  }

  @Nullable
  private Site getSiteByName(@NonNull String siteName) {
    Set<Site> sites = getSitesService().getSites();

    for (Site site : sites) {
      try {
        Content rootChannel = site.getSiteRootDocument();
        if (rootChannel == null) {
          continue;
        }
        String vanityName = urlPathFormattingHelper.getVanityName(rootChannel);
        if (siteName.equalsIgnoreCase(vanityName)) {
          return site;
        }
      } catch (CapObjectDestroyedException | SiteDestroyedException e) {
        LOG.debug("ignoring destroyed site '{}'", site.getId(), e);
      }
    }

    return null;
  }

  @NonNull
  public static List<VariantFilter> parseAttributesToFilters(@NonNull String attributes) {
    // we support two different formats: semicolon separated list of alternating keys and values (eg. a;2;b;3;c;4)
    List<VariantFilter> result = parseAttributesFromSSL(attributes);

    // ... and the good old comma separated key value pair list (eg. a=2,b=3,c=4)
    return !result.isEmpty() ? result : parseAttributesFromCSL(attributes);
  }

  @NonNull
  public static List<VariantFilter> parseAttributesFromCSL(@NonNull String attributes) {
    List<VariantFilter> result = new ArrayList<>();

    String[] kvPairs = attributes.split(",");
    for (String kvPair : kvPairs) {
      int index = kvPair.indexOf('=');
      if (index > 0) {
        String key = kvPair.substring(0, index);
        String value = kvPair.substring(index + 1);
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
          result.add(AxisFilter.on(key, value));
        }
      }
    }

    return result;
  }

  @NonNull
  public static List<VariantFilter> parseAttributesFromSSL(@NonNull String attributes) {
    List<VariantFilter> result = new ArrayList<>();

    String[] tokens = attributes.split(";");
    if (tokens.length > 0 && attributes.contains(";")) {
      for (int i = 0; i < tokens.length; i += 2) {
        String key = tokens[i];
        String value = i + 1 <= tokens.length - 1 ? tokens[i + 1] : "";
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
          result.add(AxisFilter.on(key, value));
        }
      }
    }

    return result;
  }

  @Nullable
  private Product findProduct(String productId, @Nullable String skuId, @Nullable CatalogAlias catalogAlias,
                              @Nullable String attributes, @NonNull StoreContext storeContext) {
    if (catalogAlias == null) {
      catalogAlias = storeContext.getCatalogAlias();
    }

    if (StringUtils.isBlank(attributes)) {
      return findProductWithoutAttributes(catalogAlias, productId, skuId, storeContext);
    } else {
      return findProductWithAttributes(catalogAlias, productId, attributes, storeContext);
    }
  }

  @Nullable
  private Product findProductWithoutAttributes(@NonNull CatalogAlias catalogAlias, String productId,
                                               @Nullable String skuId, @NonNull StoreContext storeContext) {
    // in general an existing skuId parameter is more current than a productId param
    // in case a skuId was passed we hope it is a real SKU and we can close case
    if (StringUtils.isNotBlank(skuId)) {
      ProductVariant productVariantForSkuId = loadProductVariant(catalogAlias, skuId, storeContext);
      if (productVariantForSkuId != null && productVariantForSkuId.isVariant()) {
        return productVariantForSkuId;
      }
    }

    // we give it a chance that the productId is actually a skuId
    ProductVariant productVariantForProductId = loadProductVariant(catalogAlias, productId, storeContext);
    if (productVariantForProductId != null && productVariantForProductId.isVariant()) {
      return productVariantForProductId;
    }

    // if not we convert it to a product and take it
    Product product = loadProduct(catalogAlias, productId, storeContext);

    if (product == null) {
      LOG.warn("Cannot handle request because neither the product '{}' nor the SKU '{}' can be determined.",
              productId, skuId);
      return null;
    }

    return product;
  }

  @Nullable
  private Product findProductWithAttributes(@NonNull CatalogAlias catalogAlias, String productId,
                                            @NonNull String attributes, @NonNull StoreContext storeContext) {
    // attention: in case the attributes are set we do not trust the skuId or productId parameter
    // we always try to determine the base product and retrieve the SKU from given attributes
    Product product = loadProduct(catalogAlias, productId, storeContext);
    if (product == null) {
      LOG.warn("Cannot handle request because the product with ID '{}' cannot be determined.", productId);
      return null;
    }

    if (product.isVariant()) {
      ProductVariant productVariant = loadProductVariant(catalogAlias, productId, storeContext);
      if (productVariant == null) {
        LOG.warn("Cannot handle request because the SKU with ID '{}' cannot be determined.", productId);
        return null;
      }

      product = productVariant.getParent();
      if (product == null) {
        LOG.warn("Cannot handle request because the base product of the SKU '{}' cannot be determined.", productId);
        return null;
      }
    }

    List<VariantFilter> filters = parseAttributesToFilters(attributes);
    List<ProductVariant> productVariants = product.getVariants(filters);

    return productVariants.stream()
            .findFirst()
            .map(Product.class::cast)
            .orElse(product);
  }

  private CommerceId formatProductId(CatalogAlias catalogAlias, String externalId, @NonNull StoreContext storeContext) {
    CommerceIdProvider commerceIdProvider = storeContext.getConnection().getIdProvider();

    return useStableIds
            ? commerceIdProvider.formatProductId(catalogAlias, externalId)
            : commerceIdProvider.formatProductTechId(catalogAlias, externalId);
  }

  private CommerceId formatProductVariantId(CatalogAlias catalogAlias, String externalId,
                                            @NonNull StoreContext storeContext) {
    CommerceIdProvider commerceIdProvider = storeContext.getConnection().getIdProvider();

    return useStableIds
            ? commerceIdProvider.formatProductVariantId(catalogAlias, externalId)
            : commerceIdProvider.formatProductVariantTechId(catalogAlias, externalId);
  }

  @Nullable
  private Product loadProduct(@NonNull CatalogAlias catalogAlias, String externalId,
                              @NonNull StoreContext storeContext) {
    CommerceId productId = formatProductId(catalogAlias, externalId, storeContext);
    return loadCommerceBean(productId, storeContext);
  }

  @Nullable
  private ProductVariant loadProductVariant(@NonNull CatalogAlias catalogAlias, String externalId,
                                            @NonNull StoreContext storeContext) {
    CommerceId productVariantId = formatProductVariantId(catalogAlias, externalId, storeContext);
    Product possibleProductVariant = loadCommerceBean(productVariantId, storeContext);
    return possibleProductVariant instanceof ProductVariant ? (ProductVariant) possibleProductVariant : null;
  }

  @Nullable
  private static <T extends CommerceBean> T loadCommerceBean(@NonNull CommerceId commerceId,
                                                             @NonNull StoreContext storeContext) {
    CommerceConnection commerceConnection = storeContext.getConnection();
    CommerceBeanFactory commerceBeanFactory = commerceConnection.getCommerceBeanFactory();
    return (T) commerceBeanFactory.loadBeanFor(commerceId, storeContext);
  }

  public void setUseStableIds(boolean useStableIds) {
    this.useStableIds = useStableIds;
  }
}
