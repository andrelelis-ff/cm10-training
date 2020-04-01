package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import com.coremedia.objectserver.view.events.ViewHookEventListener;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Arrays;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_HEAD;

@Named
public class GuidViewHookEventListener implements ViewHookEventListener<Page> {

  static final String VIEW_NAME = "asGuidCookieLink";

  @Inject
  private SettingsService settingsService;

  @Override
  public RenderNode onViewHook(ViewHookEvent<Page> event) {
    if (VIEW_HOOK_HEAD.equals(event.getId())) {
      Page bean = event.getBean();

      Boolean esOn = settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, bean);

      if (esOn != null && esOn) {
        return new RenderNode(bean, VIEW_NAME);
      }
    }
    return null;
  }

  @Override
  public int getOrder() {
    return DEFAULT_ORDER;
  }
}
