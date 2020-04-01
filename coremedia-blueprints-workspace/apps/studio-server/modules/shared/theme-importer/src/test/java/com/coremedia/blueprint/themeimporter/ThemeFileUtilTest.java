package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.common.Blob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThemeFileUtilTest {
  @Mock
  private Blob blob;


  // --- Tests ------------------------------------------------------

  @Test
  public void testIsZip() throws MimeTypeParseException {
    assertTrue(ThemeFileUtil.isZip(new MimeType("application/zip")));
  }

  @Test
  public void testIsNoZip() throws MimeTypeParseException {
    assertFalse(ThemeFileUtil.isZip(new MimeType("image/png")));
  }

  @Test
  public void testIsTheme() {
    URL theme = ThemeFileUtilTest.class.getClassLoader().getResource("com/coremedia/blueprint/themeimporter/theme.zip");
    try (InputStream is = theme.openStream()) {
      assertTrue(ThemeFileUtil.isTheme(is));
    } catch (IOException e) {
      throw new RuntimeException("Test resource hassle, not a product problem.", e);
    }
  }

  @Test
  public void testIsNoTheme() {
    URL noTheme = ThemeFileUtilTest.class.getClassLoader().getResource("com/coremedia/blueprint/themeimporter/notheme.zip");
    try (InputStream is = noTheme.openStream()) {
      assertFalse(ThemeFileUtil.isTheme(is));
    } catch (IOException e) {
      throw new RuntimeException("Test resource hassle, not a product problem.", e);
    }
  }


  // --- internal ---------------------------------------------------

  private void initBlob(String mimetype, InputStream is) {
    try {
      when(blob.getContentType()).thenReturn(new MimeType(mimetype));
      when(blob.getInputStream()).thenReturn(is);
    } catch (MimeTypeParseException e) {
      throw new RuntimeException("Test setup error, not a product problem.");
    }
  }
}
