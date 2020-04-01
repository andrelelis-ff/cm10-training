package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.viewtype.ViewtypeService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Generated extension class for immutable beans of document type "CMViewtype".
 */
public class CMViewtypeImpl extends CMViewtypeBase {
  private ViewtypeService viewtypeService;

  @Required
  public void setViewtypeService(ViewtypeService viewtypeService) {
    this.viewtypeService = viewtypeService;
  }

  @Override
  public String getLayout() {
    return viewtypeService.getLayout(getContent());
  }
}
  