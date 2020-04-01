package com.coremedia.blueprint.cae.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeadersFilter implements Filter {

  private final boolean caeIsPreview;

  public SecurityHeadersFilter(boolean isPreview) {
    this.caeIsPreview = isPreview;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // nothing to do here, just fulfill the Filter interface
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;

      httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
      httpServletResponse.setHeader("X-XSS-Protection", "1; mode=block");

      // the Strict-Transport-Security header should only be set for https requests
      if (request.isSecure()) {
        httpServletResponse.setHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
      }

      // allow the Preview CAE to be rendered in an iframe in the Studio
      if (!caeIsPreview) {
        httpServletResponse.setHeader("X-Frame-Options", "DENY");
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // nothing to do here, just fulfill the Filter interface
  }
}
