package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A technical image, e.g. for usage in stylesheets.
 * </p>
 * <p>
 * Not for editorial usage. Use CMPicture instead. In order to avoid confusion,
 * you should withdraw all rights on doctype CMImage from ordinary editors.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMImage}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMImage extends CMLocalized {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMImage'.
   */
  String NAME = "CMImage";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMImage} object
   */
  @Override
  CMImage getMaster();

  @Override
  Map<Locale, ? extends CMImage> getVariantsByLocale();

  @Override
  Collection<? extends CMImage> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMImage>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMImage>> getAspects();

  /**
   * Name of the document property 'data'.
   */
  String DATA = "data";

  /**
   * Returns the value of the document property {@link #DATA}.
   *
   * @return the value of the document property {@link #DATA}
   */
  Blob getData();

  /**
   * Name of the document property 'description'.
   */
  String DESCRIPTION = "description";

  /**
   * Returns the value of the document property {@link #DESCRIPTION}.
   *
   * @return the value of the document property {@link #DESCRIPTION}
   */
  String getDescription();
}
