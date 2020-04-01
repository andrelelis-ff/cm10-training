package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMSitemap;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMSitemap.
 * Should not be changed.
 */
public abstract class CMSitemapBase extends CMTeasableImpl implements CMSitemap {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMSitemap} objects
   */
  @Override
  public CMSitemap getMaster() {
    return (CMSitemap) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMSitemap> getVariantsByLocale() {
    return getVariantsByLocale(CMSitemap.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMSitemap> getLocalizations() {
    return (Collection<? extends CMSitemap>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMSitemap>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMSitemap>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMSitemap>> getAspects() {
    return (List<? extends Aspect<? extends CMSitemap>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #ROOT}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMLinkable} objects
   */
  @Override
  public CMNavigation getRoot() {
    Content rootValue = getContent().getLink(ROOT);
    return createBeanFor(rootValue, CMNavigation.class);
  }
}
