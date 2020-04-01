package com.coremedia.blueprint.cae.sitemap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.web.util.UriComponentsBuilder;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOError;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Renders the URLs in sitemap XML format.
 * <p>
 * Use this only for small sites.  If you expect your sitemap to exceed 50,000 entries,
 * use SitemapIndexRenderer, which splits your sitemap into chunks referenced by a
 * sitemap_index file.
 *
 * See http://www.sitemaps.org/protocol.html
 */
class SitemapXmlRenderer extends AbstractSitemapRenderer {

  private static final int SITEMAP_XML_MAX_URL_LENGTH = 2048;
  private static final int SITEMAP_XML_MAX_URLS = 50000;
  private static final int SITEMAP_XML_MAX_SIZE = 10485760;

  private static final String CLOSING = "</urlset>";
  private final String urlHostForSite;

  private int availableSize;


  // --- construct and configure ------------------------------------

  /**
   * Create an instance.
   *
   * @param absoluteUrlPrefix format like in the site prefix mappings
   */
  SitemapXmlRenderer(@NonNull String absoluteUrlPrefix) {
    urlHostForSite = getHostFromUrlString(absoluteUrlPrefix);
  }


  // --- SitemapRenderer --------------------------------------------

  @Override
  public void startUrlList() {
    super.startUrlList();

    //NOSONAR : All the following Strings are not "magic".
    println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");  //NOSONAR
    print("<!-- Generated: ");  //NOSONAR
    print(StringEscapeUtils.escapeXml(new SimpleDateFormat().format(new Date())));
    println(" -->");  //NOSONAR
    println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"");  //NOSONAR
    println("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");  //NOSONAR
    println("        xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">");  //NOSONAR

    availableSize = SITEMAP_XML_MAX_SIZE - toUTF8(currentResult()).length - toUTF8(CLOSING).length;
  }

  @Override
  public void appendUrl(String url) {
    validateUrlHost(url);
    if (currentCount()>=SITEMAP_XML_MAX_URLS) {
      throw new IllegalStateException("Sitemap is full (50,000 entries max).");
    }
    if (url.length()>SITEMAP_XML_MAX_URL_LENGTH) {
      throw new IllegalArgumentException("URL \"" + url + "\" is too long for sitemap xml (2048 chars max).");
    }

    String item = toItem(url);

    int itemSize = toUTF8(item).length;
    if (itemSize > availableSize) {
      throw new IllegalStateException("Sitemap is too large (10,485,760 bytes max).");
    }

    super.appendUrl(url);
    print(item);
    availableSize -= itemSize;
  }

  private void validateUrlHost(@NonNull String url) {
    String urlHost = getHostFromUrlString(url);
    if (!Objects.equals(urlHost, urlHostForSite)) {
      throw new IllegalArgumentException("URL \"" + url + "\" does not belong to the target domain " + urlHostForSite + ".");
    }
  }

  @Nullable
  private static String getHostFromUrlString(@NonNull String url) {
    return UriComponentsBuilder.fromUriString(url).build().getHost();
  }

  @Override
  public void endUrlList() {
    println(CLOSING);
    super.endUrlList();
  }


  // --- features ---------------------------------------------------

  boolean canAppend(String url) {
    return currentCount()<SITEMAP_XML_MAX_URLS && toUTF8(url).length<=availableSize;
  }

  boolean isEmpty() {
    return currentCount() == 0;
  }


  // --- internal ---------------------------------------------------

  private static String toItem(String url) {
    StringBuilder sb = new StringBuilder();

    // sitemapXml size is limited, do not waste chars on indentation or too many linebreaks.
    sb.append("<url>");  //NOSONAR
    sb.append("<loc>");  //NOSONAR
    sb.append(StringEscapeUtils.escapeXml(url));
    sb.append("</loc>");  //NOSONAR
    sb.append("</url>\n");  //NOSONAR

    return sb.toString();
  }

  private static byte[] toUTF8(String s) {
    try {
      return s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IOError(e);
    }
  }
}
