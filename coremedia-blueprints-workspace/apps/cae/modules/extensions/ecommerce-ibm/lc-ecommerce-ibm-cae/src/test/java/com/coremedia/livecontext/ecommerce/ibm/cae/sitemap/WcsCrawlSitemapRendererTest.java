package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WcsCrawlSitemapRendererTest {
  private static final String NICE_URL = "http://www.coremedia.com";
  private static final String NICE_URL2 = "http://www.economist.com";
  private static final String EXPECTED_SITEMAP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset>\n<url crawlurl=\"http://www.coremedia.com\" indexurl=\"http://www.coremedia.com\"/><url crawlurl=\"http://www.economist.com\" indexurl=\"http://www.economist.com\"/></urlset>\n";

  @Test
  public void testSimpleSitemap() {
    WcsCrawlSitemapRenderer testling = new WcsCrawlSitemapRenderer();
    testling.startUrlList();
    testling.appendUrl(NICE_URL);
    testling.appendUrl(NICE_URL2);
    testling.endUrlList();
    String result = testling.getResponse();
    assertEquals("The result not as expected", EXPECTED_SITEMAP, result);
  }

}
