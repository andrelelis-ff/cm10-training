package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.NoCommerceConnectionAvailable;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontResponse;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.login.CommerceUserIsLoggedInCacheKey;
import com.coremedia.livecontext.ecommerce.ibm.login.WcLoginWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.user.WcCookieHelper;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserService;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

public class UserSessionServiceImpl extends IbmStoreFrontService implements UserSessionService {

  private static final Logger LOG = LoggerFactory.getLogger(UserSessionServiceImpl.class);

  protected static final String LOGON_URL = "Logon?reLogonURL=fail&storeId={storeId}&catalogId={catalogId}&logonId={logonId}&logonPassword={logonPassword}&URL=TopCategoriesDisplay"; // NOSONAR false positive: Credentials should not be hard-coded
  protected static final String LOGOUT_URL = "Logoff?storeId={storeId}&catalogId={catalogId}";
  protected static final String GUEST_LOGIN_URL = "OrderCreate?storeId={storeId}&catalogId={catalogId}&URL=TopCategoriesDisplay";
  protected static final String GUEST_LOGIN_URL_2 = "MiniShopCartDisplayView?storeId={storeId}&catalogId={catalogId}";

  protected static final String STORE_ID_URL_VAR = "storeId";
  protected static final String CATALOG_ID_URL_VAR = "catalogId";
  protected static final String LOGON_ID_URL_VAR = "logonId";
  protected static final String LOGON_PASSWORD_URL_VAR = "logonPassword"; // NOSONAR false positive: Credentials should not be hard-coded

  private String wcsStorefrontUrl;

  private UserService userService;
  private WcLoginWrapperService loginWrapperService;
  private CommerceCache commerceCache;

  // ----- methods that use WCS storefront services -----------------------------

  @Override
  public boolean ensureGuestIdentity(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    if (isKnownUser(request)) {
      return true;
    }

    try {
      Map<String, String> uriTemplateParameters = ImmutableMap.of(
              STORE_ID_URL_VAR, resolveStoreId(),
              CATALOG_ID_URL_VAR, resolveCatalogId().value());
      StoreFrontResponse storeFrontResponse = handleStorefrontCall(GUEST_LOGIN_URL, uriTemplateParameters, request, response);

      //apply user id update on the user context, other user context values remain untouched: not relevant
      String newUserId = resolveUserId(storeFrontResponse, resolveStoreId(), true);

      UserContext userContext = CurrentUserContext.get();
      String mergedCookies = Cookies.addCookiesToHeaderValue(userContext.getCookieHeader(),
              storeFrontResponse.getCookies());

      StoreContext currentContext = CurrentStoreContext.find().orElse(null);
      if (currentContext != null && WCS_VERSION_7_7.lessThan(StoreContextHelper.getWcsVersion(currentContext))) {
        //bugfix CMS-4132: call guest login url twice because guest session was broken when called only once
        Map<String, String> cookies = storeFrontResponse.getCookies();
        HttpServletRequest requestWrapper = createRequestWrapper(request, cookies);
        StoreFrontResponse storeFrontResponse2 = handleStorefrontCall(GUEST_LOGIN_URL_2, uriTemplateParameters, requestWrapper, response);
        mergedCookies = Cookies.addCookiesToHeaderValue(mergedCookies, storeFrontResponse2.getCookies());
      }

      String newCookieHeader = WcCookieHelper.rewritePreviewCookies(mergedCookies);

      updateUserContext(userContext, newUserId, newCookieHeader);

      //return if the guest upgrade was successful.
      return isKnownUser(storeFrontResponse);
    } catch (Exception e) {
      LOG.error("Error executing guest login for user: {}", e.getMessage(), e);
      return false;
    }
  }

  private static HttpServletRequest createRequestWrapper(HttpServletRequest request, Map<String, String> cookies) {
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getHeader(String name) {
        if ("Cookie".equals(name) && !isEmpty(cookies)) {
          StringBuilder sb = new StringBuilder();
          for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            sb.append(cookie.getKey()).append("=").append(cookie.getValue()).append("; ");
          }
          return sb.toString();
        }
        return super.getHeader(name);
      }
    };
  }

  @Override
  public void pingCommerce(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    try {
      StoreContext storeContext = CurrentStoreContext.get();
      String baseUrl = getWcsStorefrontUrl(storeContext);

      Map<String, Object> params = new HashMap<>();
      params.put(WcsUrlProvider.URL_TEMPLATE, baseUrl);

      String pingUrl = getUrlProvider().provideValue(params, request, storeContext)
              .map(UriComponents::toUriString)
              .orElseThrow(() -> new IllegalStateException("Could not obtain ping URL."));

      handleStorefrontCall(pingUrl, Collections.emptyMap(), request, response);
    } catch (GeneralSecurityException e) {
      LOG.warn("Security exception occurred.", e);
    }
  }

  @Override
  @Nullable
  public String resolveUserId(@NonNull HttpServletRequest request, String currentStoreId, boolean ignoreAnonymous) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    return resolveUserIdFromJavaxCookies(Arrays.asList(cookies), currentStoreId, ignoreAnonymous);
  }

  @Nullable
  private String resolveUserId(@NonNull StoreFrontResponse response, String currentStoreId, boolean ignoreAnonymous) {
    return resolveUserIdFromApacheCookies(response.getCookies(), currentStoreId, ignoreAnonymous);
  }

  @Nullable
  private String resolveUserIdFromJavaxCookies(@NonNull List<Cookie> javaxCookies, String currentStoreId,
                                               boolean ignoreAnonymous) {
    for (Cookie cookie : javaxCookies) {
      String name = cookie.getName();
      String value = cookie.getValue();
      value = Cookies.decodeValue(value);
      if (value == null) {
        continue;
      }

      String userId = resolveUserIdFromCookieData(name, value, currentStoreId, ignoreAnonymous);
      if (userId != null) {
        return userId;
      }
    }
    return null;
  }

  @Nullable
  private String resolveUserIdFromApacheCookies(@NonNull Map<String, String> cookies, String currentStoreId,
                                                boolean ignoreAnonymous) {
    for (Map.Entry<String, String> cookie : cookies.entrySet()) {
      String name = cookie.getKey();
      String value = cookie.getValue();
      value = Cookies.decodeValue(value);
      if (value == null) {
        continue;
      }

      String userId = resolveUserIdFromCookieData(name, value, currentStoreId, ignoreAnonymous);
      if (userId != null) {
        return userId;
      }
    }
    return null;
  }

  @Nullable
  @VisibleForTesting
  String resolveUserIdFromCookieData(@NonNull String cookieName, @NonNull String cookieValue, String currentStoreId,
                                     boolean ignoreAnonymous) {
    if (isUserContainingCookie(cookieName, cookieValue)) {
      String[] tokens = splitCookie(cookieValue, ignoreAnonymous);
      if (tokens.length > 0 && tokens[1].equals(currentStoreId)) {
        return tokens[0];
      }
    }

    return null;
  }

  private boolean isUserContainingCookie(@NonNull String cookieName, @NonNull String cookieValue) {
    boolean isWcUserActivityCookie = cookieName.startsWith(IBM_WC_USERACTIVITY_COOKIE_NAME);
    boolean isPreviewWcUserActivityCookie = cookieName.startsWith(IBM_WCP_USERACTIVITY_COOKIE_NAME);
    boolean isNotDeleted = !cookieValue.contains("DEL");

    return (isWcUserActivityCookie || isPreviewWcUserActivityCookie) && isNotDeleted;
  }

  @NonNull
  private static String[] splitCookie(@NonNull String cookieValue, boolean ignoreAnonymous) {
    String[] values = cookieValue.split(",");
    if (values.length > 1) {
      try {
        int userId = Integer.parseInt(values[0]);
        if (!ignoreAnonymous || userId >= 0) {
          return values;
        }
      } catch (NumberFormatException nfe) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Cannot parse user id from cookie: " + cookieValue, nfe);
        }
      }
    }
    return new String[0];
  }

  @Override
  public boolean loginUser(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, String username,
                           String password) {
    String storeId = resolveStoreId();
    if (storeId == null) {
      return false;
    }

    try {
      Map<String, String> uriTemplateParameters = ImmutableMap.of(
              STORE_ID_URL_VAR, storeId,
              CATALOG_ID_URL_VAR, resolveCatalogId().value(),
              LOGON_ID_URL_VAR, username,
              LOGON_PASSWORD_URL_VAR, password);

      StoreFrontResponse storeFrontResponse = handleStorefrontCall(LOGON_URL, uriTemplateParameters, request, response);

      if (storeFrontResponse.isSuccess()) {
        refreshUserContext(storeFrontResponse);
      }

      return isKnownUser(storeFrontResponse);
    } catch (GeneralSecurityException e) {
      LOG.warn("Error executing login for user '{}': {}", username, e.getMessage());
      LOG.trace("For debugging purpose...", e);
      return false;
    }
  }

  /**
   * A user is assumed to be logged into the commerce system, if he is a {@link #isKnownUser() known}
   * and {@link #isRegisteredUser() registered} user. Unfortunately there is no single REST call that
   * answers the question if a user is logged in, hence we have to combine both methods.
   */
  @Override
  public boolean isLoggedIn() {
    return isKnownUser() && isRegisteredUser();
  }

  /**
   * Returns true if the current user is a known user, which is a registered user or a guest
   */
  private boolean isKnownUser() {
    UserContext userContext = CurrentUserContext.get();

    if (isAnonymousUser(userContext)) {
      return false;
    }

    String userId = userContext.getUserId();

    try {
      StoreContext storeContext = CurrentStoreContext.find()
              .orElseThrow(() -> new NoCommerceConnectionAvailable("You should not have gotten this far."));

      CommerceUserIsLoggedInCacheKey cacheKey = new CommerceUserIsLoggedInCacheKey(userId, storeContext, userContext,
              loginWrapperService, commerceCache);

      return commerceCache.get(cacheKey);
    } catch (Exception e) {
      LOG.debug("error while trying to load the current user context data, assume the user is not logged in", e);
      return false;
    }
  }

  /**
   * Returns whether the current user is a registered user. Be aware that this method returns true
   * whether or not the user is logged in.
   */
  private boolean isRegisteredUser() {
    UserContext userContext = CurrentUserContext.get();

    if (isAnonymousUser(userContext)) {
      return false;
    }

    try {
      User user = userService.findCurrentUser();
      return user != null && isNotBlank(user.getLogonId());
    } catch (Exception e) {
      LOG.error("Unknown error while trying to find a person in commerce. Will return false as answer to isLoggedIn.", e);
      return false;
    }
  }

  @Override
  public boolean logoutUser(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response)
          throws GeneralSecurityException {
    String storeId = resolveStoreId();

    if (storeId == null) {
      return true;
    }

    Map<String, String> uriTemplateParameters = ImmutableMap.of(
            STORE_ID_URL_VAR, storeId,
            CATALOG_ID_URL_VAR, resolveCatalogId().value()
    );
    StoreFrontResponse storeFrontResponse = handleStorefrontCall(LOGOUT_URL, uriTemplateParameters, request, response);
    return !isKnownUser(storeFrontResponse);
  }

  @Override
  public void clearCommerceSession(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    String storeId = CurrentStoreContext.find().map(StoreContext::getStoreId).orElse(null);

    if (storeId == null) {
      LOG.debug("cannot clear commerce session, no commerce context / storeId available");
      return;
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        String name = cookie.getName();
        String value = cookie.getValue();

        if ((name.startsWith(IBM_WC_USERACTIVITY_COOKIE_NAME) || name.startsWith(IBM_WCP_USERACTIVITY_COOKIE_NAME))
                && value != null && value.contains("%2c" + storeId)) {
          cookie.setValue(null);
          cookie.setMaxAge(0);
          cookie.setPath("/");
          response.addCookie(cookie);
        }
      }
    }
  }

  private void refreshUserContext(@NonNull StoreFrontResponse storeFrontResponse) {
    UserContext userContext = CurrentUserContext.get();

    String newUserId = resolveUserId(storeFrontResponse, resolveStoreId(), false);
    String newCookieHeader = Cookies.addCookiesToHeaderValue(userContext.getCookieHeader(),
            storeFrontResponse.getCookies());

    updateUserContext(userContext, newUserId, newCookieHeader);
  }

  private static void updateUserContext(@NonNull UserContext userContext, @Nullable String userId,
                                        @Nullable String cookieHeader) {
    UserContext updatedUserContext = UserContext.buildCopyOf(userContext)
            .withUserId(userId)
            .withCookieHeader(cookieHeader)
            .build();

    CurrentUserContext.set(updatedUserContext);
  }

  private static boolean isAnonymousUser(@NonNull UserContext userContext) {
    return Optional.ofNullable(userContext.getUserId())
            .map(Ints::tryParse)
            .map(userId -> userId < 0)
            .orElse(true);
  }

  @Required
  public void setWcsStorefrontUrl(String wcsStorefrontUrl) {
    this.wcsStorefrontUrl = wcsStorefrontUrl;
  }

  private String getWcsStorefrontUrl(StoreContext storeContext) {
    return CommercePropertyHelper.replaceTokens(wcsStorefrontUrl, storeContext);
  }

  @Required
  public void setLoginWrapperService(WcLoginWrapperService loginWrapperService) {
    this.loginWrapperService = loginWrapperService;
  }

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
