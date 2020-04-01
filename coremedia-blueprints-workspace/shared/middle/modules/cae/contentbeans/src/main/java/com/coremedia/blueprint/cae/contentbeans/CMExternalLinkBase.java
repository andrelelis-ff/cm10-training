package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMExternalLink;
import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMExternalLink.
 * Should not be changed.
 */
public abstract class CMExternalLinkBase extends CMTeasableImpl implements CMExternalLink {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMExternalLink} objects
   */
  @Override
  public CMExternalLink getMaster() {
    return (CMExternalLink) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMExternalLink> getVariantsByLocale() {
    return getVariantsByLocale(CMExternalLink.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMExternalLink> getLocalizations() {
    return (Collection<? extends CMExternalLink>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMExternalLink>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMExternalLink>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMExternalLink>> getAspects() {
    return (List<? extends Aspect<? extends CMExternalLink>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #URL}.
   *
   * @return the value of the document property {@link #URL}
   */
  @Override
  public String getUrl() {
    return getContent().getString(CMExternalLink.URL);
  }

  @Override
  public boolean isOpenInNewTab() {
    return getContent().getBoolean(OPEN_IN_NEW_TAB);
  }
}
