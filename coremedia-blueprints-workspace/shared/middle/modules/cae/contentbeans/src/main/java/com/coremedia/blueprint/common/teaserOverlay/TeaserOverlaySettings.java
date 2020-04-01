package com.coremedia.blueprint.common.teaserOverlay;

import com.coremedia.blueprint.common.contentbeans.CMSettings;

/**
 * Settings for the teaser overlay feature.
 *
 * @cm.template.api
 */
public interface TeaserOverlaySettings {

  /**
   * @cm.template.api
   */
  boolean isEnabled();

  CMSettings getStyle();

  /**
   * @cm.template.api
   */
  int getPositionX();

  /**
   * @cm.template.api
   */
  int getPositionY();

  /**
   * @cm.template.api
   */
  int getWidth();
}
