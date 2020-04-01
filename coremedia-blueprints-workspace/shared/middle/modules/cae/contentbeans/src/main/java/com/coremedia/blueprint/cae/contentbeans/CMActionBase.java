package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMAction.
 * Should not be changed.
 */
public abstract class CMActionBase extends CMPlaceholderImpl implements CMAction {
  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMAction} objects
   */
  @Override
  public CMAction getMaster() {
    return (CMAction) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMAction> getVariantsByLocale() {
    return getVariantsByLocale(CMAction.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMAction> getLocalizations() {
    return (Collection<? extends CMAction>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMAction>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMAction>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMAction>> getAspects() {
    return (List<? extends Aspect<? extends CMAction>>) super.getAspects();
  }

  @Override
  public String getType() {
    return getContent().getString(TYPE);
  }
}
