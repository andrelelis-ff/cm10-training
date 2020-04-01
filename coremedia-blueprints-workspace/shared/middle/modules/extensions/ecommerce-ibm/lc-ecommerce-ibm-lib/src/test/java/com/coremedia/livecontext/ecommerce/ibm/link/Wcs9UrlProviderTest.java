package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;
import static org.springframework.web.util.UriUtils.encodeQueryParam;

public class Wcs9UrlProviderTest extends WcsUrlProviderTest {

  private static final String SHOPPING_FLOW = "" +
          "/Logoff" +
          "  ?storeId=2".trim() +
          "  &URL=Logon".trim() +
          "    ?storeId={storeId}".trim() +
          "    &logonId=manni".trim() +
          "    &logonPassword=geheim".trim() +
          "    &reLogonURL=LogonForm".trim() +
          "    &catalogId={catalogId}".trim() +
          "    &langId={langId}".trim() +
          "    &URL=ContractSetInSession".trim() +
          "      ?{{contractIdParams}}".trim() +
          "      &URL={redirectUrl}".trim();

  private static final String SINGLE_ENCODED_ASSIGMENT_CHAR = encodeQueryParam("=", UTF_8);
  private static final String DOUBLE_ENCODED_ASSIGMENT_CHAR = encodeQueryParam(SINGLE_ENCODED_ASSIGMENT_CHAR, UTF_8);

  @Before
  public void setup() {
    testConfig.setWcsVersion("9.0");
    super.setup();
    testling.setShoppingFlowUrlForContractPreviewWcs9(SHOPPING_FLOW);
  }

  @Test
  public void testShoppingFlowUrl() {
    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, "{language}/{storeName}/{seoSegment}");
    params.put(WcsUrlProvider.SEO_SEGMENT, SEO_SEGMENT);
    testling.setPreview(true);

    StoreContext storeContextWithContractIds = IbmStoreContextBuilder
            .from(storeContext)
            .withContractIdsForPreview(ImmutableList.of("4711", "0815"))
            .build();

    String providedUrl = toUriString(testling.provideValue(params, request, storeContextWithContractIds));
    assertTrue(providedUrl.startsWith(previewStoreFront + "/Logoff?"));
    assertTrue(providedUrl.contains("URL=Logon?"));
    assertTrue(providedUrl.contains("URL" + SINGLE_ENCODED_ASSIGMENT_CHAR + "ContractSetInSession?"));
    assertTrue(providedUrl.contains("URL" + DOUBLE_ENCODED_ASSIGMENT_CHAR + "en/auroraesite/seo"));
    assertTrue(providedUrl.contains("contractId" + DOUBLE_ENCODED_ASSIGMENT_CHAR + "4711"));
    assertTrue(providedUrl.contains("contractId" + DOUBLE_ENCODED_ASSIGMENT_CHAR + "0815"));
    assertTrue(providedUrl.endsWith("#PreventAnotherUrlEncodingForWcsContractPreviewInStudio"));
  }
}
