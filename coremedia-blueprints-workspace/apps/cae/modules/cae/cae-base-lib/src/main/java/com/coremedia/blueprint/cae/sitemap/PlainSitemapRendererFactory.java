package com.coremedia.blueprint.cae.sitemap;

/**
 * Creates PlainSitemapRenderers.
 * <p>
 * Trivial, since the PlainSitemapRenderer does not need any configuration.
 */
public final class PlainSitemapRendererFactory implements SitemapRendererFactory {
  @Override
  public SitemapRenderer createInstance() {
    return new PlainSitemapRenderer();
  }

  @Override
  public String getContentType() {
    return "text/plain";
  }

  @Override
  public boolean absoluteUrls() {
    return false;
  }
}
