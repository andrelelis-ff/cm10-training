package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import com.coremedia.blueprint.cae.sitemap.AbstractSitemapRenderer;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Generates sitemap xml for a Commerce Search Crawler
 */
class WcsCrawlSitemapRenderer extends AbstractSitemapRenderer {
  private static final String OPENING = "<urlset>";
  private static final String CLOSING = "</urlset>";


  // --- SitemapRenderer --------------------------------------------

  @Override
  public void startUrlList() {
    super.startUrlList();

    //NOSONAR : All the following Strings are not "magic".
    println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");  //NOSONAR
    println(OPENING);  //NOSONAR
  }

  @Override
  public void appendUrl(String url) {
    super.appendUrl(url);
    print(toItem(toCrawlurl(url), toIndexurl(url)));
  }

  @Override
  public void endUrlList() {
    println(CLOSING);
    super.endUrlList();
  }


  // --- features ---------------------------------------------------

  protected String toCrawlurl(String url) {
    return url;
  }

  protected String toIndexurl(String url) {
    return url;
  }


  // --- internal ---------------------------------------------------

  private static String toItem(String crawlUrl, String indexUrl) {
    StringBuilder sb = new StringBuilder();

    sb.append("<url crawlurl=\"");  //NOSONAR
    sb.append(StringEscapeUtils.escapeXml(crawlUrl));
    sb.append("\" indexurl=\"");  //NOSONAR
    sb.append(StringEscapeUtils.escapeXml(indexUrl));
    sb.append("\"/>");  //NOSONAR

    return sb.toString();
  }

}
