package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <p>
 * CMExternalLink enhances an external URL with the Teasable features.
 * </p>
 * <p>
 * Represents document type {@link #NAME CMExternalLink}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMExternalLink extends CMTeasable {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMExternalLink'.
   */
  String NAME = "CMExternalLink";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMExternalLink} object
   */
  @Override
  CMExternalLink getMaster();

  @Override
  Map<Locale, ? extends CMExternalLink> getVariantsByLocale();

  @Override
  Collection<? extends CMExternalLink> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMExternalLink>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMExternalLink>> getAspects();

  /**
   * Name of the document property 'url'.
   */
  String URL = "url";

  /**
   * Returns the value of the document property {@link #URL}.
   *
   * @return the value of the document property {@link #URL}
   * @cm.template.api
   */
  String getUrl();

  /**
   * Name of the document property 'openInNewTab'
   */
  String OPEN_IN_NEW_TAB = "openInNewTab";

  /**
   * Checks whether the {@link #getUrl() target url} is opening in a new tab.
   *
   * @return <code>true</code> if the link should open in a new tab otherwise <code>false</code>
   * @cm.template.api
   */
  @Override
  boolean isOpenInNewTab();
}
