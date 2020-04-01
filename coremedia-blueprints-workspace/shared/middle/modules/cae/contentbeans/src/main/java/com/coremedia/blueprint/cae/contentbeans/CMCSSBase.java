package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMCSS;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Generated base class for immutable beans of document type CMCSS.
 * Should not be changed.
 */
public abstract class CMCSSBase extends CMAbstractCodeImpl implements CMCSS {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMCSS} object
   */
  @Override
  public CMCSS getMaster() {
    return (CMCSS) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMCSS> getVariantsByLocale() {
    return getVariantsByLocale(CMCSS.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMCSS> getLocalizations() {
    return (Collection<? extends CMCSS>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMCSS>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMCSS>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMCSS>> getAspects() {
    return (List<? extends Aspect<? extends CMCSS>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #MEDIA}.
   *
   * @return the value of the document property {@link #MEDIA}
   */
  @Override
  public String getMedia() {
    return getContent().getString(MEDIA);
  }

  /**
   * Returns the value of the document property {@link #INCLUDE}.
   *
   * @return a list of {@link CMCSS} objects
   */
  @Override
  @NonNull
  public List<? extends CMCSS> getInclude() {
    List<Content> contents = getContent().getLinks(INCLUDE);
    return createBeansFor(contents, CMCSS.class);
  }
}
