package com.coremedia.blueprint.assets.cae;

import com.coremedia.objectserver.view.substitution.Substitution;

/**
 * This Singleton represents the entry point to the single page download portal application.
 *
 * @cm.template.api
 */
public class DownloadPortal {

  private static final String PLACEHOLDER_ID = "am-download-portal";

  /**
   * Substitutes placeholders with the ID "am-download-portal" with the Download portal entry point represented
   * by the {@link DownloadPortal} singleton.
   *
   * @return the singleton instance of the {@link DownloadPortal} and never <code>null</code>
   */
  @SuppressWarnings("UnusedDeclaration")
  @Substitution(PLACEHOLDER_ID)
  public DownloadPortal getDownloadPortal() {
    return this;
  }

}
