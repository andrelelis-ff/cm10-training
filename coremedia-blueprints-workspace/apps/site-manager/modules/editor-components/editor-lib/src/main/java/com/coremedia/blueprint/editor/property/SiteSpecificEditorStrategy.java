package com.coremedia.blueprint.editor.property;

import hox.corem.editor.Editor;
import hox.corem.editor.multisite.Site;
import hox.corem.editor.proxy.DocumentModel;
import hox.corem.editor.toolkit.Context;

public final class SiteSpecificEditorStrategy {

  public static final String PATH_SEPARATOR = "/";

  /**
   * Hide Utility Class Constructor
   */
  private SiteSpecificEditorStrategy() {
  }

  public static String getSiteSpecificFolderPath(Context context, String path) {
    if (!path.startsWith(PATH_SEPARATOR)) {
      // The path should be interpreted relative to the current site.
      Object o = context.getService("document");
      if (o instanceof DocumentModel) {
        DocumentModel documentModel = (DocumentModel) o;
        Site site = Editor.getEditor().getSitesService().getSiteFor(documentModel);
        if (site != null) {
          return site.getSiteRootFolder().getPathToRootFolder() + PATH_SEPARATOR + path;
        }
      }
      return null;
    } else {
      return path;
    }
  }
}
