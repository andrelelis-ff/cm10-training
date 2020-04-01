package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMGallery;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMGallery.
 * Should not be changed.
 */
public abstract class CMGalleryBase<T extends CMMedia> extends CMCollectionImpl<T> implements CMGallery<T> {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMCollection} objects
   */
  @Override
  public CMGallery<T> getMaster() {
    return (CMGallery<T>) super.getMaster();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Locale, ? extends CMGallery<T>> getVariantsByLocale() {
    return (Map<Locale, ? extends CMGallery<T>>) super.getVariantsByLocale();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMGallery<T>> getLocalizations() {
    return (Collection<? extends CMGallery<T>>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMGallery<T>>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMGallery<T>>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMGallery<T>>> getAspects() {
    return (List<? extends Aspect<? extends CMGallery<T>>>) super.getAspects();
  }
}
