package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.xml.DelegatingSaxHandler;
import com.coremedia.xml.XmlUtil5;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.xml.XMLConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SitemapXmlRendererTest {
  private static final String NICE_URL = "http://www.coremedia.com/";
  private static final String VERY_LONG_URL = "http://www.acme.com/what/a/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/url.html";
  private static final String TOO_LONG_URL = "http://www.acme.com/what/a/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/long/url.html";

  @Test
  public void testSimpleSitemap() {
    SitemapXmlRenderer sxr = new SitemapXmlRenderer("//www.coremedia.com");
    sxr.startUrlList();
    assertTrue("Not empty", sxr.isEmpty());
    assertTrue("Cannot append url", sxr.canAppend(NICE_URL));
    sxr.appendUrl(NICE_URL);
    assertFalse("Empty", sxr.isEmpty());
    sxr.endUrlList();
    String result = sxr.getResponse();
    validate(result);
  }

  @Test
  public void testTooManyUrls() {
    SitemapXmlRenderer sxr = new SitemapXmlRenderer("//www.coremedia.com");
    sxr.startUrlList();
    for (int i=0; i<50000; ++i) {
      sxr.appendUrl(NICE_URL);
    }
    try {
      sxr.appendUrl(NICE_URL);
      fail("Must not accept the 50001st URL.");
    } catch (Exception e) {
      // Ok!
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUrlPrefix() {
    SitemapXmlRenderer sxr = new SitemapXmlRenderer("###");
    sxr.startUrlList();
    sxr.appendUrl(NICE_URL);
    fail("Must not accept invalid URL Prefix.");
  }

  @Test
  public void testTooLongUrl() {
    SitemapXmlRenderer sxr = new SitemapXmlRenderer("//www.acme.com");
    sxr.startUrlList();
    try {
      sxr.appendUrl(TOO_LONG_URL);
      fail("Must not accept too long URLs.");
    } catch (Exception e) {
      // Ok!
    }
  }

  @Test
  public void testTooBigSitemap() {
    SitemapXmlRenderer sxr = new SitemapXmlRenderer("//www.acme.com");
    sxr.startUrlList();
    int i=0;
    try {
      for (i=0; i<50000; ++i) {
        sxr.appendUrl(VERY_LONG_URL);
      }
      fail("Must not create a too large sitemap");
    } catch (Exception e) {
      // We expect an exception because of the overall size,
      // but some URLs must have been appended successfully.
      assertTrue("Wrong exception", i>5000);
    }
  }

  private void validate(String sitemap) {
    assertNotNull("Sitemap is null", sitemap);

    try {
      XmlUtil5 xmlutil = new XmlUtil5();
      xmlutil.registerSchema("http://www.sitemaps.org/schemas/sitemap/0.9",
              XMLConstants.W3C_XML_SCHEMA_NS_URI,
              new String[]{"http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"});
      InputStream inputStream = new ByteArrayInputStream(sitemap.getBytes("UTF-8"));

      try {
        // DelegatingSaxHandler for strict validation
        xmlutil.saxParse(inputStream, new DelegatingSaxHandler(null, null, null), null, "http://www.sitemaps.org/schemas/sitemap/0.9");
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    } catch (Exception e) {
      throw new RuntimeException("Sitemap validation failed", e);
    }
  }

}
