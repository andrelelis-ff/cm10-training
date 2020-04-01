package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import com.coremedia.blueprint.cae.sitemap.ContentUrlGenerator;
import com.coremedia.blueprint.cae.sitemap.UrlCollector;
import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Url generator for Calista. This generator considers the <code>secure</code>
 * request attribute and builds https links.
 */
public class WcsCalistaContentUrlGenerator extends ContentUrlGenerator {

  @Override
  public void generateUrls(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Site site,
                           boolean absoluteUrls, String protocol, UrlCollector sitemapRenderer) {
    String secureParameter = request.getParameter(SECURE_PARAM_NAME);
    super.generateUrls(request, response, site, absoluteUrls,
            Boolean.parseBoolean(secureParameter) ? "https" : "http", sitemapRenderer);
  }
}
