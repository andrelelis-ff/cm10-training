package com.coremedia.blueprint.lc.test;

import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Simple Wrapper to disable hoverly extension for tests against the real system.
 * see {@link HoverflyTestHelper#ignoreTapes}
 */
public class SwitchableHoverflyExtension extends HoverflyExtension {

  @Override
  public void beforeEach(ExtensionContext context) {
    if (!HoverflyTestHelper.useTapes()){
      return;
    }
    super.beforeEach(context);
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!HoverflyTestHelper.useTapes()){
      return;
    }
    super.beforeAll(context);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    if (!HoverflyTestHelper.useTapes()){
      return;
    }
    super.afterAll(context);
  }
}
