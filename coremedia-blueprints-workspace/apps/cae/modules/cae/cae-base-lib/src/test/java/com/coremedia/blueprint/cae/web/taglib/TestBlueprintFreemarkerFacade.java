package com.coremedia.blueprint.cae.web.taglib;

/**
 * makes methods accessible for testing
 */
public class TestBlueprintFreemarkerFacade extends BlueprintFreemarkerFacade {

  // overridden to make method public
  @Override
  public boolean isMetadataEnabled() {
    return super.isMetadataEnabled();
  }
}
