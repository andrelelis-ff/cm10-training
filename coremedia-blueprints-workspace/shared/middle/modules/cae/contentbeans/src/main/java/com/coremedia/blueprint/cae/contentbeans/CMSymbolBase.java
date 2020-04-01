package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMSymbol;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.List;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMSymbol.
 * Should not be changed.
 */
public abstract class CMSymbolBase extends CMLocalizedImpl implements CMSymbol {

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMSymbol>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMSymbol>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMSymbol>> getAspects() {
    return (List<? extends Aspect<? extends CMSymbol>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   */
  @Override
  public String getDescription() {
    return getContent().getString(DESCRIPTION);
  }

  /**
   * Returns the value of the document property {@link #ICON}.
   *
   * @return the value of the document property {@link #ICON}
   */
  @Override
  public Blob getIcon() {
    return getContent().getBlobRef(CMSymbol.ICON);
  }
}
