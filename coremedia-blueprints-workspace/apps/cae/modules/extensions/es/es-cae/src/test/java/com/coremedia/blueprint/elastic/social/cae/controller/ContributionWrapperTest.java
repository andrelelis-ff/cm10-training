package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.elastic.social.api.comments.Comment;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ContributionWrapperTest {

  @Test
  public void testWrapper() {
    ContributionWrapper<Comment, CommentWrapper> subContributionWrapper1 = mock(ContributionWrapper.class);
    ContributionWrapper<Comment, CommentWrapper> subContributionWrapper2 = mock(ContributionWrapper.class);
    List<ContributionWrapper<Comment, CommentWrapper>> subContributionWrappers = ImmutableList.of(subContributionWrapper1, subContributionWrapper2);

    Comment comment = mock(Comment.class);

    ContributionWrapper<Comment, ContributionWrapper<Comment, CommentWrapper>> wrapper = new ContributionWrapper<>(comment, subContributionWrappers);

    assertEquals(comment, wrapper.getContribution());
    assertEquals(2, wrapper.getSubContributions().size());

    Comment comment2 = mock(Comment.class);
    wrapper.setContribution(comment2);
    assertEquals(comment2, wrapper.getContribution());
  }
}
