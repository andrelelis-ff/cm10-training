package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMSettings;
import com.coremedia.cap.struct.Struct;

/**
 * Generated base class for immutable beans of document type CMSettings.
 * Should not be changed.
 */
public abstract class CMSettingsBase extends CMLocalizedImpl implements CMSettings {

  /**
   * Returns the value of the document property "settings"
   *
   * @return the value of the document property "settings"
   */
  @Override
  public Struct getSettings() {
    Struct settings = getContent().getStruct(SETTINGS);
    return settings != null ? settings : getContent().getRepository().getConnection().getStructService().emptyStruct();
  }

  /**
   * Returns the value of the document property {@link #IDENTIFIER}.
   *
   * @return the value of the document property {@link #IDENTIFIER}
   */
  @Override
  public String getIdentifier() {
    return getContent().getString(IDENTIFIER);
  }
}
