package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A collection of media contents.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMGallery}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMGallery<T extends CMMedia> extends CMCollection<T> {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMCollection'.
   */
  String NAME = "CMGallery";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMCollection} object
   */
  @Override
  CMGallery<T> getMaster();

  @Override
  Map<Locale, ? extends CMGallery<T>> getVariantsByLocale();

  @Override
  Collection<? extends CMGallery<T>> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMGallery<T>>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMGallery<T>>> getAspects();
}
