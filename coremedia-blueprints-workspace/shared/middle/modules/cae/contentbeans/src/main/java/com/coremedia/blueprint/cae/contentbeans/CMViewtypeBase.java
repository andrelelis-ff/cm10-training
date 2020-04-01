package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMViewtype;
import com.coremedia.cae.aspect.Aspect;

import java.util.List;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMViewtype.
 * Should not be changed.
 */
public abstract class CMViewtypeBase extends CMSymbolImpl implements CMViewtype {

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMViewtype>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMViewtype>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMViewtype>> getAspects() {
    return (List<? extends Aspect<? extends CMViewtype>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #LAYOUT}.
   *
   * @return the value of the document property {@link #LAYOUT}
   */
  @Override
  public String getLayout() {
    return getContent().getString(LAYOUT);
  }
}
