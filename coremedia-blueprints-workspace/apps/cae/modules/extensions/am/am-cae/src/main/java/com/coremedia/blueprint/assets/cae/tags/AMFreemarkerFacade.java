package com.coremedia.blueprint.assets.cae.tags;

import com.coremedia.blueprint.assets.cae.AMUtils;
import com.coremedia.blueprint.assets.cae.DownloadPortal;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.web.FreemarkerEnvironment;
import org.springframework.beans.factory.annotation.Required;

public class AMFreemarkerFacade {

  private SettingsService settingsService;

  private DownloadPortal downloadPortal;

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setDownloadPortal(DownloadPortal downloadPortal) {
    this.downloadPortal = downloadPortal;
  }

  public DownloadPortal getDownloadPortal() {
    return downloadPortal;
  }

  public boolean hasDownloadPortal() {
    return SiteHelper.findSite(FreemarkerEnvironment.getCurrentRequest())
            .map(site -> AMUtils.getDownloadPortalRootDocument(settingsService, site))
            .isPresent();
  }
}
