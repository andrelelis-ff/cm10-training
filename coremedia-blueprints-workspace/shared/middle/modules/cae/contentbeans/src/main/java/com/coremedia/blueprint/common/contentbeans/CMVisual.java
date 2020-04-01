package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * The CMVisual type splits the media hierarchy to visual components. It adds visibility properties
 * like width and height.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMVisual}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMVisual extends CMMedia {

  String NAME = "CMVisual";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMVisual} object
   */
  @Override
  CMVisual getMaster();

  @Override
  Map<Locale, ? extends CMVisual> getVariantsByLocale();

  @Override
  Collection<? extends CMVisual> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMVisual>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMVisual>> getAspects();

  /**
   * Name of the document property 'width'.
   */
  String WIDTH = "width";
  /**
   * Name of the document property 'height'.
   */
  String HEIGHT = "height";

  /**
   * Name of the document property 'dataUrl'.
   */
  String DATA_URL = "dataUrl";

  /**
   * Returns the value of the document property {@link #WIDTH}.
   *
   * @return the value of the document property {@link #WIDTH}
   */
  Integer getWidth();

  /**
   * Returns the value of the document property {@link #HEIGHT}.
   *
   * @return the value of the document property {@link #HEIGHT}
   */
  Integer getHeight();

  /**
   * Returns the value of the document property (@link #dataUrl}
   *
   * @return the value of the document property (@link #dataUrl}
   * @cm.template.api
   */
  String getDataUrl();
}
