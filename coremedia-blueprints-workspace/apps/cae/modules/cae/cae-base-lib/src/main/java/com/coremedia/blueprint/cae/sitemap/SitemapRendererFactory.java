package com.coremedia.blueprint.cae.sitemap;


public interface SitemapRendererFactory {
  /**
   * Returns a SitemapRenderer.
   *
   * @return a SitemapRenderer
   */
  SitemapRenderer createInstance();

  /**
   * Returns the content type of the renderers' results, e.g. "text/plain".
   *
   * @return the content type of the renderers' results, e.g. "text/plain"
   */
  String getContentType();

  /**
   * Returns true if the renderers expect absolute URLs.
   *
   * @return true if the renderers expect absolute URLs
   */
  boolean absoluteUrls();
}
