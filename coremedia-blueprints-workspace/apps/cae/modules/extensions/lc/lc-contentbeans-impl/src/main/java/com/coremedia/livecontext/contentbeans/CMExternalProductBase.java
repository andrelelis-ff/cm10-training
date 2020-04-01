package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMTeasableImpl;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class CMExternalProductBase extends CMTeasableImpl implements CMExternalProduct {

  // --- Standard Blueprint typing overrides ------------------------

  @Override
  public CMExternalProduct getMaster() {
    return (CMExternalProduct) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMExternalProduct> getVariantsByLocale() {
    return getVariantsByLocale(CMExternalProduct.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMExternalProduct> getLocalizations() {
    return (Collection<? extends CMExternalProduct>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMExternalProduct>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMExternalProduct>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMExternalProduct>> getAspects() {
    return (List<? extends Aspect<? extends CMExternalProduct>>) super.getAspects();
  }

  // --- Content property getters -----------------------------------

  @NonNull
  @Override
  public String getExternalId() {
    String externalId = getContent().getString(EXTERNAL_ID);
    return externalId != null ? externalId.trim() : "";
  }

  @Override
  public Struct getPagegridStruct() {
    return getContent().getStruct(PAGEGRID);
  }
}
