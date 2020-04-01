package com.coremedia.blueprint.elastic.social.cae;

import com.coremedia.blueprint.elastic.social.cae.controller.CommentWrapper;
import com.coremedia.elastic.social.api.comments.Comment;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WrapperHelperTest {

  @InjectMocks
  private WrapperHelper wrapperHelper;

  @Test
  public void wrapCommentWithSubComment() {
    Comment comment = mock(Comment.class);
    Comment subComment = mock(Comment.class);
    when(subComment.getReplyTo()).thenReturn(comment);
    ImmutableList<Comment> comments = ImmutableList.of(comment, subComment);

    List<CommentWrapper> commentWrappers = wrapperHelper.getCommentWrappers(comments);

    assertEquals(2, commentWrappers.size());
    assertEquals(comment, commentWrappers.get(0).getComment());
    List<CommentWrapper> subCommentsWrapper = commentWrappers.get(0).getSubComments();
    assertEquals(1, subCommentsWrapper.size());
    assertEquals(subComment, subCommentsWrapper.get(0).getComment());
  }

}