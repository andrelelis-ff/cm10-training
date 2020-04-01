package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMContext;

/**
 * Generated extension class for immutable beans of document type "CMDownload".
 */
public class CMDownloadImpl extends CMDownloadBase {

  public static final String SETTING_USE_CM_DOWNLOAD_FILENAME = "useCMDownloadFilename";
  public static final boolean DEFAULT_USE_CM_DOWNLOAD_FILENAME = false;

  @Override
  public String getFilename() {
    String filename = super.getFilename();
    CMContext context = getCurrentContextService().getContext();
    // if the method is called from CapBlobHandler.handleRequest() the context is null
    // thus the setting cannot be checked
    if (context == null) {
      return filename;
    }
    // we're probably building a link right now (because the context is available)
    // thus the setting can be checked
    SettingsService settingsService = getSettingsService();
    Boolean useCMDownloadFilename = settingsService.settingWithDefault(
            SETTING_USE_CM_DOWNLOAD_FILENAME,
            Boolean.class, DEFAULT_USE_CM_DOWNLOAD_FILENAME,
            this, context);

    return useCMDownloadFilename != null && useCMDownloadFilename
            ? filename
            : null;
  }
}
