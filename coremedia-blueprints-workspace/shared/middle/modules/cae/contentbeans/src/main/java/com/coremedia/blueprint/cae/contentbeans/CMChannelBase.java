package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMChannel.
 * Should not be changed.
 */
public abstract class CMChannelBase extends CMContextImpl implements CMChannel {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMChannel} objects
   */
  @Override
  public CMChannel getMaster() {
    return (CMChannel) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMChannel> getVariantsByLocale() {
    return getVariantsByLocale(CMChannel.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMChannel> getLocalizations() {
    return (Collection<? extends CMChannel>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMChannel>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMChannel>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMChannel>> getAspects() {
    return (List<? extends Aspect<? extends CMChannel>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #HEADER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMLinkable} objects
   */
  @Override
  public List<? extends Linkable> getHeader() {
    List<Content> contents = getContent().getLinks(CMChannel.HEADER);
    return createBeansFor(contents, CMLinkable.class);
  }

  /**
   * Returns the value of the document property {@link #FOOTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMLinkable} objects
   */
  @Override
  public List<? extends Linkable> getFooter() {
    List<Content> contents = getContent().getLinks(CMChannel.FOOTER);
    return createBeansFor(contents, CMLinkable.class);
  }
}
