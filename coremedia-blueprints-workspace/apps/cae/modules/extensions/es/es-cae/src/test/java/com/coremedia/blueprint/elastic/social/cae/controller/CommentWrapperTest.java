package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.elastic.social.api.comments.Comment;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CommentWrapperTest {

  @Test
  public void testWrapper() {
    CommentWrapper subCommentWrapper1 = mock(CommentWrapper.class);
    CommentWrapper subCommentWrapper2 = mock(CommentWrapper.class);
    List<CommentWrapper> subCommentWrappers = ImmutableList.of(subCommentWrapper1, subCommentWrapper2);

    Comment comment = mock(Comment.class);

    CommentWrapper wrapper = new CommentWrapper(comment, subCommentWrappers);

    assertEquals(comment, wrapper.getComment());
    assertEquals(2, wrapper.getSubComments().size());
  }
}
