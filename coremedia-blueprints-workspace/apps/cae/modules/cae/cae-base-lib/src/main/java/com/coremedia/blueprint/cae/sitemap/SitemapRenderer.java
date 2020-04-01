package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.multisite.Site;

/**
 * Enhance UrlCollector with some package internal features.
 */
public interface SitemapRenderer extends UrlCollector {
  /**
   * Initialize the renderer.
   */
  void startUrlList();

  /**
   * Close the list.
   */
  void endUrlList();

  /**
   * Return the response.
   * <p>
   * This may be the actual URL list or just a message if the URL list itself is
   * persisted somehow.
   *
   * @return the response to be written into the HttpResponse
   */
  String getResponse();

  /**
   * Set the site the request refers to.
   */
  void setSite(Site site);
}
