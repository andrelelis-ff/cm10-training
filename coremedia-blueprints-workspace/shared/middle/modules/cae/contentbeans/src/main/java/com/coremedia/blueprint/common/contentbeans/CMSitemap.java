package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Represents a human readable sitemap for orientation in the website.
 * </p>
 * <p>
 * Not to be confused with sitemap.org compliant sitemaps for search engines.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMSitemap}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMSitemap extends CMTeasable {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSitemap'.
   */
  String NAME = "CMSitemap";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMSitemap} object
   */
  @Override
  CMSitemap getMaster();

  @Override
  Map<Locale, ? extends CMSitemap> getVariantsByLocale();

  @Override
  Collection<? extends CMSitemap> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMSitemap>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMSitemap>> getAspects();

  /**
   * Name of the document property that holds the site's root channel.
   */
  String ROOT = "root";

  /**
   * Returns the value of the document property {@link #ROOT}.
   *
   * @return a {@link CMLinkable} object
   * @cm.template.api
   */
  CMNavigation getRoot();

  /**
   * Returns the depth of the sitemap, stored in local setting "sitemap_depth". Default is 3.
   *
   * @return depth of the sitemap
   * @cm.template.api
   */
  int getSitemapDepth();
}
