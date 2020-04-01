package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.cap.multisite.Site;

import java.io.IOException;

/**
 * Trigger a sitemap for sites
 */
public interface SitemapTrigger {

  /**
   * Trigger generation of sitemaps for all sites.
   */
  void generateSitemaps();
}
