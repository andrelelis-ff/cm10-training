package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMCSS;
import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTheme;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.user.User;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Generated base class for immutable beans of document type CMNavigation.
 * Should not be changed.
 */
public abstract class CMNavigationBase extends CMTeasableImpl implements CMNavigation {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMNavigation} objects
   */
  @Override
  public CMNavigation getMaster() {
    return (CMNavigation) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMNavigation> getVariantsByLocale() {
    return getVariantsByLocale(CMNavigation.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMNavigation> getLocalizations() {
    return (Collection<? extends CMNavigation>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMNavigation>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMNavigation>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMNavigation>> getAspects() {
    return (List<? extends Aspect<? extends CMNavigation>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #CHILDREN}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMLinkable} objects
   */
  @Override
  public List<? extends Linkable> getChildren() {
    List<Content> contents = getContent().getLinks(CHILDREN);
    return createBeansFor(contents, CMNavigation.class);
  }

  /**
   * Returns the value of the document property {@link #HIDDEN}.
   *
   * @return the value of the document property {@link #HIDDEN}
   */
  @Override
  public boolean isHidden() {
    return getContent().getBoolean(HIDDEN);
  }

  /**
   * Returns the value of the document property {@link #HIDDEN_IN_SITEMAP}.
   *
   * @return the value of the document property {@link #HIDDEN_IN_SITEMAP}
   */
  @Override
  public boolean isHiddenInSitemap() {
    return getContent().getBoolean(HIDDEN_IN_SITEMAP);
  }

  /**
   * Returns the value of the document property {@link #JAVA_SCRIPT}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMJavaScript} objects
   */
  @Override
  public List<CMJavaScript> getJavaScript() {
    List<Content> contents = getContent().getLinks(JAVA_SCRIPT);
    return createBeansFor(contents, CMJavaScript.class);
  }

  /**
   * Returns the value of the document property {@link #CSS}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMCSS} objects
   */
  @Override
  public List<CMCSS> getCss() {
    List<Content> contents = getContent().getLinks(CSS);
    return createBeansFor(contents, CMCSS.class);
  }

  /**
   * Returns the value of the document property {@link #THEME}.
   *
   * @return the value of the document property {@link #THEME}
   */
  @Override
  public CMTheme getTheme(@Nullable User developer) {
    return createBeanFor(getContent().getLink(THEME), CMTheme.class);
  }

  /**
   * Returns the value of the document property {@link #FAVICON}.
   *
   * @return the value of the document property {@link #FAVICON}
   */
  @Override
  public Blob getFavicon() {
    return getContent().getBlobRef(FAVICON);
  }

  /**
   * Returns the value of the document property {@link #PLACEMENT}.
   *
   * @return the value of the document property {@link #PLACEMENT}
   */
  @Override
  public Struct getPlacement() {
    return getContent().getStruct(PLACEMENT);
  }
}
  
