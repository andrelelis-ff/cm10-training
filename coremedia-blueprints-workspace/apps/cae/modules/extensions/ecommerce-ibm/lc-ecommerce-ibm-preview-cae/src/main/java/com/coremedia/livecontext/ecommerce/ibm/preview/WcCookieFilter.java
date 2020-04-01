package com.coremedia.livecontext.ecommerce.ibm.preview;

import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Filter WCP_ Cookies from production requests and filter WC_ Cookies from preview requests.
 */
public class WcCookieFilter implements Filter {
  public static final String COOKIE_PREFIX_PREVIEW = "WCP_";
  public static final String COOKIE_PREFIX_DEFAULT = "WC_";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequestWrapper requestWrapper;
    if (Boolean.valueOf(request.getAttribute(LiveContextPageHandlerBase.HAS_PREVIEW_TOKEN) + "")) {
      requestWrapper = new PrefixBasedCookieFilteringRequestWrapper((HttpServletRequest) request, COOKIE_PREFIX_DEFAULT);
    } else {
      requestWrapper = new PrefixBasedCookieFilteringRequestWrapper((HttpServletRequest) request, COOKIE_PREFIX_PREVIEW);
    }
    chain.doFilter(requestWrapper, response);
  }

  @Override
  public void destroy() {
  }

  /**
   * RequestWrapper to filter cookies based on the prefix of the cookie name
   */
  private static class PrefixBasedCookieFilteringRequestWrapper extends HttpServletRequestWrapper {
    private static final String REPLACEMENT = "NOT_APPLICABLE_HERE_";
    String filterPrefix;

    public PrefixBasedCookieFilteringRequestWrapper(HttpServletRequest request, String filter) {
      super(request);
      filterPrefix = filter;
    }

    @Override
    public String getHeader(String name) {
      if ("Cookie".equals(name)) {
        String cookieHeader = super.getHeader(name);
        cookieHeader = cookieHeader != null ? cookieHeader.replaceAll(filterPrefix, REPLACEMENT) : null;
        return cookieHeader;
      }
      return super.getHeader(name);
    }

    @Override
    public Cookie[] getCookies() {
      ArrayList<Cookie> result = new ArrayList<>();
      Cookie[] cookies = super.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if (!cookie.getName().startsWith(filterPrefix)) {
            result.add(cookie);
          }
        }
      }
      return result.toArray(new Cookie[result.size()]);
    }
  }
}
