package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.xml.Markup;

import java.util.*;

/**
 * Generated base class for immutable beans of document type CMMedia.
 * Should not be changed.
 */
public abstract class CMMediaBase extends CMTeasableImpl implements CMMedia {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMMedia} objects
   */
  @Override
  public CMMedia getMaster() {
    return (CMMedia) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMMedia> getVariantsByLocale() {
    return getVariantsByLocale(CMMedia.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMMedia> getLocalizations() {
    return (Collection<? extends CMMedia>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMMedia>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMMedia>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMMedia>> getAspects() {
    return (List<? extends Aspect<? extends CMMedia>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #CAPTION}.
   *
   * @return the value of the document property {@link #CAPTION}
   */
  @Override
  public Markup getCaption() {
    return getMarkup(CAPTION);
  }

  /**
   * Returns the value of the document property {@link #ALT}.
   *
   * @return the value of the document property {@link #ALT}
   */
  @Override
  public String getAlt() {
    return getContent().getString(ALT);
  }

  @Override
  public Map<String,String> getTransformMap() {
    return Collections.emptyMap();
  }

  /**
   * Returns the value of the document property {@link #COPYRIGHT}.
   *
   * @return the value of the document property {@link #COPYRIGHT}
   */
  @Override
  public String getCopyright() {
    return getContent().getString(COPYRIGHT);
  }

  /**
   * Standard Implementation of the getter to the dataUrl property. Should be overridden in Subtypes of CMVisual and
   * CMAudio if an external URL should not be preferred in favour of a local object. To do that simply override and return null.
   *
   * @return the value of the document property dataUrl that is introduced in CMVisual and CMAudio.
   */
  protected String getDataUrl() {
    return null;
  }

}
