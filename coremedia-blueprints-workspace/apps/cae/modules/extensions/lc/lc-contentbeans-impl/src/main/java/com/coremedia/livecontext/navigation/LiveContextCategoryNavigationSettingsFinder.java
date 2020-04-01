package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.base.settings.SettingsFinder;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.livecontext.context.LiveContextNavigation;

/**
 * Categories inherit settings from their contexts only.
 */
public class LiveContextCategoryNavigationSettingsFinder implements SettingsFinder {

  @Override
  public Object setting(Object bean, final String name, final SettingsService settingsService) {
    if (!(bean instanceof LiveContextCategoryNavigation)) {
      return null;
    }

    final LiveContextCategoryNavigation lcn = (LiveContextCategoryNavigation) bean;
    CMContext context = lcn.getContext();
    return settingsService.setting(name, Object.class, context);
  }

}
