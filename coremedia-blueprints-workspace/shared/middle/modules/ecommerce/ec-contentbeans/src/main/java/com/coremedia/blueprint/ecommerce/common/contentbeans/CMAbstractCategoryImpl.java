package com.coremedia.blueprint.ecommerce.common.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMChannelImpl;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CMAbstractCategoryImpl extends CMChannelImpl implements CMAbstractCategory {

  // --- Standard Blueprint typing overrides ------------------------

  @Override
  public CMAbstractCategory getMaster() {
    return (CMAbstractCategory) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMAbstractCategory> getVariantsByLocale() {
    return getVariantsByLocale(CMAbstractCategory.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMAbstractCategory> getLocalizations() {
    return (Collection<? extends CMAbstractCategory>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMAbstractCategory>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMAbstractCategory>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMAbstractCategory>> getAspects() {
    return (List<? extends Aspect<? extends CMAbstractCategory>>) super.getAspects();
  }

  // --- Content property getters -----------------------------------

  @Override
  public Struct getPdpPagegridStruct() {
    return getContent().getStruct(PDP_PAGEGRID);
  }

  // --- Features ---------------------------------------------------
}
