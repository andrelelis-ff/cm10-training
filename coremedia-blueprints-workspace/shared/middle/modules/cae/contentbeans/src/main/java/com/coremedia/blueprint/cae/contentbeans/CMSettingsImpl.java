package com.coremedia.blueprint.cae.contentbeans;

import org.apache.commons.lang3.StringUtils;

/**
 * Generated extension class for immutable beans of document type "CMSettings".
 */
public class CMSettingsImpl extends CMSettingsBase {

  @Override
  public String getIdentifier() {
    String identifier = super.getIdentifier();
    if (StringUtils.isBlank(identifier)) {
      identifier = getContent().getName();
    }
    return identifier;
  }
}
