package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;

/**
 * Generated extension class for immutable beans of document type "CMSite".
 */
public class CMSiteImpl extends CMSiteBase {

  @Override
  public CMNavigation getRoot() {
    return getValidationService().validate(super.getRoot()) ? super.getRoot() : null;
  }
}
