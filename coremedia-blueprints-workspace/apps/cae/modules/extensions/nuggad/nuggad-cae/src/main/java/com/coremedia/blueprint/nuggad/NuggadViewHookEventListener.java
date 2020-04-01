package com.coremedia.blueprint.nuggad;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import com.coremedia.objectserver.view.events.ViewHookEventListener;

import javax.inject.Inject;
import javax.inject.Named;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_HEAD;

@Named
public class NuggadViewHookEventListener implements ViewHookEventListener<Page> {

  @Inject
  private SettingsService settingsService;

  @Override
  public RenderNode onViewHook(ViewHookEvent<Page> event) {
    if (VIEW_HOOK_HEAD.equals(event.getId())) {
      Nuggad nuggad = new Nuggad(event.getBean(), settingsService);
      if (nuggad.isEnabled()) {
        return new RenderNode(nuggad, VIEW_HOOK_HEAD);
      }
    }
    return null;
  }

  @Override
  public int getOrder() {
    return DEFAULT_ORDER;
  }
}
