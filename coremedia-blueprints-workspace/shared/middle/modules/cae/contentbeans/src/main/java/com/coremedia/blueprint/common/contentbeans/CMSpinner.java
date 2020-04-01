package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A 360 degrees spinner.
 * </p>
 * <p>
 * Represents the document type CMSpinner.
 * </p>
 *
 * @cm.template.api
 */
public interface CMSpinner extends CMVisual {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSpinner'.
   */
  String NAME = "CMSpinner";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMSpinner} object
   */
  @Override
  CMSpinner getMaster();

  @Override
  Map<Locale, ? extends CMSpinner> getVariantsByLocale();

  @Override
  Collection<? extends CMSpinner> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMSpinner>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMSpinner>> getAspects();

  /**
   * The name of the document property that holds the image sequence
   * of the spinner.
   */
  String SEQUENCE = "sequence";

  /**
   * Returns the images of the spinner, from left to right.
   *
   * @cm.template.api
   */
  @NonNull
  List<CMPicture> getSequence();
}
