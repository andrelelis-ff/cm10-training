package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.blueprint.common.feeds.FeedSource;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Base type for navigation items.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMNavigation}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMNavigation extends Navigation, CMTeasable, FeedSource<CMLinkable> {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMNavigation'.
   */
  String NAME = "CMNavigation";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMNavigation} object
   */
  @Override
  CMNavigation getMaster();

  @Override
  Map<Locale, ? extends CMNavigation> getVariantsByLocale();

  @Override
  Collection<? extends CMNavigation> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMNavigation>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMNavigation>> getAspects();

  /**
   * Name of the document property 'children'.
   */
  String CHILDREN = "children";

  /**
   * Name of the document property 'hidden'.
   */
  String HIDDEN = "hidden";

  /**
   * Name of the document property 'hiddenInSitemap'.
   */
  String HIDDEN_IN_SITEMAP = "hiddenInSitemap";

  /**
   * Name of the document property 'javaScript'.
   */
  String JAVA_SCRIPT = "javaScript";

  /**
   * Returns the value of the document property {@link #JAVA_SCRIPT}.
   *
   * @return a list of {@link CMJavaScript} objects
   * @cm.template.api
   */
  List<CMJavaScript> getJavaScript();

  /**
   * Name of the document property 'css'.
   */
  String CSS = "css";

  /**
   * Name of the document property 'theme'.
   */
  String THEME = "theme";

  /**
   * Returns the value of the document property {@link #CSS}.
   *
   * @return a list of {@link CMCSS} objects
   * @cm.template.api
   */
  List<CMCSS> getCss();

  /**
   * Name of the document property 'favicon'.
   */
  String FAVICON = "favicon";

  /**
   * Returns the value of the document property {@link #FAVICON}.
   *
   * @return the value of the document property {@link #FAVICON}
   * @cm.template.api
   */
  Blob getFavicon();

  /**
   * Name of the document property 'placement'.
   */
  String PLACEMENT = "placement";

  /**
   * Returns the value of the document property {@link #PLACEMENT}.
   *
   * @return the value of the document property {@link #PLACEMENT}
   */
  Struct getPlacement();
}
