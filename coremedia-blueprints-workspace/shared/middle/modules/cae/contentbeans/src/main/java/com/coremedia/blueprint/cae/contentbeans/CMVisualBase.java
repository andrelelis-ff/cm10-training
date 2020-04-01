package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMVisual;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMVisual.
 * Should not be changed.
 */
public abstract class CMVisualBase extends CMMediaImpl implements CMVisual {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMVisual} objects
   */
  @Override
  public CMVisual getMaster() {
    return (CMVisual) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMVisual> getVariantsByLocale() {
    return getVariantsByLocale(CMVisual.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMVisual> getLocalizations() {
    return (Collection<? extends CMVisual>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMVisual>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMVisual>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMVisual>> getAspects() {
    return (List<? extends Aspect<? extends CMVisual>>) super.getAspects();
  }

  /**
   * Returns the value of the document property (@link #dataUrl}
   *
   * @return the value of the document property (@link #dataUrl}
   */
  @Override
  public String getDataUrl() {
    return getContent().getString(DATA_URL);
  }

  /**
   * Returns the value of the document property {@link #WIDTH}.
   *
   * @return the value of the document property {@link #WIDTH} or null if not set.
   */
  @Override
  public Integer getWidth() {
    return getContent().getInteger(WIDTH);
  }

  /**
   * Returns the value of the document property {@link #HEIGHT}.
   *
   * @return the value of the document property {@link #HEIGHT} or null if not set.
   */
  @Override
  public Integer getHeight() {
    return getContent().getInteger(HEIGHT);
  }
}
