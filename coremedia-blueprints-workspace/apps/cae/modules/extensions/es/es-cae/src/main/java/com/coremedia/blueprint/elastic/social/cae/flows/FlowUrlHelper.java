package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.webflow.execution.RequestContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;

@Named("webflowUrlHelper")
public class FlowUrlHelper {

  @Inject
  private LinkFormatter linkFormatter;

  @Value("${keep.https.after.logout}")
  private boolean keepHttpsAfterLogout;

  public String getNextUrl(String nextParameter, RequestContext context) {
    return getNextUrl(nextParameter, context, false);
  }

  public String getNextUrl(String nextParameter, RequestContext context, boolean forceAbsolute) {
    if (nextParameter != null && !nextParameter.isEmpty()) {
      return prependScheme(nextParameter, context, forceAbsolute);
    }
    HttpServletRequest request = getRequest(context);
    if(request.getParameterMap().containsKey("nextUrl")) {
      return prependScheme(request.getParameter("nextUrl"), context, forceAbsolute);
    }
    return getRootPageUrl(context, forceAbsolute);
  }

  /**
   * Return a page to land after logout. Force the logout URL scheme to be http.
   */
  public String getLogoutUrl(RequestContext context) {
    HttpServletRequest request = getRequest(context);
    Object oldAbsoluteAttrValue = request.getAttribute(ABSOLUTE_URI_KEY);
    try {
      request.setAttribute(ABSOLUTE_URI_KEY, true);
      String rootPageUrl = getRootPageUrl(context);
      if(!keepHttpsAfterLogout) {
        return forceHttpScheme(rootPageUrl);
      }
      return rootPageUrl;
    } finally {
      request.setAttribute(ABSOLUTE_URI_KEY, oldAbsoluteAttrValue);
    }
  }

  public String getRootPageUrl(RequestContext context) {
    return getRootPageUrl(context, false);
  }

  public String getRootPageUrl(RequestContext context, boolean forceAbsolute) {
    Page page = RequestAttributeConstants.getPage(getRequest(context));
    if (page != null) {
      Navigation navigation = page.getNavigation();
      String url = getUrl(navigation.getRootNavigation(), null, context);
      return prependScheme(url, context, forceAbsolute);
    }
    return null;
  }

  /**
   * If the given url contains a host name but no scheme, Prepend the request scheme to the URI.
   */
  private String prependScheme(String url, RequestContext context, boolean forceAbsolute) {
    if (url.startsWith("//") || (forceAbsolute && !url.startsWith("http"))) {
      HttpServletRequest request = getRequest(context);
      StringBuffer absoluteUrl = request.getRequestURL();

      if (url.startsWith("//")) {
        absoluteUrl.setLength(absoluteUrl.indexOf("//"));
      } else {
        absoluteUrl.setLength(absoluteUrl.indexOf("/", absoluteUrl.indexOf("//") + 2));
      }
      absoluteUrl.append(url);
      return absoluteUrl.toString();
    }

    return url;
  }

  /**
   * Set the scheme of the given url to http if it is https.
   */
  private String forceHttpScheme(String url) {
    if (url.startsWith("https:")) {
      return url.replaceFirst("https:", "http:");
    }
    return url;
  }

  public String getUrl(Object bean, String view, RequestContext context) {
    return getUrl(bean, view, getRequest(context), getResponse(context));
  }

  private String getUrl(Object bean, String view, @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response) {
    return linkFormatter.formatLink(bean, view, request, response, true);
  }

  private static HttpServletResponse getResponse(RequestContext context) {
    return (HttpServletResponse) context.getExternalContext().getNativeResponse();
  }

  private static HttpServletRequest getRequest(RequestContext context) {
    return (HttpServletRequest) context.getExternalContext().getNativeRequest();
  }
}
