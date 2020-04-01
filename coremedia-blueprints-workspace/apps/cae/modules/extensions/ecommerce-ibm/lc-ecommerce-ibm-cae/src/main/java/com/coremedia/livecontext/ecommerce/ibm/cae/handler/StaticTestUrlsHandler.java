package com.coremedia.livecontext.ecommerce.ibm.cae.handler;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.blueprint.cae.sitemap.SitemapRequestParams;
import com.coremedia.blueprint.cae.web.IllegalRequestException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.objectserver.web.links.TokenResolverHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_INTERNAL;

/**
 * Generates a list of static test URLs for a site.
 * <p>
 * Example URL: http://localhost:40980/blueprint/servlet/internal/aurora/statictesturls
 * <p>
 * Test URLs are configured via Spring on site basis. They may contain commerce related
 * tokens (like {storeId}, {locale} or {catalogId}) which will be replaced. As site
 * identifier the "vanity" site name (like "aurora" or the "real" site name (like
 * "Aurora Augmentation")can be used in configuration.
 */
@RequestMapping
public class StaticTestUrlsHandler {

  private static final Logger LOG = LoggerFactory.getLogger(StaticTestUrlsHandler.class);

  private static final String TESTURLS = "statictesturls";

  private SiteResolver siteResolver;
  private UrlPathFormattingHelper urlPathFormattingHelper;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  private Map<String, List<String>> testUrlsMap;

  public static final String URI_PATTERN
          = '/' + PREFIX_INTERNAL
          + "/{" + SEGMENT_ROOT + '}'
          + '/' + TESTURLS;

  @GetMapping(URI_PATTERN)
  public ModelAndView handleRequest(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    try {
      Site site = siteByRequest(request);

      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");

      String result = createUrls(site);

      boolean gzipCompression = getBooleanParameter(request, SitemapRequestParams.PARAM_GZIP_COMPRESSION);
      writeResultToResponse(result, response, gzipCompression);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException e) {
      String msg = "Error when creating url list: " + e.getMessage();
      handleError(response, msg, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (IllegalRequestException e) {
      handleError(response, e.getMessage(), null, HttpServletResponse.SC_NOT_FOUND);
    } catch (Exception e) {
      handleError(response, e.getMessage(), e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    return null;
  }

  // --- features ---------------------------------------------------

  @NonNull
  private String createUrls(@NonNull Site site) {
    StringBuilder testUrlsBuilder = new StringBuilder();

    Optional<CommerceConnection> commerceConnection = commerceConnectionInitializer.findConnectionForSite(site);
    commerceConnection.ifPresent(connection -> {
      StoreContext storeContext = connection.getStoreContext();

      List<String> testUrls = testUrlsMap.get(getSiteVanityName(site));
      if (testUrls == null) {
        testUrls = testUrlsMap.get(site.getName());
      }

      if (testUrls != null) {
        for (String s : testUrls) {
          testUrlsBuilder.append(replaceTokens(s, storeContext)).append('\n');
        }
      }
    });

    return testUrlsBuilder.toString();
  }

  // --- utilities --------------------------------------------------

  @NonNull
  private static String replaceTokens(@NonNull String url, @NonNull StoreContext storeContext) {
    Locale locale = storeContext.getLocale();

    Map<String, String> parameterMap = new HashMap<>();
    parameterMap.put("storeId", storeContext.getStoreId());
    parameterMap.put("storeName", storeContext.getStoreName());
    parameterMap.put("catalogId", storeContext.getCatalogId().map(CatalogId::value).orElse(null));
    parameterMap.put("siteId", storeContext.getSiteId());
    parameterMap.put("locale", locale != null ? locale.toLanguageTag() : null);
    parameterMap.put("language", locale != null ? locale.getLanguage() : null);

    return TokenResolverHelper.replaceTokens(url, parameterMap, false, false);
  }

  @NonNull
  private Site siteByRequest(@NonNull HttpServletRequest request) {
    String pathInfo = request.getPathInfo();

    Site site = siteResolver.findSiteByPath(pathInfo);

    if (site == null) {
      throw new IllegalRequestException("Cannot resolve a site from " + pathInfo);
    }

    return site;
  }

  @NonNull
  private String getSiteVanityName(@NonNull Site site) {
    Content rootChannel = site.getSiteRootDocument();
    if (rootChannel != null) {
      return urlPathFormattingHelper.getVanityName(rootChannel);
    }

    return site.getName();
  }

  // --- internal ---------------------------------------------------

  private static void handleError(@NonNull HttpServletResponse response, String msg, @Nullable Exception e,
                                  int httpErrorCode) {
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
   * @param result          renderer's result.
   * @param response        The http servlet response to write the urls into.
   * @param gzipCompression compression flag
   * @throws IOException in case of an io error
   */
  private static void writeResultToResponse(@NonNull String result, @NonNull HttpServletResponse response,
                                            boolean gzipCompression) throws IOException {
    OutputStream out = createOutputStream(response, gzipCompression);

    try {
      out.write(result.getBytes(StandardCharsets.UTF_8));
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Helper for parsing boolean parameter values.
   *
   * @param request The request that contains the parameter
   * @param param   The name of the parameter
   * @return A boolean param from the request
   */
  private static boolean getBooleanParameter(@NonNull HttpServletRequest request, @NonNull String param) {
    String value = request.getParameter(param);
    return value != null && Boolean.parseBoolean(value);
  }

  /**
   * Creates the output stream for writing the response depending of passed parameters.
   *
   * @param response        The HttpServletResponse to write for.
   * @param gzipCompression compression flag
   * @return The OutputStream
   * @throws IOException in case of io error
   */
  @NonNull
  private static OutputStream createOutputStream(@NonNull HttpServletResponse response, boolean gzipCompression)
          throws IOException {
    if (gzipCompression) {
      response.setHeader("Content-Encoding", "gzip");
      return new GZIPOutputStream(response.getOutputStream());
    }

    return new BufferedOutputStream(response.getOutputStream());
  }

  // --- configuration ----------------------------------------------

  @Required
  public void setSiteResolver(SiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  @Required
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  @Required
  public void setUrlPathFormattingHelper(UrlPathFormattingHelper urlPathFormattingHelper) {
    this.urlPathFormattingHelper = urlPathFormattingHelper;
  }

  public void setTestUrlsMap(Map<String, List<String>> testUrlsMap) {
    this.testUrlsMap = testUrlsMap;
  }
}
