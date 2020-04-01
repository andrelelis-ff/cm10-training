package com.coremedia.blueprint.common.cta;

import java.util.List;

/**
 * Settings for the CallToAction-Button feature.
 *
 * @cm.template.api
 */
public interface CallToActionButtonSettings {

  /**
   * @return the target for the link of the Call-To-Action Button
   * @cm.template.api
   */
  Object getTarget();

  /**
   * @return the text to show on the Call-To-Action Button
   * @cm.template.api
   */
  String getText();

  /**
   * @return TRUE if the link should be opened in a new tab otherwise FALSE.
   * @cm.template.api
   */
  boolean isOpenInNewTab();

  /**
   * @return the metadata to attach for the PBE.
   * @cm.template.api
   */
  List<String> getMetadata();
}
