package com.coremedia.blueprint.common.teaser;

/**
 * Settings for teasers.
 *
 * @cm.template.api
 */
public interface TeaserSettings {

  /**
   * If TRUE a link to the detail page will be rendered on the teaser.
   *
   * This setting does apply to {@link com.coremedia.blueprint.common.cta.CallToActionButtonSettings}
   *
   * @cm.template.api
   */
  boolean isRenderLinkToDetailPage();
}
