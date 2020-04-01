package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * The CMInteractive type adds no extra properties but leaves a pluggable spot where needed
 * properties could be attached via a DocTypeAspect.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMInteractive}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMInteractive extends CMVisual {
  String NAME = "CMInteractive";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMInteractive} object
   */
  @Override
  CMInteractive getMaster();

  @Override
  Map<Locale, ? extends CMInteractive> getVariantsByLocale();

  @Override
  Collection<? extends CMInteractive> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMInteractive>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMInteractive>> getAspects();

  /**
   * Returns the value of the document property (@link #data}
   *
   * @return the value of the document property (@link #data}
   */
  @Override
  Blob getData();
}
