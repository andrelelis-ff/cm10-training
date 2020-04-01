package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_HEAD;
import static com.coremedia.blueprint.elastic.social.cae.view.GuidViewHookEventListener.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GuidViewHookEventListenerTest {

  @InjectMocks
  private GuidViewHookEventListener listener = new GuidViewHookEventListener();

  @Mock
  private Page page;

  @Mock
  private SettingsService settingsService;

  @Test
  public void onViewHook() {
    ViewHookEvent<Page> event = new ViewHookEvent<>(page, VIEW_HOOK_HEAD, Collections.<String, Object>emptyMap());
    when(settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, page)).thenReturn(true);
    RenderNode renderNode = listener.onViewHook(event);

    assertNotNull(renderNode.getBean());
    assertEquals(page, renderNode.getBean());
    assertEquals(VIEW_NAME, renderNode.getView());
  }

  @Test
  public void onViewHookESoff() {
    ViewHookEvent<Page> event = new ViewHookEvent<>(page, VIEW_HOOK_HEAD, Collections.<String, Object>emptyMap());
    when(settingsService.nestedSetting(Arrays.asList("elasticSocial", "enabled"), Boolean.class, page)).thenReturn(false);
    RenderNode renderNode = listener.onViewHook(event);

    assertNull(renderNode);
  }

  @Test
  public void onViewHookNoHeadEvent() {
    ViewHookEvent<Page> event = new ViewHookEvent<>(page, "xyz", Collections.<String, Object>emptyMap());
    RenderNode renderNode = listener.onViewHook(event);
    assertNull(renderNode);
  }

  @Test
  public void getOrder() {
    int order = listener.getOrder();
    assertEquals(GuidViewHookEventListener.DEFAULT_ORDER, order);
  }

}
