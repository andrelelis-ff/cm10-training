package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.CMVideo;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.elastic.social.cae.controller.ContributionResult;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.objectserver.view.RenderNode;
import com.coremedia.objectserver.view.events.ViewHookEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static com.coremedia.blueprint.base.cae.web.taglib.ViewHookEventNames.VIEW_HOOK_END;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CommentsViewHookEventListenerTest {

  @InjectMocks
  @Spy
  private CommentsViewHookEventListener viewHookEventListener = new CommentsViewHookEventListener();

  @Mock
  private ElasticSocialPlugin elasticSocialPlugin;

  @Mock
  private ContextHelper contextHelper;

  @Mock
  private ElasticSocialConfiguration elasticSocialConfiguration;

  @Mock
  private CMArticle article;

  @Before
  public void setUp() {
    Content articleContent = mock(Content.class);
    ContentType articleType = mock(ContentType.class);
    when(article.getContent()).thenReturn(articleContent);
    when(articleContent.getType()).thenReturn(articleType);
    when(articleType.getName()).thenReturn(CMArticle.NAME);

    when(contextHelper.contextFor(article)).thenReturn(null);
    when(elasticSocialPlugin.getElasticSocialConfiguration(article)).thenReturn(elasticSocialConfiguration);
  }

  @Test
  public void onViewHook_featureDisabled_contributionNotCalled() {
    ViewHookEvent<CMTeasable> event = new ViewHookEvent<>(article, VIEW_HOOK_END, Collections.emptyMap());

    when(elasticSocialConfiguration.isCommentingEnabled()).thenReturn(false);
    when(elasticSocialConfiguration.getCommentDocumentTypes()).thenReturn(null);

    RenderNode renderNode = viewHookEventListener.onViewHook(event);

    verify(viewHookEventListener, never()).getContribution(article);
    assertNull(renderNode);
  }

  @Test
  public void onViewHook_commentsEnabled_returnContribution() {
    ViewHookEvent<CMTeasable> event = new ViewHookEvent<>(article, VIEW_HOOK_END, Collections.emptyMap());

    when(elasticSocialConfiguration.isCommentingEnabled()).thenReturn(true);
    when(elasticSocialConfiguration.getCommentDocumentTypes()).thenReturn(null);

    RenderNode renderNode = viewHookEventListener.onViewHook(event);

    assertTrue("Expecting a contribution result", renderNode.getBean() instanceof ContributionResult);
  }

  @Test
  public void onViewHook_onlySupportsVideos_contributionNotCalled() {
    ViewHookEvent<CMTeasable> event = new ViewHookEvent<>(article, VIEW_HOOK_END, Collections.emptyMap());

    when(elasticSocialConfiguration.isCommentingEnabled()).thenReturn(true);
    when(elasticSocialConfiguration.getCommentDocumentTypes()).thenReturn(Collections.singletonList(CMVideo.NAME));

    RenderNode renderNode = viewHookEventListener.onViewHook(event);

    verify(viewHookEventListener, never()).getContribution(article);
    assertNull(renderNode);
  }
  @Test
  public void onViewHook_eventIdNotSupported_contributionNotCalled() {
    ViewHookEvent<CMTeasable> event = new ViewHookEvent<>(article, "anotherId", Collections.emptyMap());

    RenderNode renderNode = viewHookEventListener.onViewHook(event);

    verifyZeroInteractions(elasticSocialConfiguration);
    assertNull(renderNode);
  }

}