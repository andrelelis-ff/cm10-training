package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.links.UrlPrefixResolver;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.links.BlueprintUriConstants;
import com.coremedia.cap.multisite.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.util.UriComponentsBuilder;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.ServletContext;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Some sitemap features needed by various classes.
 */
public class SitemapHelper implements ServletContextAware {
  private static final Logger LOG = LoggerFactory.getLogger(SitemapHelper.class);

  static final String SITEMAP_ORG = "sitemap-org";
  static final String SITEMAP_ORG_CONFIGURATION_KEY = "sitemapOrgConfiguration";
  static final String FILE_PREFIX = "sitemap";
  static final String SITEMAP_INDEX_FILENAME = FILE_PREFIX + "_index.xml";

  private SettingsService settingsService;
  private UrlPrefixResolver urlPrefixResolver;
  private Map<String, SitemapSetup> sitemapConfigurations;
  private boolean prependBaseUri = true;
  private ServletContext servletContext;


  // --- configuration ----------------------------------------------

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setUrlPrefixResolver(UrlPrefixResolver urlPrefixResolver) {
    this.urlPrefixResolver = urlPrefixResolver;
  }

  /**
   * Set the configurations for different sites.
   * <p>
   * Each site must specify its suitable sitemap configuration by a setting
   * "sitemapOrgConfiguration" whose value is one of the keys of this map.
   * Typically, there will be one entry for each web presence (e.g. corporate,
   * livecontext), and all the multi language sites will share the
   * configuration.
   *
   * @param sitemapConfigurations a map of configurations
   */
  @Required
  public void setSitemapConfigurations(Map<String, SitemapSetup> sitemapConfigurations) {
    this.sitemapConfigurations = sitemapConfigurations;
  }


  // --- features ---------------------------------------------------

  @NonNull
  SitemapSetup selectConfiguration(Site site) {
    String configKey = settingsService.setting(SitemapHelper.SITEMAP_ORG_CONFIGURATION_KEY, String.class, site);
    if (configKey==null) {
      throw new IllegalArgumentException("Site " + site + " is not configured for sitemap generation.  Must specify a configuration by setting \"" + SitemapHelper.SITEMAP_ORG_CONFIGURATION_KEY + "\".");
    }
    SitemapSetup sitemapConfiguration = sitemapConfigurations.get(configKey);
    if (sitemapConfiguration==null) {
      throw new IllegalStateException("No such sitemap configuration: " + configKey);
    }
    return sitemapConfiguration;
  }

  String sitemapProtocol(Site site) {
    return selectConfiguration(site).getProtocol();
  }

  public boolean isSitemapEnabled(Site site) {
    boolean want = settingsService.setting(SITEMAP_ORG_CONFIGURATION_KEY, String.class, site) != null;
    boolean can = urlPrefixResolver.getUrlPrefix(site.getId(), null, null) != null;
    if (want && !can) {
      LOG.warn("Site {} is sitemap-enabled but has no URL prefix. Sitemap generation would fail.", site);
    }
    return want && can;
  }

  public String sitemapIndexUrl(Site site) {
    StringBuilder stringBuilder = buildSitemapUrlPrefix(site);
    stringBuilder.append(SITEMAP_INDEX_FILENAME);
    return stringBuilder.toString();
  }

  public String sitemapIndexEntryUrl(Site site, String sitemapFilename) {
    StringBuilder stringBuilder = buildSitemapUrlPrefix(site);
    stringBuilder.append(sitemapFilename);
    return stringBuilder.toString();
  }

  private StringBuilder buildSitemapUrlPrefix(Site site) {
    String urlPrefix = urlPrefixResolver.getUrlPrefix(site.getId(), null, null);
    if (urlPrefix == null) {
      throw new IllegalStateException("Cannot determine URL prefix for site " + site.getId());
    }

    // adjust protocol to the one configured for this site
    String protocol = sitemapProtocol(site);
    if (!isBlank(protocol)) {
      UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlPrefix);
      uriComponentsBuilder.scheme(protocol);
      urlPrefix = uriComponentsBuilder.build().toUriString();
    }

    StringBuilder sb = new StringBuilder(urlPrefix);
    if(prependBaseUri) {
      sb.append(servletContext.getContextPath());
      sb.append("/servlet");
    }
    sb.append("/");
    sb.append(BlueprintUriConstants.Prefixes.PREFIX_SERVICE);
    sb.append("/");
    sb.append(SitemapHandler.ACTION_NAME);
    sb.append("/");
    sb.append(site.getId());
    sb.append("/");
    return sb;
  }

  public void setPrependBaseUri(boolean prependBaseUri) {
    this.prependBaseUri = prependBaseUri;
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
