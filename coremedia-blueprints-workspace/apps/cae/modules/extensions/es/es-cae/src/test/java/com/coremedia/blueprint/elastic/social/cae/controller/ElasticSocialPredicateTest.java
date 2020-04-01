package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.objectserver.view.RenderNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ElasticSocialPredicateTest {

  private ElasticSocialPredicate predicate = new ElasticSocialPredicate();


  @Test
  public void withReviewsResult() {
    ReviewsResult reviewsResult = mock(ReviewsResult.class);
    RenderNode renderNode = new RenderNode(reviewsResult, null);

    boolean apply = predicate.apply(renderNode);

    assertTrue(apply);
  }

  @Test
  public void withCommentsResult() {
    CommentsResult commentsResult = mock(CommentsResult.class);
    RenderNode renderNode = new RenderNode(commentsResult, null);

    boolean apply = predicate.apply(renderNode);

    assertTrue(apply);
  }

  @Test
  public void withLikeResult() {
    LikeResult likeResult = mock(LikeResult.class);
    RenderNode renderNode = new RenderNode(likeResult, null);

    boolean apply = predicate.apply(renderNode);

    assertTrue(apply);
  }

  @Test
  public void withComplaintResult() {
    ComplaintResult complaintResult = mock(ComplaintResult.class);
    RenderNode renderNode = new RenderNode(complaintResult, null);

    boolean apply = predicate.apply(renderNode);

    assertTrue(apply);
  }

  @Test
  public void withoutReviewsResult() {
    Object target = new Object();
    RenderNode renderNode = new RenderNode(target, null);

    boolean apply = predicate.apply(renderNode);

    assertFalse(apply);
  }
}
