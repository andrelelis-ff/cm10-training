package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.multisite.Site;
import org.springframework.beans.factory.annotation.Required;

public class SpringBasedSitemapSetupFactory implements SitemapSetupFactory {

  private SitemapSetup sitemapSetup;

  @Required
  public void setSitemapSetup(SitemapSetup sitemapSetup) {
    this.sitemapSetup = sitemapSetup;
  }

  @Override
  public SitemapSetup createSitemapSetup(Site site) {
    if (sitemapSetup ==null) {
      throw new IllegalStateException("Sitemap configuration not set.");
    }
    return sitemapSetup;
  }
}
