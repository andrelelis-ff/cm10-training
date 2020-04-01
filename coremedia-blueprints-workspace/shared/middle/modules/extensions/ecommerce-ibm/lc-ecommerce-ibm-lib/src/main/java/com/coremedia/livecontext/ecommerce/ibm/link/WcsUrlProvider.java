package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceBean;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.link.QueryParam;
import com.coremedia.objectserver.web.links.TokenResolverHelper;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.link.UrlUtil.convertToParamMap;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.springframework.web.util.UriUtils.encodeQueryParam;

/**
 * WCS-specific implementation for the LiveContextUrlProvider
 */
public class WcsUrlProvider {

  private static final Logger LOG = LoggerFactory.getLogger(WcsUrlProvider.class);

  @VisibleForTesting static final String PARAM_STORE_ID = "storeId";
  private static final String NEW_PREVIEW_SESSION_VARIABLE = "newPreviewSession";
  private static final String PARAM_CATALOG_ID = "catalogId";
  @VisibleForTesting static final String PARAM_LANG_ID = "langId";
  @VisibleForTesting static final String URL_TEMPLATE = "urlTemplate";
  @VisibleForTesting static final String SEO_SEGMENT = "seoSegment";
  @VisibleForTesting static final String SEARCH_TERM = "searchTerm";
  private static final String IS_INITIAL_STUDIO_REQUEST = "isInitialStudioRequest";
  @VisibleForTesting static final String QUERY_PARAMS = "queryParams";
  @VisibleForTesting static final String PRODUCT_ID = "productId";
  @VisibleForTesting static final String CATEGORY_ID = "categoryId";
  @VisibleForTesting static final String CATALOG_ID = "catalogId";

  private static final String PARAM_LANGUAGE = "language";
  private static final String PARAM_SEO_SEGMENT = "seoSegment";
  private static final String PARAM_STORE_NAME = "storeName";
  private static final String PARAM_SEARCH_TERM = "searchTerm";
  private static final String REDIRECT_URL = "redirectUrl";
  private static final String PARAM_CONTRACT_ID_FOR_PREVIEW = "contractId";

  private static final String SEO_URI_PREFIX = "/{language}/{storeName}/";

  private static final String DEFAULT_LANGUAGE_ID = "-1";

  private CatalogAliasTranslationService catalogAliasTranslationService;
  private String defaultStoreFrontUrl;
  private String previewStoreFrontUrl;
  private String urlPattern;
  private String shoppingFlowUrlForContractPreview;
  private String shoppingFlowUrlForContractPreviewWcs9;
  private String productNonSeoUrl;
  private String categoryNonSeoUrl;

  @Value("${cae.is.preview:false}")
  private boolean preview;

  @Value("${livecontext.max-category-segments:2}")
  private int wcsStorefrontMaxUrlSegments = 2;

  public boolean isPreview() {
    return preview;
  }

  @VisibleForTesting
  void setPreview(boolean preview) {
    this.preview = preview;
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
    Optional<String> languageId = findLanguageId(locale);
    parameters.put(PARAM_LANG_ID, languageId.orElse(DEFAULT_LANGUAGE_ID));
    parameters.put(PARAM_LANGUAGE, locale.getLanguage());

    if (languageId.isPresent()) {
      // The catalog ID may be defaulted to the store ID if the store is not an e-store.
      if (!catalogId.isPresent()) {
        catalogId = Optional.of(CatalogId.of(storeId));
      }

      parameters.put(PARAM_STORE_ID, storeId);
      parameters.put(PARAM_CATALOG_ID, catalogId.map(CatalogId::value).orElse(null));
    }
  }

  @NonNull
  private static Optional<String> findLanguageId(@NonNull Locale locale) {
    return CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .map(CommerceConnection::getCatalogService)
            .filter(CatalogServiceImpl.class::isInstance)
            .map(CatalogServiceImpl.class::cast)
            .map(catalogService -> catalogService.getLanguageId(locale));
  }

  @Autowired
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
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

  public void setShoppingFlowUrlForContractPreviewWcs9(String shoppingFlowUrlForContractPreviewWcs9) {
    this.shoppingFlowUrlForContractPreviewWcs9 = shoppingFlowUrlForContractPreviewWcs9;
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

  private static String insertContractIdParams(@NonNull String urlPattern, @NonNull Iterable<String> contractIds) {
    if (urlPattern.contains("{{contractIdParams}}")) {
      StringBuilder replacement = new StringBuilder();
      contractIds.forEach(string -> replacement
              .append("&")
              .append(PARAM_CONTRACT_ID_FOR_PREVIEW)
              .append("=")
              .append(string));
      return urlPattern.replace("{{contractIdParams}}", replacement.substring(1));
    }
    return urlPattern;
  }

  private static String encodeCascadedRedirectUrlParams(@NonNull String url) {

    int posAssignment = url.indexOf("&URL=") + 5;
    if (posAssignment >= 5) {
      String baseUrl = substring(url, 0, posAssignment);
      String unencodedParam = substring(url, posAssignment);
      String unencodedParamCascaded = encodeCascadedRedirectUrlParams(unencodedParam);
      String encodedParamCascaded = encodeQueryParam(unencodedParamCascaded, UTF_8);
      String encodedUrl = baseUrl + encodedParamCascaded;
      return encodedUrl;
    }

    return url;
  }

  @NonNull
  private Optional<UriComponentsBuilder> buildLink(@NonNull Map<String, Object> parameters,
                                                   @Nullable StoreContext storeContext) {
    String resultUrl = getUrlPattern();

    if (!parameters.isEmpty()) {
      // Optional URL template to overwrite the default Spring property.
      resultUrl = evaluateUrlTemplate(parameters).orElse(resultUrl);

      if (isNullOrEmpty(resultUrl)) {
        return Optional.empty();
      }

      // Compile shopping flow URL if contract IDs for preview are stored in the store context.
      if (storeContext != null && isContractPreview(storeContext, isPreview())) {
        String redirectUrl = applyParameters(resultUrl, parameters, storeContext);
        redirectUrl = redirectUrl.startsWith("/") ? redirectUrl.substring(1) : redirectUrl;
        parameters.put(REDIRECT_URL, redirectUrl);

        if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_9_0)) {
          resultUrl = applyParameters(shoppingFlowUrlForContractPreview, parameters, storeContext);

          // Add contract IDs.
          resultUrl = UriComponentsBuilder.fromUriString(resultUrl)
                  .queryParam(PARAM_CONTRACT_ID_FOR_PREVIEW, toArray(storeContext.getContractIdsForPreview()))
                  .build()
                  .toUriString();
        } else {
          configureParametersForUrlReplacements(parameters, storeContext);
          resultUrl = insertContractIdParams(shoppingFlowUrlForContractPreviewWcs9, storeContext.getContractIdsForPreview());
          resultUrl = CommercePropertyHelper.replaceTokens(resultUrl, parameters, false);
          resultUrl = CommercePropertyHelper.replaceTokens(resultUrl, storeContext);

          LOG.debug("Encoding WCS9 contract preview URL {}", resultUrl);
          resultUrl = encodeCascadedRedirectUrlParams(resultUrl);
          LOG.debug("Encoded WCS9 contract preview URL {}", resultUrl);

          // Proper and complete encoding of cascaded redirect URI params for WCS can only be achieved
          // here (in #encodeCascadedRedirectUrlParams). There's another URI encoding done later on in
          // com.coremedia.objectserver.web.links.LinkBindings#convertUriToString that must be prevented.
          // The 'PreventAnotherUrlEncodingForWcsContractPreviewInStudio' fragment is used as identifier
          // for the URIs that must not be encoded once again. Unfortunately there's no proper place that
          // is accessible from both involved classes where a common constant with the fragment name may
          // be defined.
          resultUrl += "#PreventAnotherUrlEncodingForWcsContractPreviewInStudio";
        }
      } else {
        resultUrl = applyParameters(resultUrl, parameters, storeContext);
      }
    }

    //TODO maybe better no absolute URL if fragmentContext available
    resultUrl = makeShopUrlAbsolute(resultUrl);
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
  public Optional<UriComponentsBuilder> buildCategoryLink(@NonNull Category category,
                                                          @NonNull List<QueryParam> queryParams,
                                                          boolean isStudioPreview) {
    Map<String, Object> newQueryParams = convertToParamMap(queryParams);
    newQueryParams.put(CATEGORY_ID, category.getExternalTechId());
    addCatalogId(category, newQueryParams);

    Map<String, Object> params = toSingleParam(newQueryParams, isStudioPreview);
    params.put(SEO_SEGMENT, category.getSeoSegment());

    return buildLink(params, category.getContext());
  }

  private void addCatalogId(@NonNull CommerceBean commerceBean, Map<String, Object> queryParams) {
    // Check if commerce bean link points to default catalog; add catalog ID parameter if that is not the case.
    AbstractCommerceBean.getCatalog(commerceBean)
            .filter(catalog -> !catalog.isDefaultCatalog())
            .map(Catalog::getExternalId)
            .ifPresent(catalogId -> queryParams.put(CATALOG_ID, catalogId));
  }

  @NonNull
  public Optional<UriComponentsBuilder> buildProductLink(@NonNull Product product,
                                                         @NonNull List<QueryParam> queryParams,
                                                         boolean isStudioPreview) {

    String seoSegments = buildSeoSegmentsFor(product);

    Map<String, Object> newQueryParams = convertToParamMap(queryParams);
    if (StringUtils.isBlank(seoSegments)) {
      // Build non-SEO URL including category/product ID.
      newQueryParams.put(PRODUCT_ID, product.getExternalTechId());
    }

    addCatalogId(product, newQueryParams);

    Map<String, Object> params = toSingleParam(newQueryParams, isStudioPreview);
    params.put(SEO_SEGMENT, seoSegments);

    return buildLink(params, product.getContext());
  }

  @NonNull
  public Optional<UriComponentsBuilder> buildExternalPageSeoLink(@Nullable String seoPath,
                                                                 @NonNull List<QueryParam> queryParams,
                                                                 boolean isStudioPreview,
                                                                 @NonNull StoreContext storeContext) {
    String urlTemplate = SEO_URI_PREFIX + seoPath;

    return buildLink(urlTemplate, queryParams, isStudioPreview, storeContext);
  }

  @NonNull
  public Optional<UriComponentsBuilder> buildExternalPageNonSeoLink(@NonNull String nonSeoPath,
                                                                    @NonNull List<QueryParam> queryParams,
                                                                    boolean isStudioPreview,
                                                                    @NonNull StoreContext storeContext) {
    return buildLink(nonSeoPath, queryParams, isStudioPreview, storeContext);
  }

  private Optional<UriComponentsBuilder> buildLink(@NonNull String urlTemplate, @NonNull List<QueryParam> queryParams,
                                                   boolean isStudioPreview, @NonNull StoreContext storeContext) {
    Map<String, Object> params = toSingleParam(convertToParamMap(queryParams), isStudioPreview);
    params.put(URL_TEMPLATE, urlTemplate);

    return buildLink(params, storeContext);
  }

  @NonNull
  public Optional<UriComponentsBuilder> buildShopLink(@NonNull String seoSegments,
                                                      @NonNull List<QueryParam> queryParams,
                                                      boolean isStudioPreview,
                                                      @NonNull StoreContext storeContext) {
    Map<String, Object> params = toSingleParam(convertToParamMap(queryParams), isStudioPreview);
    params.put(SEO_SEGMENT, seoSegments);

    return buildLink(params, storeContext);
  }

  private static Map<String, Object> toSingleParam(@NonNull Map<String, Object> queryParams, boolean isStudioPreview) {
    Map<String, Object> params = new HashMap<>();
    params.put(QUERY_PARAMS, queryParams);
    params.put(IS_INITIAL_STUDIO_REQUEST, isStudioPreview);
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
  private String makeShopUrlAbsolute(@NonNull String url) {
    if (url.startsWith("http") || url.startsWith("//")) {
      return url;
    }

    boolean usePreviewStoreFrontUrl = isPreview() && getPreviewStoreFrontUrl() != null;
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

  /**
   * Return the SEO URL for the given commerce bean.
   */
  private String buildSeoSegmentsFor(@NonNull Product product) {
    StringBuilder segments = new StringBuilder();

    String seoSegment = product.getSeoSegment();
    Category category = product.getCategory();

    if (!StringUtils.isBlank(seoSegment)) {
      segments.append(buildSeoBreadCrumbs(category));
      segments.append(seoSegment);
    }

    return segments.toString();
  }

  /**
   * This method returns the string
   * with the whole category path of the current category starting with the top level category and ending with the
   * current category + '/'.
   */
  private String buildSeoBreadCrumbs(@NonNull Category category) {
    StringBuilder segments = new StringBuilder();

    List<Category> breadcrumb = category.getBreadcrumb();

    int breadcrumbSize = breadcrumb.size();
    if (breadcrumbSize > wcsStorefrontMaxUrlSegments) {
      breadcrumb = breadcrumb.subList(breadcrumbSize - wcsStorefrontMaxUrlSegments, breadcrumbSize);
    }

    for (Category c : breadcrumb) {
      segments.append(c.getSeoSegment());
      segments.append('/');
    }

    return segments.toString();
  }

  @NonNull
  private static String[] toArray(@NonNull List<String> items) {
    // `toArray(new T[0])` as per https://shipilev.net/blog/2016/arrays-wisdom-ancients/
    //noinspection ToArrayCallWithZeroLengthArrayArgument
    return items.toArray(new String[0]);
  }
}
