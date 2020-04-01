package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMResourceBundle;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMResourceBundle.
 * Should not be changed.
 */
public abstract class CMResourceBundleBase extends CMLocalizedImpl implements CMResourceBundle {

  @Override
  public CMResourceBundle getMaster() {
    return (CMResourceBundle) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMResourceBundle> getVariantsByLocale() {
    return getVariantsByLocale(CMResourceBundle.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMResourceBundle> getLocalizations() {
    return (Collection<? extends CMResourceBundle>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMResourceBundle>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMResourceBundle>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMResourceBundle>> getAspects() {
    return (List<? extends Aspect<? extends CMResourceBundle>>) super.getAspects();
  }

  @Override
  public Struct getLocalizationStruct() {
    Struct settings = getContent().getStruct(LOCALIZATIONS);
    return settings != null ? settings : getContent().getRepository().getConnection().getStructService().emptyStruct();
  }
}
