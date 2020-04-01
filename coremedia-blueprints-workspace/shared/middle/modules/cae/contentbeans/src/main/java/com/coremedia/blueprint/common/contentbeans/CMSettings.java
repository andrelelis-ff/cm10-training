package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cap.struct.Struct;

/**
 * <p>
 * The Blueprint uses settings for various purposes.  They are available by
 * CMSettings beans.
 * </p>
 * <p>
 * Represents document type {@link #NAME CMSettings}.
 * </p>
 */
public interface CMSettings extends CMLocalized {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSettings'.
   */
  String NAME = "CMSettings";

  /**
   * Name of the document property 'settings'.
   */
  String SETTINGS = "settings";

  /**
   * Returns the value of the document property "settings"
   *
   * @return the value of the document property "settings"
   */
  Struct getSettings();

  /**
   * Name of the document property 'identifier'.
   */
  String IDENTIFIER = "identifier";

  /**
   * Returns the value of the document property {@link #IDENTIFIER}.
   *
   * @return the value of the document property {@link #IDENTIFIER}
   */
  String getIdentifier();
}
