package com.coremedia.blueprint.common.player;

/**
 * Settings for the teaser overlay feature.
 *
 * @cm.template.api
 */
public interface PlayerSettings {

  /**
   * @cm.template.api
   */
  boolean isMuted();

  /**
   * @cm.template.api
   */
  boolean isAutoplay();

  /**
   * @cm.template.api
   */
  boolean isHideControls();

  /**
   * @cm.template.api
   */
  boolean isLoop();
}
