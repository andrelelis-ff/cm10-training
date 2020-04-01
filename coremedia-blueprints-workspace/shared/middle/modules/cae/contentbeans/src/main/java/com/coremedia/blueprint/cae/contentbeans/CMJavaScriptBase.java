package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMJavaScript.
 * Should not be changed.
 */
public abstract class CMJavaScriptBase extends CMAbstractCodeImpl implements CMJavaScript {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMJavaScript} objects
   */
  @Override
  public CMJavaScript getMaster() {
    return (CMJavaScript) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMJavaScript> getVariantsByLocale() {
    return getVariantsByLocale(CMJavaScript.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMJavaScript> getLocalizations() {
    return (Collection<? extends CMJavaScript>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMJavaScript>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMJavaScript>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMJavaScript>> getAspects() {
    return (List<? extends Aspect<? extends CMJavaScript>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #INCLUDE}.
   *
   * @return a list of {@link CMJavaScript} objects
   */
  @Override
  @NonNull
  public List<? extends CMJavaScript> getInclude() {
    List<Content> contents = getContent().getLinks(INCLUDE);
    return createBeansFor(contents, CMJavaScript.class);
  }
}
