package com.coremedia.blueprint.cae.view.processing;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link CssMinifier}
 */
public class CssMinifierTest extends MinifierBaseTest {

  /**
   * Test valid CSS.
   */
  @Test
  public void testValidCss() throws IOException {

    CssMinifier testling = new CssMinifier();

    String result = minify(testling, "valid.css");

    final String expected = ".my-custom-class{color:#000}#my-custom-id{background:#fff}";

    assertEquals("Output does not match.", expected, result);
  }

  /**
   * Test invalid CSS
   */
  @Test
  public void testInvalidCss() throws IOException {

    CssMinifier testling = new CssMinifier();

    String result = minify(testling, "invalid.css");

    //expected output contains broken CSS, just as the source file does.
    final String expected = ".my class{color:#000}#my-custom-id background:#fff}";

    assertEquals("Output does not match.", expected, result);
  }

}
