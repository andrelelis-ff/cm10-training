package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import org.springframework.web.util.UriComponentsBuilder;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

class WcsAasCrawlSitemapRenderer extends WcsCrawlSitemapRenderer {
  @Override
  protected String toCrawlurl(String url) {
    if (isNotBlank(url)) {
      String[] crawlAndIndexlUrl = url.split("###");
      if (crawlAndIndexlUrl.length == 2) {
        return UriComponentsBuilder.fromUriString(crawlAndIndexlUrl[0])
                .queryParam("view", "forCrawler")
                .build().toUriString();
      }
    }

    throw new IllegalStateException("Wrong url format: " + url);
  }

  @Override
  protected String toIndexurl(String url) {
    if (isNotBlank(url)) {
      String[] crawlAndIndexlUrl = url.split("###");
      if (crawlAndIndexlUrl.length == 2) {
        return crawlAndIndexlUrl[1];
      }
    }
    throw new IllegalStateException("Wrong url format: " + url);
  }
}
