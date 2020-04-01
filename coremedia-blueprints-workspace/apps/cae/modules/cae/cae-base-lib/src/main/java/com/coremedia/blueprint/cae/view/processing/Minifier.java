package com.coremedia.blueprint.cae.view.processing;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * This interface can be used to implement minification and/or obfuscation for Javascript, CSS etc.
 */
public interface Minifier {

  void minify(Writer output, Reader input, String name) throws IOException;

}
