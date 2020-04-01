package com.coremedia.blueprint.cae.contentbeans;


/**
 * Generated extension class for beans of document type "CMAction".
 */
public class CMActionImpl extends CMActionBase {
  private static final String FLOW_TYPE = "webflow";

  @Override
  public boolean isWebFlow() {
    return FLOW_TYPE.equals(getType());
  }
}