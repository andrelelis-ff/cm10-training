package com.coremedia.blueprint.cae.settings;

import com.coremedia.blueprint.base.settings.SettingsFinder;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.Page;

public class PageSettingsFinder implements SettingsFinder {
  @Override
  public Object setting(Object bean, String name, SettingsService settingsService) {
    if (!(bean instanceof Page)) {
      return null;
    }
    Page page = (Page) bean;
    Object content = page.getContent();
    CMContext context = page.getContext();
    if (context!=null && context.equals(content)) {
      // Avoid duplicate settings computation for the same content.
      context = null;
    }
    return settingsService.setting(name, Object.class, content, context);
  }
}
