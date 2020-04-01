package com.coremedia.blueprint.cae.sitemap;

/**
 * Renders the URLs separated by newline.
 *
 * Useful as clickpaths for performance tests.
 */
class PlainSitemapRenderer extends AbstractSitemapRenderer {
  @Override
  public void appendUrl(String url) {
    super.appendUrl(url);
    println(url);
  }
}
