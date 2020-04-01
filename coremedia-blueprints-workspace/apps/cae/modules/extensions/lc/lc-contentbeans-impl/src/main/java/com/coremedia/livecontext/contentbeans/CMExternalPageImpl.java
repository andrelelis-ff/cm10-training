package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMChannelImpl;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CMExternalPageImpl extends CMChannelImpl implements CMExternalPage {

  // --- Standard Blueprint typing overrides ------------------------

  @Override
  public CMExternalPage getMaster() {
    return (CMExternalPage) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMExternalPage> getVariantsByLocale() {
    return getVariantsByLocale(CMExternalPage.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMExternalPage> getLocalizations() {
    return (Collection<? extends CMExternalPage>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMExternalPage>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMExternalPage>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMExternalPage>> getAspects() {
    return (List<? extends Aspect<? extends CMExternalPage>>) super.getAspects();
  }

  // --- Content property getters -----------------------------------

  @Override
  public String getExternalId() {
    return getContent().getString(EXTERNAL_ID);
  }

  @Override
  public String getExternalUriPath() {
    return getContent().getString(EXTERNAL_URI_PATH);
  }

  // --- Features ---------------------------------------------------

}
