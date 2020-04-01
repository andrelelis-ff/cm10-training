package com.coremedia.livecontext.ecommerce.ibm.cae.login;

import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.ForVendor;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.catalog.CatalogServiceImpl;
import com.coremedia.livecontext.ecommerce.ibm.link.PreviewTokenService;
import com.coremedia.livecontext.handler.NextURLRedirectHandler;
import com.coremedia.livecontext.web.taglib.LiveContextLoginUrlsProvider;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.objectserver.web.links.TokenResolverHelper;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.coremedia.livecontext.handler.LiveContextPageHandlerBase.isStudioPreviewRequest;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.springframework.web.util.UriUtils.encodeQueryParam;

/**
 * Provides URLs to WCS Login Form and logout handlers.
 */
@ForVendor("ibm")
public class WcsLoginUrlsProvider implements LiveContextLoginUrlsProvider {

  private static final String QUERY_PARAMETER_PREVIEW_TOKEN = "previewToken";

  private final PreviewTokenService previewTokenService;
  private final LinkFormatter linkFormatter;

  private String defaultStoreFrontUrl;
  private String previewStoreFrontUrl;
  private String loginFormUrlTemplate;
  private String logoutUrlTemplate;

  public WcsLoginUrlsProvider(@NonNull PreviewTokenService previewTokenService,
                              @NonNull LinkFormatter linkFormatter) {
    this.previewTokenService = previewTokenService;
    this.linkFormatter = linkFormatter;
  }

  @Required
  public void setDefaultStoreFrontUrl(@NonNull String defaultStoreFrontUrl) {
    this.defaultStoreFrontUrl = requireNonNull(defaultStoreFrontUrl);
  }

  @Required
  public void setPreviewStoreFrontUrl(@NonNull String previewStoreFrontUrl) {
    this.previewStoreFrontUrl = requireNonNull(previewStoreFrontUrl);
  }

  @Required
  public void setLoginFormUrlTemplate(@NonNull String loginFormUrlTemplate) {
    this.loginFormUrlTemplate = requireNonNull(loginFormUrlTemplate);
  }

  @Required
  public void setLogoutUrlTemplate(@NonNull String logoutUrlTemplate) {
    this.logoutUrlTemplate = requireNonNull(logoutUrlTemplate);
  }

  @NonNull
  @Override
  public String buildLoginFormUrl(@NonNull HttpServletRequest request) {
    return buildUrl(loginFormUrlTemplate, request);
  }

  @NonNull
  @Override
  public String buildLogoutUrl(@NonNull HttpServletRequest request) {
    return buildUrl(logoutUrlTemplate, request);
  }

  @NonNull
  @Override
  public String transformLoginStatusUrl(@NonNull String url, @NonNull HttpServletRequest request) {
    if (!isStudioPreviewRequest(request)) {
      return url;
    }
    return appendPreviewToken(url);
  }

  private String buildUrl(String urlTemplate, HttpServletRequest request) {
    StoreContext storeContext = CurrentStoreContext.get();
    CommerceConnection connection = storeContext.getConnection();
    CatalogService catalogService = connection.getCatalogService();

    String commerceTokensReplacedUrl = CommercePropertyHelper.replaceTokens(urlTemplate, storeContext);
    String nexturl = buildNextUrl();
    Map<String, ?> parametersMap = ImmutableMap.of(
            "langId", catalogService instanceof CatalogServiceImpl ?
                    ((CatalogServiceImpl) catalogService).getLanguageId(storeContext.getLocale()) : "-1",
            "storeId", storeContext.getStoreId(),
            "catalogId", storeContext.getCatalogId().get().value(),
            "nexturl", nexturl
    );
    String relativeUrl = TokenResolverHelper.replaceTokens(commerceTokensReplacedUrl, parametersMap, false, false);
    boolean studioPreviewRequest = isStudioPreviewRequest(request);
    String storeFrontUrl = studioPreviewRequest ? previewStoreFrontUrl : defaultStoreFrontUrl;

    String result = concatUrls(storeFrontUrl, relativeUrl);

    return studioPreviewRequest ? appendPreviewToken(result) : result;
  }

  @NonNull
  private String buildNextUrl() {
    ServletRequestAttributes requestAttributes = requestAttributes();
    HttpServletRequest request = requestAttributes.getRequest();
    HttpServletResponse response = requestAttributes.getResponse();
    request.setAttribute(UriConstants.Links.ABSOLUTE_URI_KEY, true);

    // Expect response to be non-`null`.
    @SuppressWarnings({"ConstantConditions", "findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
    String link = linkFormatter.formatLink(NextURLRedirectHandler.LinkTypeNextUrl.NEXTURL, null, request, response,
            false);

    if (link.startsWith("//")) {
      // CMS-15499 - WCS does not like redirect URL without scheme
      link = request.getScheme() + ":" + link;
    }
    return link;
  }

  private static ServletRequestAttributes requestAttributes() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    return (ServletRequestAttributes) requestAttributes;
  }

  @NonNull
  private String appendPreviewToken(@NonNull String url) {
    StoreContext storeContext = CurrentStoreContext.get();

    String previewToken = previewTokenService.getPreviewToken(storeContext);
    if (previewToken == null) {
      return url;
    }

    StringBuilder sb = new StringBuilder(url);
    if (url.indexOf('?') > -1) {
      // parameters already available
      sb.append('&');
    } else {
      // no parameter available yet
      sb.append('?');
    }
    sb.append(QUERY_PARAMETER_PREVIEW_TOKEN);
    sb.append('=');
    sb.append(encodeQueryParam(previewToken, UTF_8));
    return sb.toString();
  }

  private static String concatUrls(String baseUrl, String relativeUrl) {
    if (!baseUrl.endsWith("/")) {
      baseUrl += "/";
    }
    String relativeUrlPart = relativeUrl;
    if (relativeUrlPart.startsWith("/")) {
      relativeUrlPart = relativeUrlPart.substring(1);
    }
    return baseUrl + relativeUrlPart;
  }

}
