package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.coremedia.elastic.social.api.ContributionType.ANONYMOUS;
import static com.coremedia.elastic.social.api.ContributionType.DISABLED;
import static com.coremedia.elastic.social.api.ContributionType.READONLY;
import static com.coremedia.elastic.social.api.ContributionType.REGISTERED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentsResultTest {

  private CommentsResult commentsResult;

  @Mock
  private ElasticSocialService elasticSocialService;

  @Mock
  private CommunityUser user;

  private Object target = new Object();

  private List<Comment> comments;

  @Before
  public void setup() {
    comments = ImmutableList.of(mock(Comment.class));

    when(elasticSocialService.getComments(target, user)).thenReturn(comments);
  }

  @Test
  public void test() {
    ContributionType contributionType = READONLY;

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, contributionType);

    assertEquals(target, commentsResult.getTarget());
    assertEquals(user, commentsResult.getUser());
    assertEquals(true, commentsResult.isEnabled());
    assertEquals(true, commentsResult.isReadOnly());
    assertEquals(contributionType, commentsResult.getContributionType());
  }

  @Test
  public void testSimple() {
    commentsResult = new CommentsResult(target);

    assertEquals(target, commentsResult.getTarget());
  }

  @Test
  public void testGetComments() {
    commentsResult = new CommentsResult(target, user, elasticSocialService, true, READONLY);

    List<CommentWrapper> wrappers = commentsResult.getComments();
    assertEquals(comments.size(), wrappers.size());
    assertEquals(comments.size(), commentsResult.getNumberOfComments());
    verify(elasticSocialService).getComments(target, user);
  }

  @Test
  public void testSettingsNoUserWithCommentTypeDisabled() {
    Object target = new Object();

    commentsResult = new CommentsResult(target, null, elasticSocialService, true, DISABLED);
    validateInput(false, false, false, false, false, false);
  }


  @Test
  public void testSettingsAnonymousUserWithCommentTypeDisabled() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, DISABLED);
    validateInput(false, false, false, false, false, false);
  }

  @Test
  public void testSettingsRegisteredUserWithCommentTypeDisabled() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, DISABLED);
    validateInput(false, false, false, false, false, false);
  }

  @Test
  public void testSettingsNoUserWithCommentTypeAnonymous() {
    Object target = new Object();

    commentsResult = new CommentsResult(target, null, elasticSocialService, true, ANONYMOUS);
    validateInput(true, false, true, true, true, true);
  }

  @Test
  public void testSettingsAnonymousUserWithCommentTypeAnonymous() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, ANONYMOUS);
    validateInput(true, false, true, true, true, true);
  }

  @Test
  public void testSettingsRegisteredUserWithCommentTypeAnonymous() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, ANONYMOUS);
    validateInput(true, false, true, true, true, true);
  }

  @Test
  public void testSettingsNoUserWithCommentTypeRegistered() {
    Object target = new Object();

    commentsResult = new CommentsResult(target, null, elasticSocialService, true, REGISTERED);
    validateInput(true, false, true, false, false, false);
  }

  @Test
  public void testSettingsAnonymousUserWithCommentTypeRegistered() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);
    when(user.isAnonymous()).thenReturn(true);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, REGISTERED);
    validateInput(true, false, true, false, false, false);
  }

  @Test
  public void testSettingsRegisteredUserWithCommentTestRegistered() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);
    when(user.isAnonymous()).thenReturn(false);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, REGISTERED);
    validateInput(true, false, true, false, true, true);
  }

  @Test
  public void testSettingsRegisteredUserWithCommentTestRegisteredDisabled() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, false, REGISTERED);
    validateInput(false, false, true, false, false, false);
    List<CommentWrapper> comments = commentsResult.getComments();
    assertEquals(true, comments.isEmpty());
  }

  @Test
  public void testSettingsRegisteredUserWithCommentTestReadOnly() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, READONLY);
    validateInput(true, true, false, false, false, false);
    List<CommentWrapper> comments = commentsResult.getComments();
    assertEquals(true, comments.isEmpty());
  }

  @Test
  public void testSettingsAnonymousUserWithCommentTestReadOnly() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, READONLY);
    validateInput(true, true, false, false, false, false);
    List<CommentWrapper> comments = commentsResult.getComments();
    assertEquals(true, comments.isEmpty());
  }

  @Test
  public void testSettingsRegisteredUserWithCommentTestReadOnlyDisabled() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, false, READONLY);
    validateInput(false, false, false, false, false, false);
    List<CommentWrapper> comments = commentsResult.getComments();
    assertEquals(true, comments.isEmpty());
  }

  @Test
  public void testSettingsAnonymousUserWithCommentTestReadOnlyDisabled() {
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, false, READONLY);
    validateInput(false, false, false, false, false, false);
    List<CommentWrapper> comments = commentsResult.getComments();
    assertEquals(true, comments.isEmpty());
  }  
  
  @Test
  public void testRootComments() {
    Comment comment1 = mock(Comment.class);
    Comment comment2 = mock(Comment.class);
    Comment comment3 = mock(Comment.class);
    when(comment2.getReplyTo()).thenReturn(comment1);

    List<Comment> comments = ImmutableList.of(comment1, comment2, comment3);
    Object target = new Object();
    CommunityUser user = mock(CommunityUser.class);

    commentsResult = new CommentsResult(target, user, elasticSocialService, true, REGISTERED);
    when(elasticSocialService.getComments(target, user)).thenReturn(comments);

    List<CommentWrapper> rootComments = commentsResult.getRootComments();

    assertEquals(2, rootComments.size());
    verify(elasticSocialService).getComments(target, user);
  }

  private void validateInput(boolean enabled, boolean readOnly, boolean writingEnabled, boolean anonymousEnabled, boolean writingAllowed, boolean writingAllowedForUser) {
    assertEquals(enabled, commentsResult.isEnabled());
    assertEquals(readOnly, commentsResult.isReadOnly());
    assertEquals(writingEnabled, commentsResult.isWritingContributionsEnabled());
    assertEquals(anonymousEnabled, commentsResult.isAnonymousContributingEnabled());
    assertEquals(writingAllowed, commentsResult.isWritingContributionsAllowed());
    assertEquals(writingAllowedForUser, commentsResult.isWritingAllowedForUser());
  }

}
