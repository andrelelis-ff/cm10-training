package com.coremedia.livecontext.ecommerce.ibm.cae.sitemap;

import com.coremedia.blueprint.cae.sitemap.SitemapRenderer;
import com.coremedia.blueprint.cae.sitemap.SitemapRendererFactory;

public class SitemapWcsCalistaRendererFactory implements SitemapRendererFactory {
  @Override
  public SitemapRenderer createInstance() {
    return new WcsCalistaCrawlSitemapRenderer();
  }

  @Override
  public String getContentType() {
    return "text/html";
  }

  @Override
  public boolean absoluteUrls() {
    return true;
  }
}
