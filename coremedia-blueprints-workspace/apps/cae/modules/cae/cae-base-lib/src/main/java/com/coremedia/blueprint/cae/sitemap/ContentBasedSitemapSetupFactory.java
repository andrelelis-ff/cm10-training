package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.cae.web.IllegalRequestException;
import com.coremedia.cap.multisite.Site;
import org.springframework.beans.factory.annotation.Required;

public class ContentBasedSitemapSetupFactory implements SitemapSetupFactory {

  private SitemapHelper sitemapHelper;

  @Required
  public void setSitemapHelper(SitemapHelper sitemapHelper) {
    this.sitemapHelper = sitemapHelper;
  }

  @Override
  public SitemapSetup createSitemapSetup(Site site) {
    if (!sitemapHelper.isSitemapEnabled(site)) {
      throw new IllegalRequestException("Site " + site + " is not configured for sitemap generation.  Must specify a configuration by setting \"" + SitemapHelper.SITEMAP_ORG_CONFIGURATION_KEY + "\".");
    }
    return sitemapHelper.selectConfiguration(site);
  }
}
