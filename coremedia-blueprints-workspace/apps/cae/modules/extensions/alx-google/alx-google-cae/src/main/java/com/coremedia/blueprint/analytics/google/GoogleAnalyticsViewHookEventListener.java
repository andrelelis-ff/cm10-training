package com.coremedia.blueprint.analytics.google;


import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import com.coremedia.objectserver.view.events.ViewHookEventListener;

import javax.inject.Inject;
import javax.inject.Named;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_HEAD;

@Named
public class GoogleAnalyticsViewHookEventListener implements ViewHookEventListener<Page> {

  public static final String HEAD_VIEW_NAME = "head";

  @Inject
  private SettingsService settingsService;

  @Override
  public RenderNode onViewHook(ViewHookEvent<Page> event) {
    if (VIEW_HOOK_HEAD.equals(event.getId())) {
      GoogleAnalytics googleAnalytics = new GoogleAnalytics(event.getBean(), settingsService);
      if (googleAnalytics.isEnabled()) {
        return new RenderNode(googleAnalytics, HEAD_VIEW_NAME);
      }
    }
    return null;
  }

  @Override
  public int getOrder() {
    return DEFAULT_ORDER;
  }
}
