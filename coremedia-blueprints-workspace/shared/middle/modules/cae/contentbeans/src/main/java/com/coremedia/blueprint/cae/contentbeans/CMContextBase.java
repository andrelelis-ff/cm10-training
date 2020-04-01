package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMContext.
 * Should not be changed.
 */
public abstract class CMContextBase extends CMNavigationImpl implements CMContext {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMContext} object
   */
  @Override
  public CMContext getMaster() {
    return (CMContext) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMContext> getVariantsByLocale() {
    return getVariantsByLocale(CMContext.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMContext> getLocalizations() {
    return (Collection<? extends CMContext>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMContext>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMContext>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMContext>> getAspects() {
    return (List<? extends Aspect<? extends CMContext>>) super.getAspects();
  }

}
