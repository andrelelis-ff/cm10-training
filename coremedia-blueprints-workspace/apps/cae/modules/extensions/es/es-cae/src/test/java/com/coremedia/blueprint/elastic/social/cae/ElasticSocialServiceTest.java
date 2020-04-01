package com.coremedia.blueprint.elastic.social.cae;

import com.coremedia.blueprint.base.elastic.common.CategoryExtractor;
import com.coremedia.blueprint.base.elastic.social.common.ContributionTargetHelper;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.elastic.social.cae.controller.CommentWrapper;
import com.coremedia.blueprint.elastic.social.cae.controller.CommentsResult;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.elastic.core.api.SortOrder;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.staging.StagingService;
import com.coremedia.elastic.core.cms.ContentWithSite;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.ModerationType;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.comments.SortHelper;
import com.coremedia.elastic.social.api.ratings.Like;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.Rating;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.ratings.ShareService;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.elastic.social.impl.comments.CommentServiceImpl;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.coremedia.blueprint.elastic.social.cae.ElasticSocialService.COMMENT_FETCH_LIMIT;
import static com.coremedia.blueprint.elastic.social.cae.ElasticSocialService.REVIEW_FETCH_LIMIT;
import static com.coremedia.elastic.core.api.SortOrder.ASCENDING;
import static com.coremedia.elastic.core.api.users.UserService.USERS_COLLECTION;
import static com.coremedia.elastic.core.test.Injection.inject;
import static com.coremedia.elastic.social.api.comments.CommentService.COMMENTS_COLLECTION;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSocialServiceTest {
  @InjectMocks
  private ElasticSocialService elasticSocialService;

  @Mock
  private CategoryExtractor categoryExtractor;

  @Mock
  private RatingService ratingService;

  @Mock
  private ContributionTargetHelper contributionTargetHelper;

  @Mock
  private CMTeasable target;

  @Mock
  private Content content;

  @Mock
  private ContentWithSite contentWithSite;

  @Mock
  private Rating rating;

  @Mock
  private CommunityUser communityUser;

  @Mock
  private CommunityUser targetCommunityUser;

  @Mock
  private CMNavigation navigation;

  @Mock
  private CommunityUserService communityUserService;

  @Mock
  private CommentService commentService;

  @Mock
  private ReviewService reviewService;

  @Mock
  private Comment comment1;

  @Mock
  private Comment comment2;

  @Mock
  private Review review;

  @Mock
  private CMTeasable teasable;

  @Mock
  private LikeService likeService;

  @Mock
  private Like like;

  @Mock
  private StagingService stagingService;

  @Mock
  private ShareService shareService;

  @Mock
  private Site site;

  @Mock
  private Blob blob;

  @Before
  public void setUp() {
    String id1 = "4711";
    String id2 = "42";

    long timeMillies = System.currentTimeMillis();
    when(comment1.getCreationDate()).thenReturn(new Date(timeMillies));
    when(comment2.getCreationDate()).thenReturn(new Date(timeMillies - 1000));
    when(comment1.getListToRoot()).thenReturn(asList(comment1));
    when(comment2.getListToRoot()).thenReturn(asList(comment2));

    when(comment1.getId()).thenReturn(id1);
    when(comment1.getCollection()).thenReturn(COMMENTS_COLLECTION);
    when(commentService.getComment(id1)).thenReturn(comment1);

    when(targetCommunityUser.getId()).thenReturn(id2);
    when(targetCommunityUser.getCollection()).thenReturn(USERS_COLLECTION);
    when(communityUserService.getUserById(id2)).thenReturn(targetCommunityUser);

    when(contributionTargetHelper.getTarget(teasable)).thenReturn(contentWithSite);
    when(contributionTargetHelper.getContentFromTarget(contentWithSite)).thenReturn(content);
  }

  @Test
  public void testGetAverageReviewRating() {
    when(reviewService.getAverageRating(any(ContentWithSite.class))).thenReturn(4.7);
    when(contributionTargetHelper.getTarget(target)).thenReturn(contentWithSite);

    double result = elasticSocialService.getAverageReviewRating(target);

    assertEquals(4.7, result, 0.0);
  }

  @Test
  public void testGetNumberOfReviews() {
    long numberOfReviews = 4l;
    when(reviewService.getNumberOfReviews(any(ContentWithSite.class))).thenReturn(numberOfReviews);
    when(contributionTargetHelper.getTarget(target)).thenReturn(contentWithSite);

    long result = elasticSocialService.getNumberOfReviews(target);

    assertEquals(numberOfReviews, result);
  }

  @Test
  public void testGetAverageRating() {
    double averageRating = 4.7;
    when(ratingService.getAverageRating(any(ContentWithSite.class))).thenReturn(averageRating);
    when(contributionTargetHelper.getTarget(target)).thenReturn(contentWithSite);

    double result = elasticSocialService.getAverageRating(target);

    assertEquals(averageRating, result, 0.0);
  }

  @Test
  public void testGetRatingForUser() {
    int value = 7;
    when(rating.getValue()).thenReturn(value);
    when(ratingService.getRatingForUser(eq(communityUser), any())).thenReturn(rating);
    inject(elasticSocialService, ratingService);

    int result = elasticSocialService.getRating(communityUser, target);

    assertEquals(value, result);
  }

  @Test
  public void testGetNumberOfRatings() {
    when(ratingService.getNumberOfRatings(any())).thenReturn(7L);
    when(contributionTargetHelper.getTarget(target)).thenReturn(contentWithSite);

    long result = elasticSocialService.getNumberOfRatings(target);

    assertEquals(7L, result);
    verify(ratingService).getNumberOfRatings(any(ContentWithSite.class));
  }

  @Test
  public void updateRating() {
    elasticSocialService.updateRating(communityUser, teasable, navigation, 1);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = RuntimeException.class)
  public void updateRatingWithException() {
    doThrow(new RuntimeException("intended")).when(ratingService).updateRating(eq(communityUser), any(ContentWithSite.class), any(Collection.class), eq(1));
    elasticSocialService.updateRating(communityUser, teasable, navigation, 1);
  }

  @Test
  public void testGetNumberOfLikes() {
    when(likeService.getNumberOfLikes(any(ContentWithSite.class))).thenReturn(7L);
    when(contributionTargetHelper.getTarget(target)).thenReturn(contentWithSite);

    long result = elasticSocialService.getNumberOfLikes(target);

    assertEquals(7L, result);
  }

  @Test
  public void hasLiked() {
    when(likeService.getLikeForUser(eq(communityUser), any(ContentWithSite.class))).thenReturn(like);
    assertTrue(elasticSocialService.hasLiked(communityUser, teasable));
  }

  @Test
  public void updateLike() {
    elasticSocialService.updateLike(communityUser, teasable, navigation, true);
  }

  @Test
  public void testGetNumberOfComments() {
    when(commentService.getNumberOfComments(any(ContentWithSite.class))).thenReturn(7L);
    when(contributionTargetHelper.getTarget(target)).thenReturn(contentWithSite);

    long result = elasticSocialService.getNumberOfComments(target);

    assertEquals(7L, result);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = RuntimeException.class)
  public void updateLikeWithException() {
    doThrow(new RuntimeException("intended")).when(likeService).updateLike(eq(communityUser), any(ContentWithSite.class), any(Collection.class), eq(true));
    elasticSocialService.updateLike(communityUser, teasable, navigation, true);
  }

  @Test
  public void testGetOnlineOrOwnCommentsWithReplyTos() {
    long timeMillies = System.currentTimeMillis();

    Comment root1 = mock(Comment.class);
    Comment replyTo1 = mock(Comment.class);
    Comment replyTo1_1 = mock(Comment.class);
    Comment root2 = mock(Comment.class);
    Comment replyTo2 = mock(Comment.class);

    when(root1.getCreationDate()).thenReturn(new Date(timeMillies - 5000));
    when(root2.getCreationDate()).thenReturn(new Date(timeMillies - 4000));

    when(root1.getListToRoot()).thenReturn(asList(root1));
    when(root2.getListToRoot()).thenReturn(asList(root2));
    when(replyTo1.getListToRoot()).thenReturn(asList(replyTo1, root1)).thenReturn(asList(replyTo1, root1)).thenReturn(asList(replyTo1, root1)).thenReturn(asList(replyTo1, root1)).thenReturn(asList(replyTo1, root1));
    when(replyTo1_1.getListToRoot()).thenReturn(asList(replyTo1_1, replyTo1, root1)).thenReturn(asList(replyTo1_1, replyTo1, root1)).thenReturn(asList(replyTo1_1, replyTo1, root1)).thenReturn(asList(replyTo1_1, replyTo1, root1)).thenReturn(asList(replyTo1_1, replyTo1, root1));
    when(replyTo2.getListToRoot()).thenReturn(asList(replyTo2, root2)).thenReturn(asList(replyTo2, root2)).thenReturn(asList(replyTo2, root2)).thenReturn(asList(replyTo2, root2)).thenReturn(asList(replyTo2, root2));

    CommentServiceImpl commentServiceImpl = mock(CommentServiceImpl.class);

    inject(elasticSocialService, commentServiceImpl);
    when(commentServiceImpl.getOnlineComments(any(ContentWithSite.class), isNull(), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(new LinkedList<>(asList(root1, replyTo1, replyTo1_1)));
    when(commentServiceImpl.getComments(any(ContentWithSite.class), eq(communityUser), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(asList(root2, replyTo2));
    doCallRealMethod().when(commentServiceImpl).sortThreadedDiscussion(any(), eq(ASCENDING));

    List<Comment> result = elasticSocialService.getOnlineOrOwnComments(teasable, communityUser);

    assertEquals(asList(root1, replyTo1, replyTo1_1, root2, replyTo2), result);
  }

  @Test
  public void testGetOnlineOrOwnCommentsWithDuplicates() {
    CommentServiceImpl commentServiceImpl = mock(CommentServiceImpl.class);

    inject(elasticSocialService, commentServiceImpl);
    when(commentServiceImpl.getOnlineComments(any(ContentWithSite.class), isNull(), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(new LinkedList<>(asList(comment1, comment2)));
    when(commentServiceImpl.getComments(any(ContentWithSite.class), eq(communityUser), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(asList(comment2));
    doCallRealMethod().when(commentServiceImpl).sortThreadedDiscussion(any(), eq(ASCENDING));

    List<Comment> result = elasticSocialService.getOnlineOrOwnComments(teasable, communityUser);

    assertEquals(asList(comment2, comment1), result);
    verify(commentServiceImpl).getOnlineComments(any(ContentWithSite.class), isNull(), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT));
    verify(commentServiceImpl).getComments(any(ContentWithSite.class), eq(communityUser), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT));
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testCreateComment() {
    String text = "Horst rulez!";
    when(commentService.createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull())).thenReturn(comment1);
    when(categoryExtractor.getCategories(any(Content.class), nullable(Content.class))).thenReturn(singleton("test"));
    when(contributionTargetHelper.getSite(contentWithSite)).thenReturn(site);
    when(site.getLocale()).thenReturn(Locale.GERMAN);

    Comment result = elasticSocialService.createComment(communityUser, "Horst", teasable, null, text, ModerationType.NONE, null, null);

    assertSame(comment1, result);
    verify(commentService).createComment(eq(communityUser), eq(text), eq(contentWithSite), eq(singleton("test")), isNull());
    verify(comment1).setLocale(Locale.GERMAN);
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testCreateCommentIgnored() {
    when(categoryExtractor.getCategories(any(Content.class), nullable(Content.class))).thenReturn(singleton("test"));
    String text = "Horst rulez!";
    when(commentService.createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull()))
            .thenReturn(comment1);

    Comment result = elasticSocialService.createComment(communityUser, "Horst", teasable, null, text, ModerationType.NONE, null, null);

    assertSame(comment1, result);
    verify(comment1, never()).save();
    verify(commentService).save(comment1, ModerationType.NONE);
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testCreateReview() {
    String text = "Horst rulez!";
    String title = "test";
    int rating = 4;
    when(reviewService.createReview(eq(communityUser), eq(text), any(ContentWithSite.class), anyList(), eq(title), eq(rating))).thenReturn(review);
    when(contributionTargetHelper.getSite(any(ContentWithSite.class))).thenReturn(site);
    when(site.getLocale()).thenReturn(Locale.GERMAN);

    Review result = elasticSocialService.createReview(communityUser, teasable, text, title, rating, ModerationType.POST_MODERATION, null, navigation);

    assertSame(review, result);
    verify(reviewService).createReview(eq(communityUser), eq(text), any(ContentWithSite.class), anyList(), eq(title), eq(rating));
    verify(review).setLocale(Locale.GERMAN);
    verify(reviewService).save(review, ModerationType.POST_MODERATION);

  }

  @Test
  public void testGetReview() {
    Review review = mock(Review.class);
    when(reviewService.getReviewForUser(eq(communityUser), any(ContentWithSite.class))).thenReturn(review);

    Review result = elasticSocialService.getReview(communityUser, teasable);

    assertEquals(review, result);
    verify(reviewService).getReviewForUser(eq(communityUser), any(ContentWithSite.class));
  }

  @SuppressWarnings({"unchecked"})
  @Test (expected = IllegalArgumentException.class)
  public void testCreateCommentBlocked() {
    when(communityUser.isBlocked()).thenReturn(true);
    String text = "Horst rulez!";

    elasticSocialService.createComment(communityUser, "Horst", teasable, null, text, ModerationType.NONE, null, null);

    verify(commentService, never()).createComment(communityUser, text, teasable, singleton("test"), null);
    verify(comment1, never()).save();
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testCreateCommentPostModerated() {
    when(categoryExtractor.getCategories(any(Content.class), nullable(Content.class))).thenReturn(singleton("test"));
    String text = "Horst rulez!";
    when(commentService.createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull()))
            .thenReturn(comment1);

    Comment result = elasticSocialService.createComment(communityUser, "Horst", teasable, null, text, ModerationType.POST_MODERATION, null, null);

    assertSame(comment1, result);
    verify(commentService).createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull());
    verify(commentService).save(comment1, ModerationType.POST_MODERATION);
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testCreateCommentPreModerated() {
    when(categoryExtractor.getCategories(any(Content.class), nullable(Content.class))).thenReturn(singleton("test"));
    String text = "Horst rulez!";
    when(commentService.createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull()))
            .thenReturn(comment1);

    Comment result = elasticSocialService.createComment(communityUser, "Horst", teasable, navigation, text, ModerationType.PRE_MODERATION, null, null);

    assertSame(comment1, result);
    verify(commentService).createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull());
    verify(commentService).save(comment1, ModerationType.PRE_MODERATION);
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testCreateCommentWithBlob() {
    String text = "Horst rulez!";
    when(commentService.createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull()))
            .thenReturn(comment1);
    when(categoryExtractor.getCategories(any(Content.class), nullable(Content.class))).thenReturn(singleton("test"));

    Comment result = elasticSocialService.createComment(communityUser, "Horst", teasable, null, text, ModerationType.NONE, null, ImmutableList.of(blob));

    assertSame(comment1, result);
    verify(commentService).createComment(eq(communityUser), eq(text), any(ContentWithSite.class), eq(singleton("test")), isNull());
    result.setAttachments(ImmutableList.of(blob));
  }

  @Test
  public void addComplaint() {
    elasticSocialService.updateComplaint(communityUser, comment1, true);
    verify(commentService).addComplaint(communityUser, comment1);
  }

  @Test
  public void removeComplaint() {
    elasticSocialService.updateComplaint(communityUser, comment1, false);
    verify(commentService).removeComplaint(communityUser, comment1);
  }

  @Test
  public void addComplaintForUser() {
    elasticSocialService.updateComplaint(communityUser, targetCommunityUser, true);
    verify(communityUserService).addComplaint(communityUser, targetCommunityUser);
  }

  @Test
  public void removeComplaintForUser() {
    elasticSocialService.updateComplaint(communityUser, targetCommunityUser, false);
    verify(communityUserService).removeComplaint(communityUser, targetCommunityUser);
  }

  @Test
  public void hasComplaintForUser() {
    when(communityUserService.hasComplaintForUser(communityUser, targetCommunityUser)).thenReturn(true);
    assertTrue(elasticSocialService.hasComplaint(communityUser, targetCommunityUser));
  }

  @Test
  public void hasNoComplaintForUser() {
    when(communityUserService.hasComplaintForUser(communityUser, targetCommunityUser)).thenReturn(false);
    assertFalse(elasticSocialService.hasComplaint(communityUser, targetCommunityUser));
  }

  @Test
  public void hasComplaintForUnknownCollection() {
    when(targetCommunityUser.getCollection()).thenReturn("abcd");
    assertFalse(elasticSocialService.hasComplaint(communityUser, targetCommunityUser));
  }

  @Test
  public void hasComplaintForComment() {
    when(commentService.hasComplaintForUser(communityUser, comment1)).thenReturn(true);
    assertTrue(elasticSocialService.hasComplaint(communityUser, comment1));
  }

  @Test
  public void hasNoComplaintForComment() {
    when(commentService.hasComplaintForUser(communityUser, comment1)).thenReturn(false);
    assertFalse(elasticSocialService.hasComplaint(communityUser, comment1));
  }

  @Test
  public void hasComplaintForUnknownTarget() {
    assertFalse(elasticSocialService.hasComplaint(communityUser, "unknown"));
  }

  @Test
  public void loadCommentsResultDelivery() {
    UserContext.setUser(communityUser);

    when(commentService.getOnlineComments(any(ContentWithSite.class), isNull(), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(new LinkedList<>(asList(comment1)));
    when(commentService.getComments(any(ContentWithSite.class), eq(communityUser), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(asList(comment2));
    sortThreadedDiscussion();
    when(contributionTargetHelper.getTarget(contentWithSite)).thenReturn(contentWithSite);

    CommentsResult commentsResult = new CommentsResult(contentWithSite, communityUser, elasticSocialService, true, ContributionType.REGISTERED);
    // this triggers loading of data for the given CommentsResult and internally calls com.coremedia.blueprint.elastic.social.cae.ElasticSocialService.loadCommentsResult()
    List<CommentWrapper> commentsWrappers = commentsResult.getComments();

    assertEquals(2, commentsWrappers.size());
    assertEquals(comment2, commentsWrappers.get(0).getComment());
    assertEquals(comment1, commentsWrappers.get(1).getComment());
    assertEquals(2, commentsResult.getNumberOfComments());
    verify(commentService).getOnlineComments(any(ContentWithSite.class), isNull(), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT));
  }

  private void sortThreadedDiscussion() {
    doAnswer(invocationOnMock -> {
      Object[] arguments = invocationOnMock.getArguments();
      SortHelper.sortThreadedDiscussion((List)arguments[0], (SortOrder) arguments[1]);
      return null;
    }).when(commentService).sortThreadedDiscussion(any(), eq(ASCENDING));
  }

  @Test
  public void getCommentsResultPreview() {
    elasticSocialService.setPreview(true);
    when(commentService.getCommentsForPreview(any(ContentWithSite.class), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(asList(comment1, comment2));
    sortThreadedDiscussion();
    when(contributionTargetHelper.getTarget(contentWithSite)).thenReturn(contentWithSite);

    CommentsResult commentsResult = new CommentsResult(contentWithSite, communityUser, elasticSocialService, true, ContributionType.REGISTERED);
    // this triggers loading of data for the given CommentsResult and internally calls com.coremedia.blueprint.elastic.social.cae.ElasticSocialService.loadCommentsResult()
    List<CommentWrapper> commentsWrappers = commentsResult.getComments();

    assertEquals(2, commentsResult.getNumberOfContributions());
    assertEquals(comment2, commentsWrappers.get(0).getComment());
    assertEquals(comment1, commentsWrappers.get(1).getComment());

    verify(stagingService).applyChanges(comment1);
    verify(stagingService).applyChanges(comment2);
  }



  @Test
  public void getReviewsResultForPreview() {
    List<Review> reviews = Collections.singletonList(review);
    when(reviewService.getReviewsForPreview(contentWithSite, ASCENDING, REVIEW_FETCH_LIMIT)).thenReturn(reviews);
    when(contributionTargetHelper.getTarget(contentWithSite)).thenReturn(contentWithSite);
    elasticSocialService.setPreview(true);

    List<Review> reviewsResult = elasticSocialService.getReviews(contentWithSite, communityUser);

    assertEquals(1, reviewsResult.size());
    assertEquals(review, reviewsResult.get(0));
    verify(reviewService).getReviewsForPreview(contentWithSite, ASCENDING, REVIEW_FETCH_LIMIT);
  }

  @Test
  public void getReviewsResultForLive() {
    List<Review> reviews = Collections.singletonList(review);
    when(reviewService.getOnlineReviews(target, null, ASCENDING, REVIEW_FETCH_LIMIT)).thenReturn(reviews);
    when(contributionTargetHelper.getTarget(target)).thenReturn(target);
    elasticSocialService.setPreview(false);

    List<Review> reviewsResult = elasticSocialService.getReviews(target, communityUser);

    verify(reviewService).getOnlineReviews(target, null, ASCENDING, REVIEW_FETCH_LIMIT);

    assertEquals(1, reviewsResult.size());
    assertEquals(review, reviewsResult.get(0));
  }


  @Test
  public void getNotIgnoredComments() {
    CommentServiceImpl commentServiceImpl = mock(CommentServiceImpl.class);

    inject(elasticSocialService, commentServiceImpl);
    when(commentServiceImpl.getCommentsForPreview(any(ContentWithSite.class), eq(ASCENDING), eq(COMMENT_FETCH_LIMIT)))
            .thenReturn(asList(comment1, comment2));
    doCallRealMethod().when(commentServiceImpl).sortThreadedDiscussion(any(), eq(ASCENDING));

    List<Comment> notIgnoredComments = elasticSocialService.getNotIgnoredComments(teasable);
    assertNotNull(notIgnoredComments);
    assertEquals(asList(comment2, comment1), notIgnoredComments);
    verify(stagingService).applyChanges(comment1);
    verify(stagingService).applyChanges(comment2);
  }

  @Test
  public void share() {
    elasticSocialService.share(communityUser, teasable, navigation, "provider");
    verify(shareService).updateShare(eq(communityUser), any(ContentWithSite.class), eq("provider"), eq(Collections.<String>emptyList()));
  }

}
