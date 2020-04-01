package com.coremedia.livecontext.ecommerce.ibm.cae;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmTestConfig;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion;
import com.coremedia.objectserver.web.links.TokenResolverHelper;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WcsUrlProviderTest {

  private static final String SEO_SEGMENT = "seo";
  private static final String SEARCH_TERM_WITH_UMLAUTS = "eté-küche";
  private static final String PRODUCT_ID = "1111";
  private static final String CATEGORY_ID = "2222";
  private static final String CATALOG_ID = "3333";

  private static final String URL_TEMPLATE = "/SearchDisplay?storeId={storeId}&storeName={storeName}&seoSegment={seoSegment}&searchTerm={searchTerm}&language={language}&catalogId={catalogId}&langId={langId}&pageSize=12";
  private static final String DEFAULT_STOREFRONT = "//shop-preview-production-helios.blueprint-box.vagrant/webapp/wcs/stores/servlet";
  private static final String PREVIEW_STOREFRONT = "//shop-preview-helios.blueprint-box.vagrant/webapp/wcs/preview/servlet";
  private static final String SHOPPING_FLOW = "/Logon?logonId=manni&logonPassword=geheim" +
          "&URL=ContractSetInSession?URL={redirectUrl}&reLogonURL=LogonForm&storeId={storeId}" +
          "&catalogId={catalogId}&langId={langId}";
  private static final String PRODUCT_NON_SEO_URL = "ProductDisplay?productId={productId}&storeId={storeId}&langId={langId}";
  private static final String CATEGORY_NON_SEO_URL = "CategoryDisplay?categoryId={categoryId}&storeId={storeId}&langId={langId}";

  private final IbmTestConfig testConfig = new IbmTestConfig();

  private CatalogServiceImpl catalogService;

  private WcsUrlProvider testling;
  private HttpServletRequest request;
  private StoreContextImpl storeContext;
  private String previewStoreFront;

  @Before
  public void setup() {
    testling = new WcsUrlProvider();
    testling.setDefaultStoreFrontUrl(DEFAULT_STOREFRONT);
    previewStoreFront = PREVIEW_STOREFRONT;
    if (WcsVersion.WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      previewStoreFront = previewStoreFront.replace("/wcs/", "/remote/");
    }
    testling.setPreviewStoreFrontUrl(previewStoreFront);
    testling.setUrlPattern(URL_TEMPLATE);
    testling.setShoppingFlowUrlForContractPreview(SHOPPING_FLOW);
    testling.setProductNonSeoUrl(PRODUCT_NON_SEO_URL);
    testling.setCategoryNonSeoUrl(CATEGORY_NON_SEO_URL);

    request = new MockHttpServletRequest();

    catalogService = mock(CatalogServiceImpl.class);
    when(catalogService.getLanguageId(any(Locale.class))).thenReturn("-1");

    CommerceConnection connection = mock(CommerceConnection.class);
    storeContext = testConfig.getStoreContext(connection);

    when(connection.getCatalogService()).thenReturn(catalogService);
    when(connection.getStoreContext()).thenReturn(storeContext);
  }

  @Test
  public void testUrlFormatting() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, URL_TEMPLATE);
    params.put(WcsUrlProvider.SEO_SEGMENT, SEO_SEGMENT);
    params.put(WcsUrlProvider.SEARCH_TERM, SEARCH_TERM_WITH_UMLAUTS);

    String formattedUrl = toUriString(testling.provideValue(params, request, storeContext));
    assertNotNull(formattedUrl);
    assertThat("URL Tokens got replaced and umlauts are correctly added to the URL.",
            formattedUrl,
            Matchers.allOf(Matchers.not(Matchers.containsString("{")),
                    Matchers.not(Matchers.containsString("}")),
                    Matchers.containsString(SEARCH_TERM_WITH_UMLAUTS)
            ));
  }

  @Test
  public void testUrlPreviewLive() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.IS_STUDIO_PREVIEW, true);

    String url;

    url = toUriString(testling.provideValue(params, request, storeContext));
    assertTrue(url.startsWith(previewStoreFront));

    params.put(WcsUrlProvider.IS_STUDIO_PREVIEW, false);

    url = toUriString(testling.provideValue(params, request, storeContext));
    assertTrue(url.startsWith(DEFAULT_STOREFRONT));

    params.remove(WcsUrlProvider.IS_STUDIO_PREVIEW);

    url = toUriString(testling.provideValue(params, request, storeContext));
    assertTrue(url.startsWith(DEFAULT_STOREFRONT));
  }

  @Test
  public void testUrlEmptyParameterMap() {
    assertEquals(UriComponentsBuilder.fromUriString(DEFAULT_STOREFRONT + URL_TEMPLATE).build().toString(),
            toUriString(testling.provideValue(emptyMap(), request, null)));
  }

  @Test
  public void testUrlTemplateIsEmpty() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, "");
    assertNull(toUriString(testling.provideValue(params, request, null)));

    params.put(WcsUrlProvider.URL_TEMPLATE, null);
    assertEquals(UriComponentsBuilder.fromUriString(DEFAULT_STOREFRONT + URL_TEMPLATE).build().toString(),
            toUriString(testling.provideValue(params, request, null)));
  }

  @Test
  public void testShoppingFlowUrl() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, "{language}/{storeName}/{seoSegment}");
    params.put(WcsUrlProvider.SEO_SEGMENT, SEO_SEGMENT);
    params.put(WcsUrlProvider.IS_STUDIO_PREVIEW, true);

    StoreContext storeContextWithContractIds = IbmStoreContextBuilder
            .from(storeContext)
            .withContractIdsForPreview(ImmutableList.of("4711", "0815"))
            .build();

    String providedUrl = toUriString(testling.provideValue(params, request, storeContextWithContractIds));
    assertTrue(providedUrl.contains("en/auroraesite/seo"));
    assertTrue(providedUrl.startsWith(previewStoreFront + "/Logon?"));
    assertTrue(providedUrl.contains("contractId=4711"));
    assertTrue(providedUrl.contains("contractId=0815"));
  }

  @Test
  public void testUrlWithRemainingTokens() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, "{language}/{storeName}/{seoSegment}");
    params.put(WcsUrlProvider.SEO_SEGMENT, "simsalabim");

    String providedUrl = toUriString(testling.provideValue(params, request, null));
    assertThat("Remaining tokens in the URL are kept.",
            providedUrl,
            Matchers.allOf(
                    Matchers.stringContainsInOrder(
                            asList("{language}", "{storeName}", "simsalabim")),
                    Matchers.not(Matchers.containsString("seoSegment")))
    );
  }

  @Test
  public void testNonSeoProductUrl() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.SEO_SEGMENT, "");

    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put(WcsUrlProvider.PRODUCT_ID, PRODUCT_ID);
    params.put(WcsUrlProvider.QUERY_PARAMS, queryParams);

    String providedUrl = toUriString(testling.provideValue(params, request, storeContext));
    String expectedUrl = UriComponentsBuilder.fromUriString(DEFAULT_STOREFRONT + "/" + PRODUCT_NON_SEO_URL).build().toString();

    Map<String, Object> parametersMap = new HashMap<>();
    parametersMap.put(WcsUrlProvider.PRODUCT_ID, PRODUCT_ID);
    Locale locale = StoreContextHelper.getLocale(storeContext);
    String languageId = catalogService.getLanguageId(locale);
    parametersMap.put(WcsUrlProvider.PARAM_LANG_ID, languageId);
    parametersMap.put(WcsUrlProvider.PARAM_STORE_ID, storeContext.getStoreId());

    expectedUrl = TokenResolverHelper.replaceTokens(expectedUrl, parametersMap, false, false);
    assertEquals(expectedUrl, providedUrl);
  }

  @Test
  public void testNonSeoCategoryUrl() {
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put(WcsUrlProvider.CATEGORY_ID, CATEGORY_ID);

    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.SEO_SEGMENT, "");
    params.put(WcsUrlProvider.QUERY_PARAMS, queryParams);

    String providedUrl = toUriString(testling.provideValue(params, request, storeContext));
    String expectedUrl = UriComponentsBuilder
            .fromUriString(DEFAULT_STOREFRONT + "/" + CATEGORY_NON_SEO_URL)
            .build()
            .toString();

    Map<String, Object> parametersMap = new HashMap<>();
    parametersMap.put(WcsUrlProvider.CATEGORY_ID, CATEGORY_ID);
    Locale locale = StoreContextHelper.getLocale(storeContext);
    String languageId = catalogService.getLanguageId(locale);
    parametersMap.put(WcsUrlProvider.PARAM_LANG_ID, languageId);
    parametersMap.put(WcsUrlProvider.PARAM_STORE_ID, storeContext.getStoreId());

    expectedUrl = TokenResolverHelper.replaceTokens(expectedUrl, parametersMap, false, false);
    assertEquals(expectedUrl, providedUrl);
  }

  @Test
  public void testWithCatalogId() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, "{language}/{storeName}/{seoSegment}");
    params.put(WcsUrlProvider.SEO_SEGMENT, "simsalabim");
    params.put(WcsUrlProvider.SEO_SEGMENT, SEO_SEGMENT);

    String providedUrlNoCatalogId = toUriString(testling.provideValue(params, request, storeContext));
    assertThat("catalogId param not contained",
            providedUrlNoCatalogId, Matchers.not(Matchers.containsString("catalogId=" + CATALOG_ID)));

    params.put(WcsUrlProvider.CATALOG_ID, CATALOG_ID);

    String providedUrlWithCatalogId = toUriString(testling.provideValue(params, request, storeContext));
    assertThat("catalogId param must be appended.",
            providedUrlWithCatalogId, Matchers.containsString("catalogId=" + CATALOG_ID));
  }

  @Nullable
  private String toUriString(
          @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UriComponents> uriComponents) {
    return uriComponents
            .map(UriComponents::toUriString)
            .orElse(null);
  }
}
