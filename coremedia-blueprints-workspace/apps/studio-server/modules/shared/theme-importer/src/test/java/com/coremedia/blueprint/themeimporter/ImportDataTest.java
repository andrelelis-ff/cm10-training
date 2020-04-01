package com.coremedia.blueprint.themeimporter;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ImportDataTest {
  @Test
  public void testReadProperties() throws IOException {
    byte[] latin1bytes = new byte[] {(byte)0xC4, (byte)0xD6,(byte)0xDC,(byte)0xE4,(byte)0xF6,(byte)0xFC,(byte)0xDF};
    try (InputStream is = new ByteArrayInputStream(latin1bytes)) {
      String actual = new ImportData(null, null).readProperties(is);
      assertEquals("ImportData#readProperties is not latin-1 aware.", "ÄÖÜäöüß", actual);
    }
  }
}
