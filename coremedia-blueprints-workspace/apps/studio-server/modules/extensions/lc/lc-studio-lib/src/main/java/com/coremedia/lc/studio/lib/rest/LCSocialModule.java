package com.coremedia.lc.studio.lib.rest;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class LCSocialModule extends SimpleModule {

  @Inject
  private ProductInSiteJsonSerializer productInSiteJsonSerializer;

  public LCSocialModule() {
    super("LCSocial", new Version(1, 0, 0, null, null, null));
  }

  @Override
  public void setupModule(final SetupContext context) {
    addSerializer(productInSiteJsonSerializer);
    super.setupModule(context);
  }
}
