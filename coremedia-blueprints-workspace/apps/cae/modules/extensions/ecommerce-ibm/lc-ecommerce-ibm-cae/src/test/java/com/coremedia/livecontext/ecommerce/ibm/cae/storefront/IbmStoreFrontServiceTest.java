package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontConnector;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontResponse;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IbmStoreFrontServiceTest {

  private static final String STORE_ID = "Sirius Cybernetics Corporation";
  private static final CatalogId CATALOG_ID = CatalogId.of("Total Perspective Vortex");
  private static final String STOREFRONT_REQUEST_URL = "crash";
  private static final Map<String, String> PARAMETERS = new HashMap<>();
  private static final String GUEST_OR_LOGGEDIN_USER_ID = "38009";
  private static final String ANONYMOUS_USER_ID = "-1002";

  @Spy
  private IbmStoreFrontService testling;

  @Mock
  private StoreFrontConnector storeFrontConnector;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private StoreFrontResponse storeFrontResponse;

  @Mock
  private HttpServletRequest sourceRequest;

  @Mock
  private HttpServletResponse sourceResponse;

  @Mock
  private WcsUrlProvider urlProvider;

  private javax.servlet.http.Cookie[] sourceRequestCookies;

  @Test(expected = GeneralSecurityException.class)
  public void testHandleStoreFrontCallGeneralSecurityException() throws GeneralSecurityException {
    //noinspection unchecked
    when(storeFrontConnector.executeGet(any(String.class), any(Map.class), any(HttpServletRequest.class))).thenThrow(GeneralSecurityException.class);
    try {
      testling.handleStorefrontCall(STOREFRONT_REQUEST_URL, PARAMETERS, sourceRequest, sourceResponse);
    } finally {
      verifyNoCookiesAtAll();
    }
  }

  @Test
  public void handleStoreFrontCallEmptyCookies() throws GeneralSecurityException {
    StoreFrontResponse response = testling.handleStorefrontCall(STOREFRONT_REQUEST_URL, PARAMETERS, sourceRequest, sourceResponse);

    assertNotNull(response);
    verifyNoCookiesAtAll();
  }

  @Test
  public void isLoggedInServletRequestNoCookiesAtAll() {
    when(sourceRequest.getCookies()).thenReturn(null);
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontEmptyCookies() {
    when(storeFrontResponse.getCookies()).thenReturn(Collections.emptyMap());
    assertFalse(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestEmptyCookies() {
    when(sourceRequest.getCookies()).thenReturn(new javax.servlet.http.Cookie[]{});
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontIrrelevantCookies() {
    initializeCookies(new String[]{
            "By-products of Designer People",
            "Happy Vertical People Transporter"
    }, new String[]{
            "42",
            "42"
    });
    assertFalse(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestIrrelevantCookies() {
    initializeCookies(new String[]{
            "By-products of Designer People",
            "Happy Vertical People Transporter"
    }, new String[]{
            "42",
            "42"
    });
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontOnlyDeleteCookies() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "DEL",
            "DEL"
    });
    assertFalse(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestOnlyDeleteCookies() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "DEL",
            "DEL"
    });
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontBlankCookieValues() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "",
            ""
    });
    assertFalse(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestBlankCookieValues() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "",
            ""
    });
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontAnonymousToGuest() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "38009",
            "DEL",
            "DEL"
    });
    assertTrue(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestAnonymousToGuest() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "38009",
            "DEL",
            "DEL"
    });
    assertTrue(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontGuestToAnonymous() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "DEL",
            "-1002",
            "other-than-DEL"
    });
    assertFalse(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestGuestToAnonymous() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "DEL",
            "-1002",
            "other-than-DEL"
    });
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontInvalidGuestAsWellAsAnonymous() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "39002",
            "-1002",
            "other-than-DEL"
    });
    assertFalse(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestInvalidGuestAsWellAsAnonymous() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
            UserSessionServiceImpl.IBM_GENERIC_ACTIVITY_COOKIE_NAME
    }, new String[]{
            "39002",
            "-1002",
            "other-than-DEL"
    });
    assertFalse(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void isLoggedInStoreFrontSuccessfully() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID
    }, new String[]{
            "39002"
    });
    assertTrue(testling.isKnownUser(storeFrontResponse));
    verify(storeFrontResponse).getCookies();
  }

  @Test
  public void isLoggedInServletRequestSuccessfully() {
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID
    }, new String[]{
            "39002"
    });
    assertTrue(testling.isKnownUser(sourceRequest));
    verify(sourceRequest).getCookies();
  }

  @Test
  public void resolveStoreId() {
    assertEquals(STORE_ID, testling.resolveStoreId());
  }

  @Test
  public void resolveCatalogId() {
    assertEquals(CATALOG_ID, testling.resolveCatalogId());
  }

  @Before
  public void setup() throws GeneralSecurityException {
    testling.setStoreFrontConnector(storeFrontConnector);
    testling.setUrlProvider(urlProvider);

    //noinspection unchecked
    when(storeFrontConnector.executeGet(any(String.class), any(Map.class), any(HttpServletRequest.class))).thenReturn(storeFrontResponse);

    CommerceConnection commerceConnection = mock(CommerceConnection.class);

    StoreContext storeContext = mock(StoreContext.class);
    when(storeContext.getStoreId()).thenReturn(STORE_ID);
    when(storeContext.getCatalogId()).thenReturn(Optional.of(CATALOG_ID));
    when(storeContext.getConnection()).thenReturn(commerceConnection);

    CurrentStoreContext.set(storeContext);

    when(storeFrontResponse.getCookies()).thenReturn(Collections.emptyMap());
    initializeCookies(new String[]{
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID,
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID,
    }, new String[]{
            GUEST_OR_LOGGEDIN_USER_ID,
            "DEL"
    });
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }

  private void verifyNoCookiesAtAll() {
    verify(sourceResponse, never()).addCookie(any(javax.servlet.http.Cookie.class));
    verify(sourceResponse, never()).addHeader(any(String.class), any(String.class));
    verify(sourceResponse, never()).setHeader(any(String.class), any(String.class));
  }

  private void initializeCookies(String[] names, String[] values) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    if (names.length > 0) {
      sourceRequestCookies = new javax.servlet.http.Cookie[names.length];
      for (int i = 0; i < names.length; i++) {
        builder.put(names[i], values[i]);

        javax.servlet.http.Cookie servletCookie = mock(javax.servlet.http.Cookie.class);
        when(servletCookie.getName()).thenReturn(names[i]);
        when(servletCookie.getValue()).thenReturn(values[i]);
        sourceRequestCookies[i] = servletCookie;
      }
    }

    when(storeFrontResponse.getCookies()).thenReturn(builder.build());
    when(sourceRequest.getCookies()).thenReturn(sourceRequestCookies);
  }
}
