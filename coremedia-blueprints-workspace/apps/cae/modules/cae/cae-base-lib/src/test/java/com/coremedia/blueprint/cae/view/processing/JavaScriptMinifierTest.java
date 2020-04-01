package com.coremedia.blueprint.cae.view.processing;

import org.junit.Test;
import org.mozilla.javascript.EvaluatorException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests {@link JavaScriptMinifier}
 */
public class JavaScriptMinifierTest extends MinifierBaseTest {

  /**
   * Test valid JavaScript with default configuration as per the code.
   */
  @Test
  public void testValidJavaScript() throws IOException {

    JavaScriptMinifier testling = new JavaScriptMinifier();

    String processedCode = minify(testling, "valid.js");

    final String expected = "(function(){this.coremedia=window.coremedia||{};this.coremedia.blueprint=coremedia.blueprint||{};this.coremedia.blueprint.media={equalHeights:a};function a(){$(\".collection\").each(function(){$(this).find(\".teaserBox .content\").css({height:\"auto\"}).equalHeights()})}})();";

    assertEquals("Output does not match.", expected, processedCode);
  }

  /**
   * Test invalid JavaScript with default configuration. No output is the expected behaviour.
   */
  @Test(expected = EvaluatorException.class)
  public void testInvalidJavaScript() throws IOException {

    JavaScriptMinifier testling = new JavaScriptMinifier();

    minify(testling, "invalid.js");

    fail("must not reach this line.");
  }

}
