package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents the document type {@link #NAME CMArticle}.
 *
 * @cm.template.api
 */
public interface CMArticle extends CMTeasable {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMArticle'.
   */
  String NAME = "CMArticle";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMArticle} object
   */
  @Override
  CMArticle getMaster();

  @Override
  Map<Locale, ? extends CMArticle> getVariantsByLocale();

  @Override
  Collection<? extends CMArticle> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMArticle>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMArticle>> getAspects();
}
