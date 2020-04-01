package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMImage;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMImage.
 * Should not be changed.
 */
public abstract class CMImageBase extends CMLocalizedImpl implements CMImage {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMImage} objects
   */
  @Override
  public CMImage getMaster() {
    return (CMImage) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMImage> getVariantsByLocale() {
    return getVariantsByLocale(CMImage.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMImage> getLocalizations() {
    return (Collection<? extends CMImage>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMImage>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMImage>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMImage>> getAspects() {
    return (List<? extends Aspect<? extends CMImage>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #DATA}.
   *
   * @return the value of the document property {@link #DATA}
   */
  @Override
  public Blob getData() {
    return getContent().getBlobRef(DATA);
  }

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   */
  @Override
  public String getDescription() {
    return getContent().getString(DESCRIPTION);
  }
}
