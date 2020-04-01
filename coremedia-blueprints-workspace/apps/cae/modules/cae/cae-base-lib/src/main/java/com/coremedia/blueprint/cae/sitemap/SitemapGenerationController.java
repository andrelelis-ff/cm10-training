package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.blueprint.cae.web.IllegalRequestException;
import com.coremedia.cap.multisite.Site;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Creates lists of URLs, e.g. for sitemaps or performance tests.
 * <p/>
 * This controller accepts the following request parameters:
 * <p/>
 * <ul>
 * <li>excludeFolders: A comma separated list of folder names inside the content repository to exclude. The
 * folder name must not contain slashes at the beginning or at the end.</li>
 * </ul>
 */
public class SitemapGenerationController {
  private static final Logger LOG = LoggerFactory.getLogger(SitemapGenerationController.class);

  private SiteResolver siteResolver;
  private SitemapSetupFactory sitemapSetupFactory;

  // --- configuration ----------------------------------------------

  @Required
  public void setSiteResolver(SiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  @Required
  public void setSitemapSetupFactory(SitemapSetupFactory sitemapSetupFactory) {
    this.sitemapSetupFactory = sitemapSetupFactory;
  }

  // --- features ---------------------------------------------------

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
    try {
      Site site = siteByRequest(request);
      SitemapSetup sitemapConfiguration = selectConfiguration(site);
      response.setContentType(sitemapConfiguration.getSitemapRendererFactory().getContentType());
      response.setCharacterEncoding("UTF-8");
      String result = createUrls(site, sitemapConfiguration, request, response);
      boolean gzipCompression = getBooleanParameter(request, SitemapRequestParams.PARAM_GZIP_COMPRESSION, false);
      writeResultToResponse(result, response, gzipCompression);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException e) {
      String msg = "Error when creating url list: " + e.getMessage();
      handleError(response, msg, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (IllegalRequestException e) {
      handleError(response, e.getMessage(), null, HttpServletResponse.SC_NOT_FOUND);
    } catch(Exception e) {
      handleError(response, e.getMessage(), e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    return null;
  }

  /**
   * This default implementation returns the injected configuration.
   */
  @NonNull
  private SitemapSetup selectConfiguration(Site site) {
    return sitemapSetupFactory.createSitemapSetup(site);
  }

  /**
   * Walks recursivly through the repository and resolves the link url
   * for each content object that is not filtered through the exclusion
   * criteria.
   * <p>
   * This is a suitable point of entry for internal sitemap generation
   * triggers, unless link building depends on interceptors.
   *
   * @return The renderer's result.
   */
  String createUrls(Site site, SitemapSetup config, HttpServletRequest request, HttpServletResponse response) {
    SitemapRendererFactory sitemapRendererFactory = config.getSitemapRendererFactory();
    SitemapRenderer sitemapRenderer = sitemapRendererFactory.createInstance();
    sitemapRenderer.setSite(site);
    sitemapRenderer.startUrlList();
    for (SitemapUrlGenerator urlGenerator : config.getUrlGenerators()) {
      urlGenerator.generateUrls(request, response, site, sitemapRendererFactory.absoluteUrls(), config.getProtocol(), sitemapRenderer);
    }
    sitemapRenderer.endUrlList();
    return sitemapRenderer.getResponse();
  }


  // --- utilities --------------------------------------------------

  @NonNull
  protected final Site siteByRequest(HttpServletRequest request) {
    Site site = siteResolver.findSiteByPath(request.getPathInfo());
    if (site==null) {
      throw new IllegalRequestException("Cannot resolve a site from " + request.getPathInfo());
    }
    return site;
  }


  // --- internal ---------------------------------------------------

  private void handleError(HttpServletResponse response, String msg, Exception e, int httpErrorCode) {
    if (e != null) {
      LOG.error(msg, e);
    } else {
      LOG.info(msg);
    }
    try {
      response.sendError(httpErrorCode, msg);
    } catch (IOException e1) {
      LOG.error("Cannot send error to client.", e1);
    }
  }

  /**
   * Writes the generated URLs to the response output stream
   *
   * @param result renderer's result.
   * @param response The http servlet response to write the urls into.
   * @param gzipCompression compression flag
   * @throws IOException
   */
  private void writeResultToResponse(String result, HttpServletResponse response, boolean gzipCompression) throws IOException {
    OutputStream out = createOutputStream(response, gzipCompression);
    try {
      out.write(result.getBytes("UTF-8"));
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Helper for parsing boolean parameter values.
   *
   * @param request The request that contains the parameter
   * @param param The name of the parameter
   * @param defaultValue The default value if the parameter is not set
   * @return A boolean param from the request
   */
  private boolean getBooleanParameter(HttpServletRequest request, String param, boolean defaultValue) {
    String value = request.getParameter(param);
    if (value != null) {
      return Boolean.parseBoolean(value);
    }
    return defaultValue;
  }

  /**
   * Creates the output stream for writing the response depending of passed parameters.
   *
   * @param response The HttpServletResponse to write for.
   * @param gzipCompression compression flag
   * @return The OutputStream
   * @throws IOException
   */
  private OutputStream createOutputStream(HttpServletResponse response, boolean gzipCompression) throws IOException {
    if (gzipCompression) {
      response.setHeader("Content-Encoding", "gzip");
      return new GZIPOutputStream(response.getOutputStream());
    }
    return new BufferedOutputStream(response.getOutputStream());
  }
}
