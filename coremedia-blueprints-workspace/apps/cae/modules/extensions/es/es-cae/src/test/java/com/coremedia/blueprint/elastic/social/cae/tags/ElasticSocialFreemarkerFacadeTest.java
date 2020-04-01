package com.coremedia.blueprint.elastic.social.cae.tags;

import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.blueprint.elastic.social.cae.controller.CommentsResult;
import com.coremedia.blueprint.elastic.social.cae.user.ElasticSocialUserHelper;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.objectserver.beans.ContentBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSocialFreemarkerFacadeTest {
  @InjectMocks
  private ElasticSocialFreemarkerFacade elasticSocialFreemarkerFacade = new ElasticSocialFreemarkerFacade();

  @Mock
  private ElasticSocialService elasticSocialService;

  @Mock
  private Object object;

  @Mock
  private CommunityUser communityUser;

  @Mock
  private ContentBean contentBean;

  @Mock
  private CommentsResult commentsResult;

  @Mock
  private ElasticSocialUserHelper elasticSocialUserHelper;

  @Test
  public void isLoginAction() {
    boolean isLoginAction = elasticSocialFreemarkerFacade.isLoginAction(object);
    assertFalse(isLoginAction);
  }

  @Test
  public void getCurrentUser() {
    CommunityUser currentUser = elasticSocialFreemarkerFacade.getCurrentUser();
    assertNull(currentUser);
  }

  @Test
  public void isAnonymous() {
    when(communityUser.isAnonymous()).thenReturn(true);
    boolean isAnonymous = elasticSocialFreemarkerFacade.isAnonymous(communityUser);
    assertTrue(isAnonymous);
  }

  @Test
  public void getCommentsResult() {
    CommentsResult result = elasticSocialFreemarkerFacade.getCommentsResult(contentBean);
    assertNotNull(result);
    assertEquals(contentBean, result.getTarget());
  }
}
