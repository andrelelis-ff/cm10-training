package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.links.UrlPrefixResolver;
import org.springframework.beans.factory.annotation.Required;

public final class SitemapIndexRendererFactory implements SitemapRendererFactory {

  private String targetDirectory;
  private UrlPrefixResolver urlPrefixResolver;
  private SitemapHelper sitemapHelper;
  private boolean prependBaseUri = true;


  // --- Spring config ----------------------------------------------

  @Required
  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  @Required
  public void setUrlPrefixResolver(UrlPrefixResolver urlPrefixResolver) {
    this.urlPrefixResolver = urlPrefixResolver;
  }

  @Required
  public void setSitemapHelper(SitemapHelper sitemapHelper) {
    this.sitemapHelper = sitemapHelper;
  }

  // Keep consistent with BaseUriPrepender#setActive: Not @Required, default true.
  public void setPrependBaseUri(boolean prependBaseUri) {
    this.prependBaseUri = prependBaseUri;
  }


  // --- SitemapRendererFactory -------------------------------------

  @Override
  public SitemapRenderer createInstance() {
    SitemapIndexRenderer sitemapIndexRenderer = new SitemapIndexRenderer();
    sitemapIndexRenderer.setTargetDirectory(targetDirectory);
    sitemapIndexRenderer.setUrlPrefixResolver(urlPrefixResolver);
    sitemapIndexRenderer.setSitemapHelper(sitemapHelper);
    sitemapIndexRenderer.setPrependBaseUri(prependBaseUri);
    return sitemapIndexRenderer;
  }

  @Override
  public String getContentType() {
    // The SitemapIndexRenderer does not return the actual sitemap index
    // but a success message, so the type is not xml but plain.
    return "text/plain";
  }

  @Override
  public boolean absoluteUrls() {
    return true;
  }
}