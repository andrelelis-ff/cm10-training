package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.cae.aspect.Aspect;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * <p>
 * CMCSS beans provide static CSS resources with a media attribute and
 * a dependency list with other CSS documents.
 * </p>
 * <p>
 * Represents document type {@link #NAME CMCSS}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMCSS extends CMAbstractCode {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMCSS'.
   */
  String NAME = "CMCSS";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMCSS} object
   */
  @Override
  CMCSS getMaster();

  @Override
  Map<Locale, ? extends CMCSS> getVariantsByLocale();

  @Override
  Collection<? extends CMCSS> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMCSS>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMCSS>> getAspects();

  /**
   * Name of the document property 'media'.
   */
  String MEDIA = "media";

  /**
   * Returns the value of the document property {@link #MEDIA}.
   *
   * @return the value of the document property {@link #MEDIA}
   * @cm.template.api
   */
  String getMedia();

  /**
   * Returns the value of the document property {@link #INCLUDE}.
   *
   * @return a list of {@link CMCSS} objects
   */
  @Override
  @NonNull
  List<? extends CMCSS> getInclude();
}
