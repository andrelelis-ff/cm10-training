package com.coremedia.blueprint.elastic.social.cae.filter;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.multisite.Site;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * A filter that copies the current site from the request to the user's session (if available)
 */
@Named
public class SessionSiteFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // nothing to do here
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      SiteHelper.findSite(request)
              .map(Site::getId)
              .ifPresent(siteId -> getSession((HttpServletRequest) request)
                      .ifPresent(session -> session.setAttribute(SiteHelper.SITE_KEY, siteId)));
    }

    chain.doFilter(request, response);
  }

  @NonNull
  private static Optional<HttpSession> getSession(@NonNull HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return Optional.ofNullable(session);
  }

  @Override
  public void destroy() {
    // nothing to do here
  }
}
