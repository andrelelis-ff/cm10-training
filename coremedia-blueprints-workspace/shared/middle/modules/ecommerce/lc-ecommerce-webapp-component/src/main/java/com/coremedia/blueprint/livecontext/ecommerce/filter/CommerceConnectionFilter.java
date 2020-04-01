package com.coremedia.blueprint.livecontext.ecommerce.filter;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Initializes the commerce connection for the requests site (if any).
 *
 * @see SiteHelper
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class CommerceConnectionFilter implements Filter {

  private final CommerceConnectionInitializer commerceConnectionInitializer;

  CommerceConnectionFilter(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  @Override
  public void init(FilterConfig filterConfig) {
    // Do nothing.
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    try {
      SiteHelper.findSite(request)
              .flatMap(commerceConnectionInitializer::findConnectionForSite)
              .map(CommerceConnection::getStoreContext)
              .ifPresent(CurrentStoreContext::set);
      chain.doFilter(request, response);
    } finally {
      CurrentStoreContext.remove();
    }
  }

  @Override
  public void destroy() {
    // Do nothing.
  }
}
