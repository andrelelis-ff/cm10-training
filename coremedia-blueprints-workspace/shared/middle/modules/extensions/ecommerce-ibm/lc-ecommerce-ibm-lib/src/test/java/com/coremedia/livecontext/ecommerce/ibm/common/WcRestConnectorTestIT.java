package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.login.LoginService;
import com.coremedia.livecontext.ecommerce.ibm.login.WcCredentials;
import com.coremedia.livecontext.ecommerce.ibm.login.WcSession;
import com.coremedia.livecontext.ecommerce.ibm.order.WcCart;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.link.LinkService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_8;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SwitchableHoverflyExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_WcRestConnectorTestIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true

        )
)
public class WcRestConnectorTestIT extends AbstractWrapperServiceTestCase {

  private static final String PARAM_LANG_ID = "langId";
  private static final String PARAM_CURRENCY = "currency";
  private static final String PARAM_FOR_USER = "forUser";

  private static final WcRestServiceMethod<Map, Map> FIND_PERSON_BY_SELF = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/person/@self", Map.class, Map.class)
          .secure(true)
          .requiresAuthentication(true)
          .previewSupport(true)
          .build();

  private static final WcRestServiceMethod<WcCart, Void> GET_CART = WcRestServiceMethod
          .builder(HttpMethod.GET, "store/{storeId}/cart/@self", (Class<Void>) null, WcCart.class)
          .secure(true)
          .requiresAuthentication(true)
          .userCookiesSupport(true)
          .build();

  private static final WcRestServiceMethod<Map, Void> FIND_SUB_CATEGORIES_SEARCH = WcRestServiceMethod
          .builderForSearch(HttpMethod.GET, "store/{storeId}/categoryview/byParentCategory/{parentCategoryId}?profileName=CoreMedia_findSubCategories", Map.class)
          .previewSupport(true)
          .userCookiesSupport(true)
          .contractsSupport(true)
          .build();

  private static final String SITE_ID = "awesome-site";
  private static final CatalogId CATALOG_ID = CatalogId.of("catalogId");
  private static final Currency CURRENCY_EUR = Currency.getInstance("EUR");
  private static final Locale LOCALE_DE = Locale.GERMAN;

  @Inject
  protected WcRestConnector testling;

  @Inject
  private LoginService loginService;

  @Inject
  @Named("commerce:wcs1")
  private CommerceConnection connection;

  @BeforeEach
  void setup() {
    storeInfoService.getWcsVersion().ifPresent(testConfig::setWcsVersion);
    connection.setInitialStoreContext(testConfig.getStoreContext(connection));
  }

  /**
   * Attention: This test is not hoverfly ready. We should only run it when we test against the
   * real backend system (because it takes longer anyway).
   * To achieve this we test if the "hoverfly.ignoreTapes" Java property is set to "true".
   * This test only makes sense against wcs version <7.8, because ohterwise basic authentication is activated and no
   * reconnect is neccessary.
   */
  @Test
  void testReconnectForAuthorizedServiceCalls() {
    if (useTapes() || WCS_VERSION_7_8.compareTo(testConfig.getWcsVersion()) <= 0) {
      return;
    }

    StoreContext storeContext = testConfig.getStoreContext(connection);
    UserContext userContext = UserContext.builder().withUserName(TEST_USER).build();

    WcCredentials credentials = loginService.loginServiceIdentity(storeContext);
    credentials.getSession().setWCToken("1002%2cOeke6xduXJ%2ba1BTzBtkz1dYInKBdv5WLd5yBKF0NKe1BGCQivNu5r0uNrX5L8q1ibo8sLXxFXrk%2b%0d%0aFVEvfIZzytRYmjwqjAiryXQ8utp5G%2bcA4%2fg0s%2fGVRq7DiPbdBEUcvwhH6Tx3bJg%3d");
    StoreContextHelper.setCredentials(storeContext, credentials);

    WcRestConnector spiedTestling = spy(testling);

    Locale locale = storeContext.getLocale();
    Currency currency = storeContext.getCurrency();
    String userName = UserContextHelper.getForUserName(userContext);

    List<String> variableValues = newArrayList(storeContext.getStoreId());
    Map<String, String[]> parametersMap = createParametersMap(locale, currency, userName);
    Map bodyData = null;

    spiedTestling.callService(FIND_PERSON_BY_SELF, variableValues, parametersMap, bodyData, storeContext, userContext);

    verify(spiedTestling, times(2)).callServiceInternal(
            any(WcRestServiceMethod.class),
            any(List.class),
            any(Map.class),
            isNull(),
            any(StoreContext.class),
            any(UserContext.class)
    );
  }

  @Test
  void testGetRequestCookieHeader() {
    String cookieHeader = "myCookieHeader";

    StoreContext storeContext = buildStoreContext()
            .withWcsVersion(testConfig.getWcsVersion())
            .build();

    UserContext userContext = mock(UserContext.class);
    when(userContext.getCookieHeader()).thenReturn(cookieHeader);
    LoginService loginServiceMock = mock(LoginService.class);
    LinkService linkServiceMock = mock(LinkService.class);
    WcCredentials wcCredentialsMock = mock(WcCredentials.class);
    WcSession wcSessionMock = mock(WcSession.class);
    when(wcSessionMock.getWCToken()).thenReturn("WCToken");
    when(wcSessionMock.getWCTrustedToken()).thenReturn("WCTrustedToken");
    when(wcCredentialsMock.getSession()).thenReturn(wcSessionMock);

    try {
      testling.setLoginService(loginServiceMock);
      when(loginServiceMock.loginServiceIdentity(storeContext)).thenReturn(wcCredentialsMock);

      Map<String, String> requiredHeaders = testling.getRequiredHeaders(GET_CART, true, storeContext, userContext);

      String cookieHeader2 = requiredHeaders.get("Cookie");
      assertNotNull(cookieHeader2);
      assertEquals(cookieHeader, cookieHeader2);
    } finally {
      testling.setLoginService(loginService);
    }
  }

  @Test
  void testGetRequestCookieHeaderForContracts() {
    String cookieHeader = "myCookieHeader";

    StoreContextImpl storeContext = buildStoreContext()
            .withContractIds(ImmutableList.of("contractA", "contractB"))
            .withWcsVersion(WCS_VERSION_7_8)
            .build();

    UserContext userContext = mock(UserContext.class);
    when(userContext.getCookieHeader()).thenReturn(cookieHeader);

    LoginService loginServiceMock = mock(LoginService.class);
    WcCredentials wcCredentialsMock = mock(WcCredentials.class);
    WcSession wcSessionMock = mock(WcSession.class);
    when(wcSessionMock.getWCToken()).thenReturn("WCToken");
    when(wcSessionMock.getWCTrustedToken()).thenReturn("WCTrustedToken");
    when(wcCredentialsMock.getSession()).thenReturn(wcSessionMock);

    try {
      testling.setLoginService(loginServiceMock);
      when(loginServiceMock.loginServiceIdentity(storeContext)).thenReturn(wcCredentialsMock);

      Map<String, String> requiredHeaders = testling
              .getRequiredHeaders(FIND_SUB_CATEGORIES_SEARCH, true, storeContext, userContext);

      String cookieHeader2 = requiredHeaders.get("Cookie");
      assertNotNull(cookieHeader2);
      assertEquals(cookieHeader, cookieHeader2);
    } finally {
      testling.setLoginService(loginService);
    }
  }

  @Test
  void testGetRequestUri() {
    StoreContext storeContext = testConfig.getStoreContext(connection);

    Locale locale = storeContext.getLocale();
    Currency currency = CURRENCY_EUR;
    String userName = "mu&rk{e}l";

    List<String> variableValues = newArrayList("param1value", "param & 2 {value}", "param3value");
    Map<String, String[]> parametersMap = createParametersMap(locale, currency, userName);

    URI requestUri = wcRestConnector.buildRequestUri("store/{param1}/person/{param2}@self?q={param3}", true, false,
            variableValues, parametersMap, storeContext);
    String serviceEndpoint = System.getProperty("livecontext.ibm.wcs.secureUrl", "https://shop-ref.ecommerce.coremedia.com");
    assertEquals(serviceEndpoint + "/wcs/resources/store/param1value/person/param%20&%202%20%7Bvalue%7D@self?q=param3value&currency=EUR&forUser=mu%26rk%7Be%7Dl&langId=-1",
            requestUri.toString());
  }

  @NonNull
  private IbmStoreContextBuilder buildStoreContext() {
    return IbmStoreContextBuilder.from(connection, SITE_ID)
            .withStoreId("storeId")
            .withStoreName("storeName")
            .withCatalogId(CATALOG_ID)
            .withCurrency(CURRENCY_EUR)
            .withLocale(LOCALE_DE);
  }

  /**
   * Adds the given values to a parameters map
   */
  @NonNull
  private static Map<String, String[]> createParametersMap(Locale locale, Currency currency, String userName) {
    Map<String, String[]> parameters = new TreeMap<>();

    if (locale != null) {
      parameters.put(PARAM_LANG_ID, new String[]{"-1"});
    }

    if (currency != null) {
      parameters.put(PARAM_CURRENCY, new String[]{currency.toString()});
    }

    if (userName != null && !userName.isEmpty()) {
      parameters.put(PARAM_FOR_USER, new String[]{userName});
    }

    return parameters;
  }
}
