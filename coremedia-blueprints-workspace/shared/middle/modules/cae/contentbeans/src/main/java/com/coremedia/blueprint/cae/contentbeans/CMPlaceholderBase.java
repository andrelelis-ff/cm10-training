package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMPlaceholder;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMPlaceholder.
 * Should not be changed.
 */
public abstract class CMPlaceholderBase extends CMTeasableImpl implements CMPlaceholder {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMLinkable} objects
   */
  @Override
  public CMPlaceholder getMaster() {
    return (CMPlaceholder) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMPlaceholder> getVariantsByLocale() {
    return getVariantsByLocale(CMPlaceholder.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMPlaceholder> getLocalizations() {
    return (Collection<? extends CMPlaceholder>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMPlaceholder>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMPlaceholder>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMPlaceholder>> getAspects() {
    return (List<? extends Aspect<? extends CMPlaceholder>>) super.getAspects();
  }

  @Override
  public String getId() {
    return getContent().getString(ID);
  }
}
