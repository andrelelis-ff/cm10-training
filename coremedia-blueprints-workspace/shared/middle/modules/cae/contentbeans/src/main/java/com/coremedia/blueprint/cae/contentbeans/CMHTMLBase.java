package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMHTML;
import com.coremedia.cae.aspect.Aspect;

import com.coremedia.xml.Markup;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMHTML.
 * Should not be changed.
 */
public abstract class CMHTMLBase extends CMMediaImpl implements CMHTML {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMHTML} objects
   */
  @Override
  public CMHTML getMaster() {
    return (CMHTML) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMHTML> getVariantsByLocale() {
    return getVariantsByLocale(CMHTML.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMHTML> getLocalizations() {
    return (Collection<? extends CMHTML>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMHTML>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMHTML>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMHTML>> getAspects() {
    return (List<? extends Aspect<? extends CMHTML>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   */
  @Override
  public String getDescription() {
    return getContent().getString(CMHTML.DESCRIPTION);
  }

  /**
   * Returns the value of the document property {@link #DATA}.
   *
   * @return the value of the document property {@link #DATA}
   */
  @Override
  public Markup getData() {
    return getMarkup(DATA);
  }

}
