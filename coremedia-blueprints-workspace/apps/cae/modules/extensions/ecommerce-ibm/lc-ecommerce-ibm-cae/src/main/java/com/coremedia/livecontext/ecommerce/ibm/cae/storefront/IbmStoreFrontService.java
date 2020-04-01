package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontConnector;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontResponse;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The store front service is used for synchronizing sessions between the CAE and the commerce system. Sessions
 * in the commerce system are represented by cookies, which the CAE needs to pass through to the browser. This is
 * necessary to allow a user to request pages from the CAE as well as from the commerce system while both
 * systems share the same set of commerce cookies.
 */
public abstract class IbmStoreFrontService extends StoreFrontService {

  /**
   * The prefix of the cookie that commerce uses to track activities of users. Users are
   * anonymous, registered and guest users. This cookie flows both across http and https.
   * While anonymous user are identified by negative userId, known users (guest or registered) have positive
   * user ids. The user Id can be read from the cookie name or from its value.
   */
  public static final String IBM_WC_USERACTIVITY_COOKIE_NAME = "WC_USERACTIVITY_";
  public static final String IBM_WCP_USERACTIVITY_COOKIE_NAME = "WCP_USERACTIVITY_";

  /**
   * The prefix of the cookie that commerce uses for anonymous users only. It is not present for known users.
   */
  public static final String IBM_GENERIC_ACTIVITY_COOKIE_NAME = "WC_GENERIC_ACTIVITYDATA";

  private static final String REGEXP_POSITIVE_NUMBER = "\\d+";

  private static final Logger LOG = LoggerFactory.getLogger(IbmStoreFrontService.class);

  private static final String PREVIEW_TOKEN = "previewToken";

  private WcsUrlProvider urlProvider;

  /**
   * Calls the store front via the {@link StoreFrontConnector storefront connector} and copies all
   * <code>Set-Cookie</code> headers from the {@link StoreFrontResponse store front response} to the source response,
   * which is the original cae response that will be sent back to the users browsers.
   *
   * @param uri                   The path to the requested store front call. The URI is expanded by the
   *                              {@link WcsUrlProvider} and may contain placeholders, which the store front connector
   *                              will replace with value given by the uriTemplateParameters.
   * @param uriTemplateParameters a map of key value pairs that are used to replace the placeholder within the given
   *                              URI.
   * @param sourceRequest         the original CAE request, that led to this store front request. Any cookie that was
   *                              sent to the CAE wil be copied to the store front request.
   * @param sourceResponse        the response that will be sent back to the browser. Any cookie that the store front
   *                              put into its response must be copied to the sourceResponse.
   * @return the {@link StoreFrontResponse store front response}
   */
  @Override
  public StoreFrontResponse handleStorefrontCall(
          @NonNull String uri,
          @NonNull Map<String, String> uriTemplateParameters,
          @NonNull HttpServletRequest sourceRequest,
          @NonNull HttpServletResponse sourceResponse) throws GeneralSecurityException {
    String fullQualifiedUrl = prepareStoreFrontUrl(uri, sourceRequest);
    return super.handleStorefrontCall(fullQualifiedUrl, uriTemplateParameters, sourceRequest, sourceResponse);
  }

  @NonNull
  private String prepareStoreFrontUrl(@NonNull String uri, @NonNull HttpServletRequest sourceRequest) {
    String fullQualifiedUrl = makeAbsolute(uri, !isEmpty(sourceRequest.getParameter(PREVIEW_TOKEN)), sourceRequest);
    UriComponentsBuilder ucb = UriComponentsBuilder.fromUriString(fullQualifiedUrl);
    if (sourceRequest.getParameterMap().containsKey(PREVIEW_TOKEN)) {
      ucb.queryParam(PREVIEW_TOKEN, sourceRequest.getParameter(PREVIEW_TOKEN)).build().toUriString();
    }
    return ucb.build().toUriString();
  }

  private String makeAbsolute(@NonNull String url, boolean isPreview, HttpServletRequest request) {
    if (isAbsolute(url)) {
      return url;
    }

    String fullQualifiedUrl = url;

    Map<String, Object> params = new HashMap<>();
    params.put(WcsUrlProvider.URL_TEMPLATE, url);
    params.put(WcsUrlProvider.IS_STUDIO_PREVIEW, isPreview);
    params.put(WcsUrlProvider.IS_INITIAL_STUDIO_REQUEST, false);

    UriComponents uc = urlProvider.provideValue(params, request, getStoreContext()).orElse(null);
    if (uc != null) {
      fullQualifiedUrl = uc.toUriString();
    }
    if (isProtocolRelative(fullQualifiedUrl)) {
      fullQualifiedUrl = "https:" + fullQualifiedUrl;
    }

    return fullQualifiedUrl;
  }

  private static boolean isAbsolute(@NonNull String uri) {
    try {
      return new URI(uri).isAbsolute();
    } catch (URISyntaxException e) {
      LOG.trace("Cannot parse uri: {}", uri, e);
      return false;
    }
  }

  private static boolean isProtocolRelative(@NonNull String uri) {
    return uri.startsWith("//");
  }

  /**
   * One of two methods, that interprets the commerce cookies to decide whether the current user is known in
   * commerce or not.
   * <p>
   * This one is used for store front calls, while the
   * {@link #isKnownUser(javax.servlet.http.HttpServletRequest) other one} is used for CAE requests.
   * <p>
   * A known user is either a guest or a registered user. We cannot distinguish them by looking at the cookies.
   * That means: We do not know whether a user is logged in or if he is just a guest only by looking at the cookies.
   *
   * @param storeFrontResponse the {@link StoreFrontResponse storefront response}
   *                           that was returned from the store front with a set of cookies.
   * @return <code>true</code> if the cookies represent a logged in user <code>false</code> otherwise.
   */
  protected boolean isKnownUser(@NonNull StoreFrontResponse storeFrontResponse) {
    boolean isGuestOrRegistered = false;
    boolean isAnonymous = false;

    Map<String, String> cookies = storeFrontResponse.getCookies();
    for (Map.Entry<String, String> cookie : cookies.entrySet()) {
      String name = cookie.getKey();
      String value = cookie.getValue();
      if (!isGuestOrRegistered) {
        isGuestOrRegistered = isGuestOrRegisteredUser(name, value);
      }
      if (!isAnonymous) {
        isAnonymous = isAnonymousUser(name, value);
      }
    }

    return isGuestOrRegistered && !isAnonymous;
  }

  /**
   * One of two methods, that interprets the commerce cookies to decide whether the current user is known in
   * commerce or not.
   * <p>
   * This one is used for CAE requests while {@link #isKnownUser(StoreFrontResponse) the other one} is used for store
   * front requests.
   * <p>
   * A known user is either a guest or a registered user. We cannot distinguish them by looking at the cookies. That
   * means: We do not know whether a user has registered and logged in or if he is just a guest only by looking at the
   * cookies.
   *
   * @param request the request, which holds the cookies
   * @return <code>true</code> if the cookies represent a logged in user <code>false</code> otherwise.
   */
  public boolean isKnownUser(@NonNull HttpServletRequest request) {
    boolean isGuestOrRegistered = false;
    boolean isAnonymous = false;

    javax.servlet.http.Cookie[] clientCookies = request.getCookies();
    if (clientCookies != null) {
      for (javax.servlet.http.Cookie oneCookie : clientCookies) {
        String name = oneCookie.getName();
        String value = oneCookie.getValue();
        if (!isGuestOrRegistered) {
          isGuestOrRegistered = isGuestOrRegisteredUser(name, value);
        }
        if (!isAnonymous) {
          isAnonymous = isAnonymousUser(name, value);
        }
      }
    }

    return isGuestOrRegistered && !isAnonymous;
  }

  protected String resolveStoreId() {
    return getStoreContext().getStoreId();
  }

  @NonNull
  protected CatalogId resolveCatalogId() {
    return getStoreContext().getCatalogId().get();
  }

  @NonNull
  private static StoreContext getStoreContext() {
    return CurrentStoreContext.get();
  }

  private static boolean isAnonymousUser(@NonNull String cookieName, @Nullable String cookieValue) {
    return cookieName.startsWith(IBM_GENERIC_ACTIVITY_COOKIE_NAME) && isValid(cookieValue, null);
  }

  protected boolean isGuestOrRegisteredUser(String cookieName, String cookieValue) {
    return (cookieName.startsWith(IBM_WC_USERACTIVITY_COOKIE_NAME)
            || cookieName.startsWith(IBM_WCP_USERACTIVITY_COOKIE_NAME))
            && isValid(cookieValue, REGEXP_POSITIVE_NUMBER);
  }

  private static boolean isValid(@Nullable String cookieValue, @Nullable String validate) {
    if (isBlank(cookieValue)) {
      return false;
    }

    String[] fields = cookieValue.split("%2c"); //delimiter for fep7
    if (fields.length == 1) {
      fields = cookieValue.split("%2C"); //delimiter for fep7
    }
    // special value that IBM uses to mark a cookie which must be deleted by the browser.
    // The browser actually uses the expiry date of such a cookie to delete it, which is set to something
    // in the past. Although this case could be removed as it is evaluated by the following
    // regexp as well, it is here for documentation purposes.
    if ("DEL".equals(fields[0])) {
      return false;
    }

    //noinspection SimplifiableIfStatement
    if (validate != null) {
      return fields[0].matches(validate);
    }

    return true;
  }

  @Required
  public void setUrlProvider(WcsUrlProvider urlProvider) {
    this.urlProvider = urlProvider;
  }

  public WcsUrlProvider getUrlProvider() {
    return urlProvider;
  }
}
