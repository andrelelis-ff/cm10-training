package com.coremedia.livecontext.hybrid;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Sets the domain for all cookies that are added to the
 * {@link #doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain) given response }
 * to a fixed value which can be {@link #setCookieDomain(String[]) configured}.
 *
 * It therefor wraps the given HttpServletResponse that will be handed down the filter chain into a
 * custom class that overrides the {@link javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie) add cookie method}
 *
 * This filter must run before any other code may want to add cookies to the response. Furthermore this filter does not
 * handle container managed cookies like the session cookie for example!
 */
public class CookieLevelerFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

  private String[] cookieDomains;

  @Override
  public void init(FilterConfig filterConfig) {
    LOGGER.info("Configured domains: {}", ArrayUtils.toString(cookieDomains));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    if (!(response instanceof HttpServletResponse) || !(request instanceof HttpServletRequest)) {
      chain.doFilter(request, response);
      return;
    }

    if (isEmpty(cookieDomains)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest servletRequest = (HttpServletRequest) request;
    HttpServletResponse servletResponse = (HttpServletResponse) response;

    String host = servletRequest.getServerName();

    for (String oneRegisteredDomain: cookieDomains) {
      if(host.endsWith(oneRegisteredDomain)) {
        chain.doFilter(request, new HttpServletResponseCookieAware(servletResponse, oneRegisteredDomain));
        return;
      }
    }
    LOGGER.debug("Request host {} is not configured for rewrite cookies to it.", host);
    chain.doFilter(request, response);
  }

  @Value("${livecontext.cookie.domain}")
  public void setCookieDomain(String[] cookieDomain) {
    this.cookieDomains = cookieDomain;
  }

  protected static class HttpServletResponseCookieAware extends SaveContextOnUpdateOrErrorResponseWrapper {
    private final String cookieDomain;

    HttpServletResponseCookieAware(HttpServletResponse response, String cookieDomain) {
      super(response, false);
      this.cookieDomain = cookieDomain;
    }

    @Override
    protected void saveContext(SecurityContext context) {
    }

    @Override
    public void addCookie(Cookie cookie) {
      cookie.setDomain(cookieDomain);
      super.addCookie(cookie);
    }
  }
}
