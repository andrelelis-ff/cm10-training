package com.coremedia.blueprint.studio.rest.intercept;

import com.coremedia.blueprint.themeimporter.ThemeFileUtil;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.themeimporter.ThemeImporter;
import com.coremedia.cap.themeimporter.ThemeImporterResult;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.InterceptorControlAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

/**
 * If the file to be uploaded is a theme, process it with the theme importer
 * instead of simply creating a CMDownload document.
 */
public class ThemeUploadInterceptor extends ContentWriteInterceptorBase {
  private static final Logger LOG = LoggerFactory.getLogger(ThemeUploadInterceptor.class);

  private String dataProperty;
  private ThemeImporter themeImporter;

  public ThemeUploadInterceptor(String dataProperty, ThemeImporter themeImporter) {
    this.dataProperty = dataProperty;
    this.themeImporter = themeImporter;
  }

  // --- ContentWriteInterceptor ------------------------------------

  /**
   * Checks if the uploaded file is a theme and if so, invokes the
   * theme importer
   */
  @Override
  public void intercept(ContentWriteRequest request) {
    Object value = request.getProperties().get(dataProperty);
    Blob themeBlob = null;
    try {
      themeBlob = fetchThemeBlob(value);
    } catch (Exception e) {
      // Unfortunately, this case has been observed for real world zip files
      // which java.util.zip.ZipInputStream cannot handle.
      LOG.warn("Cannot determine whether {} is a theme.  Assuming that it is not.", value, e);
    }
    if (themeBlob!=null) {
      try (InputStream is = themeBlob.getInputStream()) {
        Content targetFolder = request.getParent();
        Content homeFolder = targetFolder.getRepository().getConnection().getSession().getUser().getHomeFolder();
        boolean isProductionTheme = homeFolder == null || !targetFolder.isChildOf(homeFolder);

        // Usecase/Assumption/Motivation:
        // A production theme is checked in, versioned and ready for publication.
        // A developer theme remains checked out for subsequent changes.
        boolean checkInAfterImport = isProductionTheme;  // NOSONAR make business logic obvious
        // cleanBeforeImport is true for development themes, since we do not
        // expect frontend developers to use Studio for uploading partial themes.
        // We may be wrong here though, and change this again in a later version.
        // cleanBeforeImport is false for production themes, because an existing
        // production theme is linked and published, so that deletion would not
        // work without further ado.
        boolean cleanBeforeImport = !isProductionTheme;

        ThemeImporterResult themeImporterResult =
                themeImporter.importThemes(request.getParent().getPath(), Collections.singletonList(is), checkInAfterImport, cleanBeforeImport);
        request.setAttribute(InterceptorControlAttributes.DO_NOTHING, true);
        Set<Content> themeDescriptors = themeImporterResult.getThemeDescriptors();
        request.setAttribute(InterceptorControlAttributes.UPLOADED_DOCUMENTS, themeDescriptors);
      } catch (IOException e) {
        throw new IllegalStateException("Error closing blob input stream", e);
      }
    }
  }


  // --- internal ---------------------------------------------------

  /**
   * Check if the value is a theme blob
   *
   * @return the theme blob, or null if value is no theme blob.
   */
  private Blob fetchThemeBlob(Object value) {
    if (value instanceof Blob) {
      Blob blob = (Blob) value;
      try (InputStream inputStream = blob.getInputStream()) {
        return ThemeFileUtil.isZip(blob.getContentType()) && ThemeFileUtil.isTheme(inputStream) ? blob : null;
      } catch (IOException e) {
        throw new IllegalArgumentException("Error reading input stream", e);
      }
    }
    return null;
  }

}
