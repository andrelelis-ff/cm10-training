package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontConnector;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontResponse;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.ibm.login.WcLoginWrapperService;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserService;
import com.google.common.collect.ImmutableMap;
import org.apache.http.cookie.Cookie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserSessionServiceImplTest {

  private static final String USERNAME = "zaphod";
  private static final String USERID = "42";
  private static final String PASSWORD = "trish";
  private static final String STOREFRONT_SECURE_URL = "Doors";
  private static final String GUEST_OR_LOGGEDIN_USER_ID = "38009";
  private static final String ANONYMOUS_USER_ID = "-1002";

  @Mock
  private StoreFrontConnector storeFrontConnector;

  @Mock
  private StoreFrontResponse storeFrontResponse;

  @Mock
  private User anonymousUser;

  @Mock
  private User registeredUser;

  @Mock
  private Cookie userActivityCookie;

  @Mock
  private HttpServletRequest sourceRequest;

  @Mock
  private HttpServletResponse sourceResponse;

  @Mock
  private WcLoginWrapperService wcLoginWrapperService;

  @Mock
  private WcsUrlProvider urlProvider;

  private StoreContextImpl storeContext;

  @Mock
  private UserService userService;

  @InjectMocks
  private UserSessionServiceImpl testling;

  @SuppressWarnings("unchecked")
  @Before
  public void defaultSetup() throws GeneralSecurityException {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);

    storeContext = IbmStoreContextBuilder
            .from(commerceConnection, "any-site-id")
            .withStoreId("10001")
            .withCatalogId(CatalogId.of("catalog"))
            .build();

    when(commerceConnection.getUserService()).thenReturn(Optional.of(userService));

    CurrentStoreContext.set(storeContext);

    UserContext userContext = UserContext.builder().withUserId(USERID).build();
    CurrentUserContext.set(userContext);

    testling.setUserService(userService);

    CommerceCache commerceCache = new CommerceCache();
    commerceCache.setEnabled(false);
    commerceCache.setCacheTimesInSeconds(emptyMap());
    testling.setCommerceCache(commerceCache);

    when(storeFrontConnector.executeGet(contains("Logon"), any(Map.class), any(HttpServletRequest.class))).thenReturn(storeFrontResponse);
    when(storeFrontConnector.executeGet(contains("Logoff"), any(Map.class), any(HttpServletRequest.class))).thenReturn(storeFrontResponse);

    when(userActivityCookie.getName()).thenReturn(UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID);
    when(userActivityCookie.getValue()).thenReturn("irrelevant");

    when(anonymousUser.getLogonId()).thenReturn(null);
    when(registeredUser.getLogonId()).thenReturn("yes");

    when(storeFrontResponse.getCookies()).thenReturn(emptyMap());
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
    CurrentUserContext.remove();
  }

  @Test
  public void loginUserNoStoreId() {
    StoreContextImpl storeContextWithoutStoreId = IbmStoreContextBuilder
            .from(storeContext)
            .withStoreId(null)
            .build();
    CurrentStoreContext.set(storeContextWithoutStoreId);

    assertFalse(testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD));
    verifyNoCookiesAtAll();
  }

  @SuppressWarnings("unchecked")
  @Test(expected = AuthenticationServiceException.class)
  public void loginUserAuthenticationException() throws GeneralSecurityException {
    when(storeFrontConnector.executeGet(any(String.class), any(Map.class), any(HttpServletRequest.class))).thenThrow(AuthenticationServiceException.class);
    try {
      testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD);
    } finally {
      verifyNoCookiesAtAll();
    }
  }

  @Test
  public void loginUserNoCookiesFromWCS() throws GeneralSecurityException {
    when(urlProvider.provideValue(anyMap(), any(), any()))
            .thenReturn(Optional.of(UriComponentsBuilder.fromUriString(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL).build()));
    boolean loggedIn = testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD);

    assertFalse(loggedIn);
    verify(storeFrontConnector).executeGet(eq(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL), any(Map.class), eq(sourceRequest));
    assertNull(CurrentUserContext.get().getCookieHeader());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void loginUserGenericSecurityException() throws GeneralSecurityException {
    when(storeFrontConnector.executeGet(any(String.class), any(Map.class), any(HttpServletRequest.class))).thenThrow(GeneralSecurityException.class);
    assertFalse(testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD));
    verifyNoCookiesAtAll();
  }

  @Test
  public void loginUserIrrelevantCookies() throws GeneralSecurityException {
    when(urlProvider.provideValue(anyMap(), any(), any()))
            .thenReturn(Optional.of(UriComponentsBuilder.fromUriString(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL).build()));
    when(storeFrontResponse.isSuccess()).thenReturn(true);
    when(storeFrontResponse.getCookies()).thenReturn(ImmutableMap.of("Happy-Vertical-People-Transporter", "42", "Matter-transference-beams", "irrelevant"));

    boolean loggedIn = testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD);
    assertFalse(loggedIn);
    verify(storeFrontConnector).executeGet(eq(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL), any(Map.class), eq(sourceRequest));
    assertEquals("Happy-Vertical-People-Transporter=42; Matter-transference-beams=irrelevant", CurrentUserContext.get().getCookieHeader());
  }

  @Test
  public void loginUserSuccessfully() throws GeneralSecurityException {
    when(urlProvider.provideValue(anyMap(), any(), any()))
            .thenReturn(Optional.of(UriComponentsBuilder.fromUriString(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL).build()));
    when(storeFrontResponse.isSuccess()).thenReturn(true);
    when(storeFrontResponse.getCookies()).thenReturn(ImmutableMap.of(UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID, "42", "Matter-transference-beams", "irrelevant"));

    boolean loggedIn = testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD);
    assertTrue(loggedIn);
    verify(storeFrontConnector).executeGet(eq(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL), any(Map.class), eq(sourceRequest));
    assertEquals("WC_USERACTIVITY_38009=42; Matter-transference-beams=irrelevant", CurrentUserContext.get().getCookieHeader());
  }

  @Test
  public void loginUserSuccessfullyWithDeletedAnonymousCookie() throws GeneralSecurityException {
    when(storeFrontResponse.isSuccess()).thenReturn(true);
    when(storeFrontResponse.getCookies()).thenReturn(ImmutableMap.of(
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID, "42",
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID, "DEL",
            "Matter-transference-beams", "irrelevant"));
    when(urlProvider.provideValue(anyMap(), any(), any()))
            .thenReturn(Optional.of(UriComponentsBuilder.fromUriString(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL).build()));

    boolean loggedIn = testling.loginUser(sourceRequest, sourceResponse, USERNAME, PASSWORD);
    assertTrue(loggedIn);
    verify(storeFrontConnector).executeGet(eq(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGON_URL), any(Map.class), eq(sourceRequest));
    assertEquals("WC_USERACTIVITY_38009=42; WC_USERACTIVITY_-1002=DEL; Matter-transference-beams=irrelevant", CurrentUserContext.get().getCookieHeader());
  }

  @Test
  public void logoutUserNoStoreId() throws GeneralSecurityException {
    when(urlProvider.provideValue(anyMap(), any(), any()))
            .thenReturn(Optional.of(UriComponentsBuilder.fromUriString(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGOUT_URL).build()));

    StoreContextImpl storeContextWithoutStoreId = IbmStoreContextBuilder
            .from(storeContext)
            .withStoreId(null)
            .build();
    CurrentStoreContext.set(storeContextWithoutStoreId);

    assertTrue(testling.logoutUser(sourceRequest, sourceResponse));
    verifyNoCookiesAtAll();
  }

  @Test
  public void logoutUserSuccessfully() throws GeneralSecurityException {
    when(urlProvider.provideValue(anyMap(), any(), any()))
            .thenReturn(Optional.of(UriComponentsBuilder.fromUriString(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGOUT_URL).build()));
    when(storeFrontResponse.isSuccess()).thenReturn(true);
    when(storeFrontResponse.getCookies()).thenReturn(ImmutableMap.of(
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + GUEST_OR_LOGGEDIN_USER_ID, "DEL",
            UserSessionServiceImpl.IBM_WC_USERACTIVITY_COOKIE_NAME + ANONYMOUS_USER_ID, ANONYMOUS_USER_ID));

    boolean loggedOut = testling.logoutUser(sourceRequest, sourceResponse);
    assertTrue(loggedOut);
    verify(storeFrontConnector).executeGet(eq(STOREFRONT_SECURE_URL + UserSessionServiceImpl.LOGOUT_URL), any(Map.class), eq(sourceRequest));
    assertNull(CurrentUserContext.get().getCookieHeader());
  }

  @Test
  public void isLoggedInNoPersonAtAll() {
    when(wcLoginWrapperService.isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class))).thenReturn(false);

    assertFalse(testling.isLoggedIn());
    verify(wcLoginWrapperService).isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class));
  }

  @Test
  public void isLoggedInException() {
    when(wcLoginWrapperService.isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class))).thenThrow(new CommerceException(""));

    assertFalse(testling.isLoggedIn());
    verify(wcLoginWrapperService).isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class));
  }

  @Test
  public void isLoggedInUserUnknown() {
    when(wcLoginWrapperService.isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class))).thenReturn(true);
    when(userService.findCurrentUser()).thenReturn(null);

    assertFalse(testling.isLoggedIn());
  }

  @Test
  public void isLoggedInSuccessfully() {
    when(wcLoginWrapperService.isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class))).thenReturn(true);

    User user = mock(User.class);
    when(user.getUserId()).thenReturn(USERID);
    when(user.getLogonId()).thenReturn(USERNAME);
    when(userService.findCurrentUser()).thenReturn(user);

    assertTrue(testling.isLoggedIn());
    verify(wcLoginWrapperService).isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class));
  }

  @Test
  public void isAnonymousUser() {
    UserContext userContext = UserContext.builder().withUserId("").withUserName("").build();
    CurrentUserContext.set(userContext);

    assertFalse(testling.isLoggedIn());
    verify(wcLoginWrapperService, times(0)).isLoggedIn(anyString(), any(StoreContext.class), any(UserContext.class));
  }

  private void verifyNoCookiesAtAll() {
    verify(sourceResponse, never()).addHeader(any(String.class), any(String.class));
    verify(sourceResponse, never()).setHeader(any(String.class), any(String.class));
  }
}
