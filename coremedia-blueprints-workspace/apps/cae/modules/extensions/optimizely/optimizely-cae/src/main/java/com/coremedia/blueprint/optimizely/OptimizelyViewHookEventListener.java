package com.coremedia.blueprint.optimizely;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import com.coremedia.objectserver.view.events.ViewHookEventListener;

import javax.inject.Inject;
import javax.inject.Named;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_HEAD;

@Named
public class OptimizelyViewHookEventListener implements ViewHookEventListener<Page> {

  @Inject
  private SettingsService settingsService;

  @Override
  public RenderNode onViewHook(ViewHookEvent<Page> event) {
    if (VIEW_HOOK_HEAD.equals(event.getId())) {
      Optimizely optimizely = new Optimizely(event.getBean(), settingsService);
      if (optimizely.isEnabled()) {
        return new RenderNode(optimizely, VIEW_HOOK_HEAD);
      }
    }
    return null;
  }

  @Override
  public int getOrder() {
    return DEFAULT_ORDER;
  }
}
