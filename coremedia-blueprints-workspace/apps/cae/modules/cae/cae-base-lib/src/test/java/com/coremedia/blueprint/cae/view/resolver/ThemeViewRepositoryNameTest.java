package com.coremedia.blueprint.cae.view.resolver;

import com.coremedia.blueprint.cae.view.resolver.ThemeTemplateViewRepositoryProvider.ThemeViewRepositoryName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ThemeViewRepositoryNameTest {
  @Test
  public void parseStandardName() {
    ThemeViewRepositoryName tvrn = new ThemeViewRepositoryName(null, "theme::42/bricks");
    assertEquals(-1, tvrn.developerId);
    assertEquals(42, tvrn.themeId);
    assertEquals("bricks", tvrn.viewRepositoryName);
    assertNull(tvrn.developer());
  }

  @Test
  public void parseDeveloperName() {
    ThemeViewRepositoryName tvrn = new ThemeViewRepositoryName(null, "theme:10:42/bricks");
    assertEquals(10, tvrn.developerId);
    assertEquals(42, tvrn.themeId);
    assertEquals("bricks", tvrn.viewRepositoryName);
  }
}
