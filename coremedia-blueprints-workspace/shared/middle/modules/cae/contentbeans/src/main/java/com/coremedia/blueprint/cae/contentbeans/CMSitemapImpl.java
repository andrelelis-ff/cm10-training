package com.coremedia.blueprint.cae.contentbeans;

/**
 * Generated extension class for immutable beans of document type "CMSitemap".
 */
public class CMSitemapImpl extends CMSitemapBase {
  private static final int DEFAULT_SITEMAP_DEPTH = 3;
  private static final String PROPERTY_SITEMAP_DEPTH = "sitemap_depth";

  @Override
  public int getSitemapDepth() {
    return getSettingsService().settingWithDefault(PROPERTY_SITEMAP_DEPTH, Integer.class, DEFAULT_SITEMAP_DEPTH, getContent());
  }
}
