package com.coremedia.blueprint.themeimporter;

import javax.activation.MimeType;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThemeFileUtil {

  // static class
  private ThemeFileUtil() {}

  /**
   * Check whether the given MIME type describes a zip file.
   *
   * @param mimeType the MIME type
   */
  public static boolean isZip(MimeType mimeType) {
    String primaryType = mimeType.getPrimaryType();
    String subType = mimeType.getSubType();
    return "application".equals(primaryType) &&
            ("zip".equals(subType) || "x-zip".equals(subType) || "x-zip-compressed".equals(subType));
  }

  /**
   * Check whether the input stream encodes a theme.
   * At the end of the check, the input stream is closed.
   * <p>
   * We assume that the blob is a theme if it is a zip and it contains a
   * THEME-METADATA directory.
   * <p>
   * This is of course not bullet proof, but in the context of a generic file
   * upload we cannot be absolutely sure.
   *
   * @param is a zip input stream
   */
  public static boolean isTheme(InputStream is) {
    try (ZipInputStream zis = new ZipInputStream(is)) {
      for (ZipEntry entry = zis.getNextEntry(); entry!=null; entry=zis.getNextEntry()) {
        // Directories do not necessarily appear as separate zip entries, but
        // only implicitly with a longer path.  (Observed for our corporate
        // theme, same with "unzip -l" command.)  Therefore we check with
        // startsWith.
        if (entry.getName().startsWith(ThemeImporterImpl.THEME_METADATA_DIR + "/")) {
          return true;
        }
      }
      return false;
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading input stream", e);
    }
  }
}
