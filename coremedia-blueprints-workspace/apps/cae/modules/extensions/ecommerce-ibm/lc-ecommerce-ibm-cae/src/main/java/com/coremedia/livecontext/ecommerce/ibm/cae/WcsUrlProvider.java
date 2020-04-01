package com.coremedia.livecontext.ecommerce.ibm.cae;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceBean;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.cae.handlers.PreviewHandler;
import com.coremedia.livecontext.contentbeans.CMExternalPage;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.ForVendor;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;
import com.coremedia.livecontext.handler.LiveContextProductSeoLinkBuilderHelper;
import com.coremedia.livecontext.handler.LiveContextUrlProvider;
import com.coremedia.objectserver.web.links.TokenResolverHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.handler.LiveContextPageHandlerBase.URL_PROVIDER_IS_INITIAL_STUDIO_REQUEST;
import static com.coremedia.livecontext.handler.LiveContextPageHandlerBase.URL_PROVIDER_IS_STUDIO_PREVIEW;
import static com.coremedia.livecontext.handler.LiveContextPageHandlerBase.URL_PROVIDER_QUERY_PARAMS;
import static com.coremedia.livecontext.handler.LiveContextPageHandlerBase.URL_PROVIDER_SEO_SEGMENT;
import static com.coremedia.livecontext.handler.LiveContextPageHandlerBase.URL_PROVIDER_URL_TEMPLATE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * WCS-specific implementation for the LiveContextUrlProvider
 */
@ForVendor("ibm")
public class WcsUrlProvider implements LiveContextUrlProvider {

  protected static final String PARAM_STORE_ID = "storeId";
  protected static final String NEW_PREVIEW_SESSION_VARIABLE = "newPreviewSession";
  private static final String PARAM_CATALOG_ID = "catalogId";
  protected static final String PARAM_LANG_ID = "langId";
  public static final String URL_TEMPLATE = "urlTemplate";
  public static final String SEO_SEGMENT = "seoSegment";
  public static final String SEARCH_TERM = "searchTerm";
  public static final String IS_STUDIO_PREVIEW = "isStudioPreview";
  public static final String IS_INITIAL_STUDIO_REQUEST = "isInitialStudioRequest";
  protected static final String QUERY_PARAMS = "queryParams";
  protected static final String PRODUCT_ID = "productId";
  protected static final String CATEGORY_ID = "categoryId";
  public static final String CATALOG_ID = "catalogId";

  private static final String PARAM_LANGUAGE = "language";
  private static final String PARAM_SEO_SEGMENT = "seoSegment";
  private static final String PARAM_STORE_NAME = "storeName";
  private static final String PARAM_SEARCH_TERM = "searchTerm";
  private static final String REDIRECT_URL = "redirectUrl";
  private static final String PARAM_CONTRACT_ID_FOR_PREVIEW = "contractId";

  private static final String SEO_URI_PREFIX = "/{language}/{storeName}/";

  private CatalogAliasTranslationService catalogAliasTranslationService;
  private String defaultStoreFrontUrl;
  private String previewStoreFrontUrl;
  private String urlPattern;
  private String shoppingFlowUrlForContractPreview;
  private String productNonSeoUrl;
  private String categoryNonSeoUrl;
  private LiveContextProductSeoLinkBuilderHelper liveContextProductSeoLinkBuilderHelper;

  private static boolean isStudioPreview(@NonNull Map<String, Object> parameters) {
    return findBooleanParameterValue(parameters, IS_STUDIO_PREVIEW).orElse(false);
  }

  private static boolean isInitialStudioRequest(@NonNull Map<String, Object> parameters) {
    return findBooleanParameterValue(parameters, IS_INITIAL_STUDIO_REQUEST).orElse(false);
  }

  @NonNull
  private static Optional<Boolean> findBooleanParameterValue(@NonNull Map<String, Object> parameters,
                                                             @NonNull String key) {
    return Optional.ofNullable(parameters.get(key))
            .filter(Boolean.class::isInstance)
            .map(Boolean.class::cast);
  }

  private static boolean isNonDefaultCatalog(@NonNull Map<String, Object> parameters) {
    return parameters.containsKey(CATALOG_ID) && null != parameters.get(CATALOG_ID);
  }

  private static boolean isContractPreview(@NonNull StoreContext storeContext, boolean isStudioPreview) {
    return isStudioPreview && !storeContext.getContractIdsForPreview().isEmpty();
  }

  protected void configureParametersForUrlReplacements(@NonNull Map<String, Object> parameters,
                                                       @NonNull StoreContext storeContext) {
    parameters.put(PARAM_STORE_NAME, StringUtils.lowerCase(storeContext.getStoreName()));
    parameters.put(PARAM_LANGUAGE, storeContext.getLocale().getLanguage());

    // catalog ID
    String storeId = storeContext.getStoreId();
    Optional<CatalogId> catalogId = storeContext.getCatalogId();
    if (!catalogId.isPresent()) {
      CatalogAlias catalogAlias = storeContext.getCatalogAlias();
      String siteId = storeContext.getSiteId();
      catalogId = catalogAliasTranslationService.getCatalogIdForAlias(catalogAlias, storeContext);
    }

    // The language ID has to be transformed into the format of the commerce system.
    Locale locale = StoreContextHelper.getLocale(storeContext);
    String languageId = getLanguageId(storeContext, locale);
    parameters.put(PARAM_LANG_ID, languageId);
    parameters.put(PARAM_LANGUAGE, locale.getLanguage());

    // The catalog ID may be defaulted to the store ID if the store is not an e-store.
    if (!catalogId.isPresent()) {
      catalogId = Optional.of(CatalogId.of(storeId));
    }

    parameters.put(PARAM_STORE_ID, storeId);
    parameters.put(PARAM_CATALOG_ID, catalogId.map(CatalogId::value).orElse(null));
  }

  @NonNull
  private static String getLanguageId(@NonNull StoreContext storeContext, @NonNull Locale locale) {
    CommerceConnection connection = storeContext.getConnection();
    CatalogServiceImpl catalogService = (CatalogServiceImpl) connection.getCatalogService();
    return catalogService.getLanguageId(locale);
  }

  @Autowired
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  @Autowired
  void setLiveContextProductSeoLinkBuilderHelper(
          LiveContextProductSeoLinkBuilderHelper liveContextProductSeoLinkBuilderHelper) {
    this.liveContextProductSeoLinkBuilderHelper = liveContextProductSeoLinkBuilderHelper;
  }

  /**
   * The URL template is not mandatory and may be passed with the parameter array of the "provideValue" method.
   */
  public String getUrlPattern() {
    return urlPattern;
  }

  public String getDefaultStoreFrontUrl() {
    return defaultStoreFrontUrl;
  }

  public void setDefaultStoreFrontUrl(String defaultStoreFrontUrl) {
    this.defaultStoreFrontUrl = defaultStoreFrontUrl;
  }

  public String getPreviewStoreFrontUrl() {
    return previewStoreFrontUrl;
  }

  public void setPreviewStoreFrontUrl(String previewStoreFrontUrl) {
    this.previewStoreFrontUrl = previewStoreFrontUrl;
  }

  public void setUrlPattern(String urlPattern) {
    this.urlPattern = urlPattern;
  }

  public void setShoppingFlowUrlForContractPreview(String shoppingFlowUrlForContractPreview) {
    this.shoppingFlowUrlForContractPreview = shoppingFlowUrlForContractPreview;
  }

  public void setProductNonSeoUrl(String productNonSeoUrl) {
    this.productNonSeoUrl = productNonSeoUrl;
  }

  public void setCategoryNonSeoUrl(String categoryNonSeoUrl) {
    this.categoryNonSeoUrl = categoryNonSeoUrl;
  }

  /**
   * The method expects at least the store context for the URL formatting.
   * Additional optional values may be passed with the array.
   *
   * @param parameters   The parameters that are used to format the URL. The following values may be passed:
   *                     <ol>
   *                     <li>StoreContext (optional)</li>
   *                     <li>URL Template (optional)</li>
   *                     <li>URL Parameters Map (optional)</li>
   *                     <li>SEO Segment (optional)</li>
   *                     <li>Search Term (optional)</li>
   *                     <li>StudioPreview Flag(optional)</li>
   *                     </ol>
   * @param request
   * @param storeContext
   */
  @NonNull
  public Optional<UriComponents> provideValue(@NonNull Map<String, Object> parameters,
                                              @NonNull HttpServletRequest request,
                                              @Nullable StoreContext storeContext) {
    return buildLink(parameters, storeContext)
            .map(UriComponentsBuilder::build);
  }

  @NonNull
  private Optional<UriComponentsBuilder> buildLink(@NonNull Map<String, Object> parameters,
                                                   @Nullable StoreContext storeContext) {
    String resultUrl = getUrlPattern();
    boolean isStudioPreview = isStudioPreview(parameters);

    if (!parameters.isEmpty()) {
      // Optional URL template to overwrite the default Spring property.
      resultUrl = evaluateUrlTemplate(parameters).orElse(resultUrl);

      if (isNullOrEmpty(resultUrl)) {
        return Optional.empty();
      }

      // Compile shopping flow URL if contract IDs for preview are stored in the store context.
      if (storeContext != null && isContractPreview(storeContext, isStudioPreview)) {
        String redirectUrl = applyParameters(resultUrl, parameters, storeContext);
        redirectUrl = redirectUrl.startsWith("/") ? redirectUrl.substring(1) : redirectUrl;
        parameters.put(REDIRECT_URL, redirectUrl);
        resultUrl = applyParameters(shoppingFlowUrlForContractPreview, parameters, storeContext);

        // Add contract IDs.
        resultUrl = UriComponentsBuilder.fromUriString(resultUrl)
                .queryParam(PARAM_CONTRACT_ID_FOR_PREVIEW, toArray(storeContext.getContractIdsForPreview()))
                .build()
                .toUriString();
      } else {
        resultUrl = applyParameters(resultUrl, parameters, storeContext);
      }
    }

    //TODO maybe better no absolute URL if fragmentContext available
    resultUrl = makeShopUrlAbsolute(resultUrl, isStudioPreview);
    resultUrl = applyParameters(resultUrl, parameters, storeContext);
    resultUrl = ensureNewSessionForNewStudioTabs(parameters, resultUrl);

    // Append catalog ID for non-default catalog URLs.
    if (isNonDefaultCatalog(parameters) && !resultUrl.contains(CATALOG_ID + "=")) {
      resultUrl = UriComponentsBuilder
              .fromUriString(resultUrl)
              .queryParam(CATALOG_ID, parameters.get(CATALOG_ID))
              .build()
              .toUriString();
    }

    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resultUrl);
    return Optional.of(builder);
  }

  @NonNull
  @Override
  public Optional<UriComponentsBuilder> buildCategoryLink(@NonNull Category category,
                                                          @NonNull Map<String, Object> queryParams,
                                                          @NonNull HttpServletRequest request) {
    Map<String, Object> newQueryParams = new HashMap<>(queryParams);
    newQueryParams.put(CATEGORY_ID, category.getExternalTechId());

    // Check if commerce bean link points to default catalog; add catalog ID parameter if that is not the case.
    AbstractCommerceBean.getCatalog(category).filter(c -> !c.isDefaultCatalog())
            .ifPresent(catalog -> newQueryParams.put(CATALOG_ID, catalog.getExternalId()));

    Map<String, Object> params = toSingleParam(newQueryParams, request);
    params.put(URL_PROVIDER_SEO_SEGMENT, category.getSeoSegment());

    return buildLink(params, category.getContext());
  }

  @NonNull
  @Override
  public Optional<UriComponentsBuilder> buildProductLink(@NonNull Product product,
                                                         @NonNull Map<String, Object> queryParams,
                                                         @NonNull HttpServletRequest request) {
    String seoSegments = liveContextProductSeoLinkBuilderHelper.buildSeoSegmentsFor(product);

    Map<String, Object> newQueryParams = new HashMap<>(queryParams);
    if (StringUtils.isBlank(seoSegments)) {
      // Build non-SEO URL including category/product ID.
      newQueryParams.put(PRODUCT_ID, product.getExternalTechId());
    }

    // Check if commerce bean link points to default catalog; add catalog ID parameter if that is not the case.
    AbstractCommerceBean.getCatalog(product).filter(c -> !c.isDefaultCatalog())
            .ifPresent(catalog -> newQueryParams.put(CATALOG_ID, catalog.getExternalId()));

    Map<String, Object> params = toSingleParam(newQueryParams, request);
    params.put(URL_PROVIDER_SEO_SEGMENT, seoSegments);

    return buildLink(params, product.getContext());
  }

  @NonNull
  @Override
  public Optional<UriComponentsBuilder> buildPageLink(@NonNull CMExternalPage navigation,
                                                      @NonNull Map<String, Object> queryParams,
                                                      @NonNull HttpServletRequest request,
                                                      @NonNull StoreContext storeContext) {
    String urlTemplate = navigation.getExternalUriPath();

    if (isEmpty(urlTemplate)) {
      urlTemplate = SEO_URI_PREFIX + navigation.getExternalId();
    }

    Map<String, Object> params = toSingleParam(queryParams, request);
    params.put(URL_PROVIDER_URL_TEMPLATE, urlTemplate);

    return buildLink(params, storeContext);
  }

  @NonNull
  @Override
  public Optional<UriComponentsBuilder> buildShopLink(@NonNull String seoSegments,
                                                      @NonNull Map<String, Object> queryParams,
                                                      @NonNull HttpServletRequest request,
                                                      @NonNull StoreContext storeContext) {
    Map<String, Object> params = toSingleParam(queryParams, request);
    params.put(URL_PROVIDER_SEO_SEGMENT, seoSegments);

    return buildLink(params, storeContext);
  }

  private static Map<String, Object> toSingleParam(@NonNull Map<String, Object> queryParams,
                                                   @NonNull HttpServletRequest request) {
    Map<String, Object> params = new HashMap<>();
    params.put(URL_PROVIDER_QUERY_PARAMS, queryParams);
    params.put(URL_PROVIDER_IS_STUDIO_PREVIEW, LiveContextPageHandlerBase.isStudioPreviewRequest(request));
    params.put(URL_PROVIDER_IS_INITIAL_STUDIO_REQUEST, PreviewHandler.isStudioPreviewRequest(request));
    params.put(CATALOG_ID, queryParams.get(CATALOG_ID));
    return params;
  }

  private static String ensureNewSessionForNewStudioTabs(@NonNull Map<String, Object> parameters, String resultUrl) {
    boolean isInitialStudioRequest = isInitialStudioRequest(parameters);
    if (isInitialStudioRequest) {
      resultUrl = UriComponentsBuilder
              .fromUriString(resultUrl)
              .queryParam(NEW_PREVIEW_SESSION_VARIABLE, Boolean.TRUE.toString())
              .build()
              .toUriString();
    }
    return resultUrl;
  }

  @NonNull
  private Optional<String> evaluateUrlTemplate(@NonNull Map<String, Object> parameters) {
    String urlTemplate = (String) parameters.get(URL_TEMPLATE);

    if (urlTemplate != null) {
      return Optional.of(urlTemplate);
    }

    String seoSegment = (String) parameters.get(SEO_SEGMENT);
    Map queryParams = (Map) parameters.get(QUERY_PARAMS);

    if (!isNullOrEmpty(seoSegment) || queryParams == null) {
      return Optional.empty();
    }

    if (queryParams.containsKey(PRODUCT_ID)) {
      return Optional.ofNullable(productNonSeoUrl);
    } else if (queryParams.containsKey(CATEGORY_ID)) {
      return Optional.ofNullable(categoryNonSeoUrl);
    } else {
      return Optional.empty();
    }
  }

  private String applyParameters(@Nullable String url, @NonNull Map<String, Object> parameters,
                                 StoreContext storeContext) {
    Map<String, Object> configuredParams = new HashMap<>();

    // optional SEO segment
    configuredParams.put(PARAM_SEO_SEGMENT, parameters.get(SEO_SEGMENT));

    // optional search term
    configuredParams.put(PARAM_SEARCH_TERM, parameters.get(SEARCH_TERM));

    // optional redirect URL
    configuredParams.put(REDIRECT_URL, parameters.get(REDIRECT_URL));

    // map info from store context
    if (storeContext != null) {
      configureParametersForUrlReplacements(configuredParams, storeContext);
    }

    Object queryParams = parameters.get(QUERY_PARAMS);
    if (queryParams instanceof Map) {
      Map queryParamsMap = (Map) queryParams;
      configuredParams.putAll(queryParamsMap);
    }

    String commerceTokensReplacedUrl = CommercePropertyHelper.replaceTokens(url, storeContext);
    return TokenResolverHelper.replaceTokens(commerceTokensReplacedUrl, configuredParams, false, false);
  }

  @NonNull
  private String makeShopUrlAbsolute(@NonNull String url, boolean isStudioPreview) {
    if (url.startsWith("http") || url.startsWith("//")) {
      return url;
    }

    boolean usePreviewStoreFrontUrl = isStudioPreview && getPreviewStoreFrontUrl() != null;
    String prefix = usePreviewStoreFrontUrl ? getPreviewStoreFrontUrl() : getDefaultStoreFrontUrl();

    // Avoid `//` in concatenated URLs.
    if (!prefix.endsWith("/")) {
      prefix += "/";
    }

    String relativeUrlPart = url;
    if (relativeUrlPart.startsWith("/")) {
      relativeUrlPart = relativeUrlPart.substring(1);
    }

    return prefix + relativeUrlPart;
  }

  @NonNull
  private static String[] toArray(@NonNull List<String> items) {
    // `toArray(new T[0])` as per https://shipilev.net/blog/2016/arrays-wisdom-ancients/
    //noinspection ToArrayCallWithZeroLengthArrayArgument
    return items.toArray(new String[0]);
  }
}
