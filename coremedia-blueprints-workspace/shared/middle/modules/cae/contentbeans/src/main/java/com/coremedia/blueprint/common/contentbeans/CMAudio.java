package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.blueprint.common.player.PlayerSettings;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * The CMAudio type splits the media hierarchy to audio components.
 * </p>
 * <p>
 * It provides the audio data as a blob property of mime type audio.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMAudio}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMAudio extends CMMedia {

  String NAME = "CMAudio";

  /**
   * Name of the player settings struct.
   */
  String PLAYER_SETTINGS = "playerSettings";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMAudio} object
   */
  @Override
  CMAudio getMaster();

  @Override
  Map<Locale, ? extends CMAudio> getVariantsByLocale();

  @Override
  Collection<? extends CMAudio> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMAudio>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMAudio>> getAspects();

  /**
   * Name of the document property 'dataUrl'.
   */
  String DATA_URL = "dataUrl";

  /**
   * Returns the value of the document property (@link #data}
   *
   * @return the value of the document property (@link #data}
   */
  @Override
  Blob getData();

  /**
   * Returns the value of the document property (@link #dataUrl}
   *
   * @return the value of the document property (@link #dataUrl}
   * @cm.template.api
   */
  String getDataUrl();

  /**
   * @cm.template.api
   */
  PlayerSettings getPlayerSettings();
}
