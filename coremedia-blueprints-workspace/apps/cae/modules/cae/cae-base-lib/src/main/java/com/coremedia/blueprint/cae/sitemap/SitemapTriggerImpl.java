package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.links.BlueprintUriConstants;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

public class SitemapTriggerImpl implements SitemapTrigger, ServletContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(SitemapTriggerImpl.class);

  private SitemapHelper sitemapHelper;
  private UrlPathFormattingHelper urlPathFormattingHelper;
  private SitesService sitesService;

  private static final String LOCALHOST = "localhost";
  private int myOwnPort = 49080;
  private ServletContext servletContext;

  public void generateSitemaps() {
    Set<Site> sites = sitesService.getSites();
    if (sites.isEmpty()) {
      // This sometimes happens on test machines when the first
      // sitemap generation starts before the example content has been imported.
      LOG.info("Sitemap generation found no sites at all.  Check your content repository and your SitesService configuration.");
    }

    for (Site site : sites) {
      if (sitemapHelper.isSitemapEnabled(site)) {
        try {
          LOG.info("Start sitemap generation for {}", site);
          String result = generateSitemap(site);
          LOG.info("Finished sitemap generation for {}: {}", site, result);
        } catch (Exception e) {
          LOG.warn("Sitemap generation for {} failed.", site, e);
        }
      } else {
        LOG.info("{} has no sitemap configuration.", site);
      }
    }
  }

  /*
   * Unfortunately we cannot simply use SitemapGenerationController#createUrls
   * here, because link building depends on features provided by interceptors
   * in request scope only (at least for the livecontext extension).  So we
   * must trigger sitemap creation by a http roundtrip to ourselves.
   */
  /**
   * Trigger generation for one site
   *
   * @param site site to generate the sitemap for
   * @return the result of the generation - the sitemap for this site
   * @throws IOException
   */
  @VisibleForTesting
  String generateSitemap(Site site) throws IOException {
    HttpGet httpGet = new HttpGet(sitemapGenerationUrl(urlSegment(site)));
    try(CloseableHttpClient httpclient = createHttpClient();
        CloseableHttpResponse response = httpclient.execute(httpGet)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpServletResponse.SC_OK) {
        return IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
      } else {
        throw new IllegalStateException("Unable to generate sitemap for " + site + " (" + statusCode + ")");
      }
    }
  }

  @VisibleForTesting
  CloseableHttpClient createHttpClient() {
    return HttpClients.createDefault();
  }

  private String urlSegment(Site site) {
    Content rootChannel = site.getSiteRootDocument();
    String segment = rootChannel==null ? null : urlPathFormattingHelper.getVanityName(rootChannel);
    if (segment==null) {
      throw new IllegalArgumentException(site + " has no segment.");
    }
    return segment;
  }

  // http://localhost:${myOwnPort}/${contextPath}/servlet/internal/${segment}/sitemap-org
  private URI sitemapGenerationUrl(String segment) {
    UriComponentsBuilder ucb = UriComponentsBuilder.newInstance();
    ucb.scheme("http");
    ucb.host(LOCALHOST);
    ucb.port(myOwnPort);
    ucb.pathSegment(resolveContextPath(), "servlet", BlueprintUriConstants.Prefixes.PREFIX_INTERNAL, segment, SitemapHelper.SITEMAP_ORG);
    return ucb.build().toUri();
  }

  private String resolveContextPath() {
    String contextPath = servletContext.getContextPath();
    if (contextPath != null && contextPath.startsWith("/")) {
      return contextPath.substring(1);
    }
    return contextPath;
  }

  // --- configuration ----------------------------------------------

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setSitemapHelper(SitemapHelper sitemapHelper) {
    this.sitemapHelper = sitemapHelper;
  }

  @Required
  public void setUrlPathFormattingHelper(UrlPathFormattingHelper urlPathFormattingHelper) {
    this.urlPathFormattingHelper = urlPathFormattingHelper;
  }

  /**
   * Defaults to 49080, must be the port of this particular servlet container.
   * No proxy, because we run "/internal" requests which are blocked by proxies.
   */
  public void setMyOwnPort(String myOwnPort) {
    this.myOwnPort = Integer.parseInt(myOwnPort);
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
