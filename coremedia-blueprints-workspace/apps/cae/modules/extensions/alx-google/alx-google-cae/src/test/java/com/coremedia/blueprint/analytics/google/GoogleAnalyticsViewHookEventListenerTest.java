package com.coremedia.blueprint.analytics.google;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import static com.coremedia.blueprint.analytics.google.GoogleAnalyticsViewHookEventListener.HEAD_VIEW_NAME;
import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_HEAD;
import static com.coremedia.blueprint.analytics.google.GoogleAnalytics.GOOGLE_ANALYTICS_SERVICE_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAnalyticsViewHookEventListenerTest {
  @InjectMocks
  private GoogleAnalyticsViewHookEventListener listener = new GoogleAnalyticsViewHookEventListener();

  @Mock
  private Page page;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Content content;

  @Test
  public void onViewHook() {
    ViewHookEvent<Page> event = new ViewHookEvent<>(page, VIEW_HOOK_HEAD, Collections.<String, Object>emptyMap());
    Map<String, Object> map = new HashMap<>();
    map.put("webPropertyId", "UA-12345-678");
    when(settingsService.settingAsMap(GOOGLE_ANALYTICS_SERVICE_KEY, String.class, Object.class, page)).thenReturn(map);
    when(page.getContent()).thenReturn(content);
    RenderNode renderNode = listener.onViewHook(event);

    assertNotNull(renderNode.getBean());
    assertEquals(HEAD_VIEW_NAME, renderNode.getView());
    assertEquals(content, ((GoogleAnalytics)renderNode.getBean()).getContent());
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
    assertEquals(GoogleAnalyticsViewHookEventListener.DEFAULT_ORDER, order);
  }
}