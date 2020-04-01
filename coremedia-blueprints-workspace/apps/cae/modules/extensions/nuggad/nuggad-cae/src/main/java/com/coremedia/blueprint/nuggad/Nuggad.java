package com.coremedia.blueprint.nuggad;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.util.ExtensionsAspectUtil;

public class Nuggad {
  private Page page;
  private SettingsService settingsService;

  public Nuggad(Page page, SettingsService settingsService) {
    this.page = page;
    this.settingsService = settingsService;
  }

  public boolean isEnabled() {
    return settingsService.settingWithDefault("nuggad.enabled", Boolean.class, false, page.getNavigation())
            && ExtensionsAspectUtil.isFeatureConfigured(getNuggn()) && ExtensionsAspectUtil.isFeatureConfigured(getNuggsid());
  }

  public String getNuggn() {
    return settingsService.settingWithDefault("nuggad.nuggn" + ExtensionsAspectUtil.EXTERNAL_ACCOUNT, String.class, "", page.getNavigation());
  }

  public String getNuggsid() {
    return settingsService.settingWithDefault("nuggad.nuggsid" + ExtensionsAspectUtil.EXTERNAL_ACCOUNT, String.class, "", page.getNavigation());
  }
}
