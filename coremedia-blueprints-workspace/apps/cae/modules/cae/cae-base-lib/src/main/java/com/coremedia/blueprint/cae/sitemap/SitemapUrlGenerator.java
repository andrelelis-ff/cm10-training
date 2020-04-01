package com.coremedia.blueprint.cae.sitemap;


import com.coremedia.cap.multisite.Site;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SitemapUrlGenerator {
  void generateUrls(HttpServletRequest request,
                    HttpServletResponse response,
                    Site site,
                    boolean absoluteUrls,
                    String protocol,
                    UrlCollector urlCollector);
}
