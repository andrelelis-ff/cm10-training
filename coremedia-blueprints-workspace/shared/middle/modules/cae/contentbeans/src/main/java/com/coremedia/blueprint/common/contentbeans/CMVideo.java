package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.blueprint.common.player.PlayerSettings;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * The CMVideo adds no extra properties but leaves a pluggable spot where needed properties could be
 * attached via a DocTypeAspect.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMVideo}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMVideo extends CMVisual {

  String NAME = "CMVideo";

  /**
   * Name of the document property 'timeLine'.
   */
  String TIMELINE = "timeLine";

  /**
   * Name of the player settings struct.
   */
  String PLAYER_SETTINGS = "playerSettings";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMVisual} object
   */
  @Override
  CMVisual getMaster();

  @Override
  Map<Locale, ? extends CMVideo> getVariantsByLocale();

  @Override
  Collection<? extends CMVideo> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMVideo>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMVideo>> getAspects();

  /**
   * Returns the value of the document property (@link #data}
   *
   * @return the value of the document property (@link #data}
   */
  @Override
  Blob getData();

  /**
   * @cm.template.api
   */
  Struct getTimeLine();

  /**
   * @cm.template.api
   */
  CMTeasable getTimeLineDefaultTarget();

  /**
   * @cm.template.api
   */
  List getTimeLineSequences();

  /**
   * @cm.template.api
   */
  PlayerSettings getPlayerSettings();
}
