package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated extension class for immutable beans of document type "CMDownload".
 */
public class CMDownloadImpl extends CMDownloadBase {

  public static final String SETTING_USE_CM_DOWNLOAD_FILENAME = "useCMDownloadFilename";
  public static final boolean DEFAULT_USE_CM_DOWNLOAD_FILENAME = false;

  @Override
  public String getFilename() {
    String filename = super.getFilename();
    SettingsService settingsService = getSettingsService();
    Boolean useCMDownloadFilename = settingsService.settingWithDefault(
            SETTING_USE_CM_DOWNLOAD_FILENAME,
            Boolean.class, DEFAULT_USE_CM_DOWNLOAD_FILENAME,
            getNavigationContexts(getContexts()));

    return useCMDownloadFilename != null && useCMDownloadFilename
            ? filename
            : null;
  }

  private Object[] getNavigationContexts(List<CMContext> contexts) {
    List<Object> navigationContexts = new ArrayList<>();
    // add this because searching for the setting in the download itself at first
    navigationContexts.add(this);
    if (!CollectionUtils.isEmpty(contexts)) {
      for (CMContext context : contexts) {
        List<? extends Linkable> navigationPathList = context.getNavigationPathList();
        // reverse list vor correct channel hierarchy from download to root page
        navigationContexts.addAll(Lists.reverse(navigationPathList));
      }
    }
    return navigationContexts.toArray();
  }
}
