package com.coremedia.blueprint.common.contentbeans;

import java.util.List;

/**
 * @cm.template.api
 */
public interface CMImageMap extends CMTeaser {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMImageMap'.
   */
  String NAME = "CMImageMap";

  /**
   * Returns a {@link java.util.List} of all specified hot zones of the image map.
   *
   * @return a list of all specified hot zones
   */
  public List<java.util.Map<String, Object>> getImageMapAreas();
}
