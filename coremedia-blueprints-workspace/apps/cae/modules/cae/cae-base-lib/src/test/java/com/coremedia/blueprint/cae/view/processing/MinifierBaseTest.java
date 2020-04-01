package com.coremedia.blueprint.cae.view.processing;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Abstract Superclass of Tests that test implementations of the {@link Minifier} interface.
 */
public abstract class MinifierBaseTest {

  /**
   * Process file with given {@link Minifier} implementation.
   *
   * @return processed String
   */
  protected String minify(Minifier minifier, String filename) throws IOException {

    InputStream inputStream = getClass().getResourceAsStream(filename);
    Reader reader = new InputStreamReader(inputStream);

    Writer writer = new StringWriter();

    minifier.minify(writer, reader, filename);

    return writer.toString();
  }

}
