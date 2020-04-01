package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.coremedia.elastic.social.api.ContributionType.REGISTERED;
import static com.coremedia.elastic.social.api.comments.Comment.State.APPROVED;
import static com.coremedia.elastic.social.api.comments.Comment.State.REJECTED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReviewsResultTest {

  @Mock
  private ElasticSocialService elasticSocialService;

  @Mock
  private ElasticSocialConfiguration elasticSocialConfiguration;

  @Mock
  private Object target;

  @Mock
  private CommunityUser user;

  @Before
  public void setup() {
    List<Review> reviews = ImmutableList.of(createReview(3));
    when(elasticSocialService.getReviews(target, user)).thenReturn(reviews);
  }

  @Test
  public void testGetReviews() {
    List<Review> reviews = ImmutableList.of(createReview(3));
    when(elasticSocialService.getReviews(target, user)).thenReturn(reviews);

    ReviewsResult result = new ReviewsResult(target, user, elasticSocialService, true, REGISTERED, elasticSocialConfiguration);

    assertEquals(reviews, result.getReviews());
    verify(elasticSocialService).getReviews(target, user);
  }

  @Test
  public void testGetNumberOfOnlineReviews() {
    long numberOfOnlineReviews = 5;
    when(elasticSocialService.getNumberOfReviews(target)).thenReturn(numberOfOnlineReviews);

    ReviewsResult result = new ReviewsResult(target, user, elasticSocialService, true, REGISTERED, elasticSocialConfiguration);

    assertEquals(numberOfOnlineReviews, result.getNumberOfOnlineReviews());
    verify(elasticSocialService).getReviews(target, user);
  }

  @Test
  public void testGetAverageRating() {
    double averageRating = 4.0;
    when(elasticSocialService.getAverageReviewRating(target)).thenReturn(averageRating);

    ReviewsResult result = new ReviewsResult(target, user, elasticSocialService, true, REGISTERED, elasticSocialConfiguration);

    assertEquals(averageRating, result.getAverageRating(), 0.1);
    verify(elasticSocialService).getReviews(target, user);
  }


  @Test
  public void testGetNumberOfOnlineReviewsFor() {
    Review review1 = createReview(1);
    Review review2a = createReview(2);
    Review review2b = createReview(2);
    Review review3 = createReview(REJECTED, 3);

    ImmutableList<Review> reviews = ImmutableList.of(review1, review2a, review2b, review3);
    when(elasticSocialService.getReviews(target, user)).thenReturn(reviews);

    ReviewsResult result = new ReviewsResult(target, user, elasticSocialService, true, REGISTERED, elasticSocialConfiguration);

    assertEquals(1, result.getNumberOfOnlineReviewsFor(1));
    assertEquals(2, result.getNumberOfOnlineReviewsFor(2));
    assertEquals(0, result.getNumberOfOnlineReviewsFor(3));
    assertEquals(0, result.getNumberOfOnlineReviewsFor(4));
    verify(elasticSocialService).getReviews(target, user);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetRootContributions() {
    ReviewsResult result = new ReviewsResult(target, user, elasticSocialService, true, REGISTERED, elasticSocialConfiguration);
    result.getRootContributions();
  }

  private Review createReview(int rating) {
    return  createReview(APPROVED, rating);
  }

  private Review createReview(Comment.State state, int rating) {
    Review review = mock(Review.class);
    when(review.getRating()).thenReturn(rating);
    when(review.getState()).thenReturn(state);
    return  review;
  }
}
