package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.multisite.Site;

/**
 * A SitemapSetupFactory is responsible to deliver a SitemapSetup.
 */
public interface SitemapSetupFactory {

  /**
   * Derives a SitemapSetup from the given site.
   *
   * @param site the site a SitemapSetup is requested for.
   * @return the sitemap setup derived from the given site.
   */
  SitemapSetup createSitemapSetup(Site site);
}
