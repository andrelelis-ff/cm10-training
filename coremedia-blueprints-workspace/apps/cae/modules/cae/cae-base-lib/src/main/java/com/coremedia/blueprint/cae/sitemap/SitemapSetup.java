package com.coremedia.blueprint.cae.sitemap;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Container for all beans needed to configure a SitemapGenerationController.
 * <p>
 * While this indirection looks unnecessarily complex for handlers with fix
 * configurations, it allows for an easy configuration switch
 * (e.g. request/site specific) in more generic handlers (namely the
 * {@link com.coremedia.blueprint.cae.sitemap.SitemapGenerationHandler}).
 */
public class SitemapSetup {
  private List<SitemapUrlGenerator> urlGenerators;
  private SitemapRendererFactory sitemapRendererFactory;
  private String protocol;


  // --- configuration ----------------------------------------------

  @Required
  public void setUrlGenerators(List<SitemapUrlGenerator> urlGenerators) {
    this.urlGenerators = urlGenerators;
  }

  @Required
  public void setSitemapRendererFactory(SitemapRendererFactory sitemapRendererFactory) {
    this.sitemapRendererFactory = sitemapRendererFactory;
  }

  /**
   * The protocol of the site URLs (should be http or https)
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }


  // --- features ---------------------------------------------------

  List<SitemapUrlGenerator> getUrlGenerators() {
    return urlGenerators;
  }

  SitemapRendererFactory getSitemapRendererFactory() {
    return sitemapRendererFactory;
  }

  String getProtocol() {
    return protocol;
  }
}
