package com.coremedia.ecommerce.studio.rest.filter;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inject site into request context.
 * <p>
 * This should make the commerce connection (injected by a follow-up filter) available in Studio.
 */
public class SiteFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(SiteFilter.class);

  private static final Pattern SITE_ID_URL_PATTERN = Pattern.compile(".*?/livecontext/.+?/(?<siteId>.+?)((/.*)|$)");

  @Inject
  @SuppressWarnings("squid:S3306") //squid:S3306 Constructor injection should be used instead of field injection
  private SitesService sitesService;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // not needed
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    findSite(request).ifPresent(site -> SiteHelper.setSiteToRequest(site, request));

    chain.doFilter(request, response);
  }

  @NonNull
  private Optional<Site> findSite(@NonNull ServletRequest request) {
    return findPathInfo(request)
            .flatMap(SiteFilter::extractSiteId)
            .flatMap(this::findSiteById);
  }

  @NonNull
  private static Optional<String> findPathInfo(@NonNull ServletRequest request) {
    if (!(request instanceof HttpServletRequest)) {
      return Optional.empty();
    }

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;

    String pathInfo = httpServletRequest.getPathInfo();
    if (StringUtils.isEmpty(pathInfo)) {
      pathInfo = httpServletRequest.getServletPath();
    }

    return Optional.ofNullable(pathInfo);
  }

  @NonNull
  @VisibleForTesting
  static Optional<String> extractSiteId(@NonNull CharSequence pathInfo) {
    Matcher matcher = SITE_ID_URL_PATTERN.matcher(pathInfo);

    if (!matcher.matches()) {
      return Optional.empty();
    }

    String siteId = matcher.group("siteId");

    if (siteId == null) {
      LOG.debug("Unable to extract site ID from URL path info '{}'.", pathInfo);
    }

    return Optional.ofNullable(siteId);
  }

  @NonNull
  private Optional<Site> findSiteById(@NonNull String siteId) {
    Optional<Site> site = sitesService.findSite(siteId);

    if (!site.isPresent()) {
      LOG.debug("Unknown site ID '{}'.", siteId);
    }

    return site;
  }

  @Override
  public void destroy() {
    // not needed
  }
}
