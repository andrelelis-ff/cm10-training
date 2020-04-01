package com.coremedia.blueprint.cae.view.processing;

import com.yahoo.platform.yui.compressor.CssCompressor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A {@link Minifier} implementation for CSS. Uses the YUI-Compressor to minify given CSS
 */
public class CssMinifier implements Minifier {

  private int linebreak = -1;

  // --- Spring Config -------------------------------------------------------------------------------------------------

  public void setLinebreak(int linebreak) {
    this.linebreak = linebreak;
  }

  public int getLinebreak() {
    return linebreak;
  }

  // --- Functionality -------------------------------------------------------------------------------------------------

  /**
   * Reads a Stylesheet from the given Input stream, and writes a compressed version to the output.
   * <p/>
   * Make sure that - if the CSS comes com the @link com.coremedia.blueprint.common.contentbeans.CMAbstractCode
   * Class or similar - the Markup has already been removed from the code.
   *
   * @param output the output
   * @param input  the input
   */
  @Override
  public void minify(Writer output, Reader input, String name) throws IOException {
    CssCompressor compressor = new CssCompressor(input);
    compressor.compress(output, linebreak);
  }

}
