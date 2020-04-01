package com.coremedia.blueprint.elastic.social.cae;

import com.coremedia.blueprint.base.elastic.common.CategoryExtractor;
import com.coremedia.blueprint.base.elastic.social.common.ContributionTargetHelper;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.models.Model;
import com.coremedia.elastic.core.api.staging.StagingService;
import com.coremedia.elastic.social.api.ModerationType;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.ratings.Like;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.Rating;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.ratings.ShareService;
import com.coremedia.elastic.social.api.reviews.DuplicateReviewException;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.objectserver.beans.AbstractContentBean;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.coremedia.elastic.core.api.SortOrder.ASCENDING;
import static com.coremedia.elastic.core.api.users.UserService.USERS_COLLECTION;
import static com.coremedia.elastic.social.api.comments.CommentService.COMMENTS_COLLECTION;

/**
 * Service to access the social functionality provided by Elastic Social.
 * The service accepts {@link Object}s as target parameters and will
 * internally decide on how to handle it. For example a {@link com.coremedia.blueprint.common.contentbeans.Page} might
 * not be the direct target but the main content of that navigation.
 */
@Named
public class ElasticSocialService {

  private static final Logger LOG = LoggerFactory.getLogger(ElasticSocialService.class);

  @VisibleForTesting
  static final int COMMENT_FETCH_LIMIT = 1000;
  @VisibleForTesting
  static final int REVIEW_FETCH_LIMIT = 10000;

  @Inject
  private CommentService commentService;

  @Inject
  private ReviewService reviewService;

  @Inject
  private RatingService ratingService;

  @Inject
  private LikeService likeService;

  // used for complaining about users
  @Inject
  private CommunityUserService communityUserService;

  @Inject
  private CategoryExtractor categoryExtractor;

  @Inject
  private ShareService shareService;

  // used for preview which includes moderation changes
  @Inject
  private StagingService stagingService;

  @Inject
  private ContributionTargetHelper contributionTargetHelper;

  private boolean preview;

  /**
   * Retrieves the sorted list of all {@link Comment}s on a given target <tt>CMTeasable</tt>.
   * Only {@link Comment}s, which are online or have been authored by the given {@link CommunityUser}
   * will be returned.
   *
   * @param target the target of the comments
   * @param author the {@link CommunityUser} of unapproved {@link Comment}s to include in the result
   * @return a list of online {@link Comment}s and those waiting for approval written by the given {@link CommunityUser}
   */
  @NonNull
  public List<Comment> getOnlineOrOwnComments(@NonNull Object target, @Nullable CommunityUser author) {
    Object realTarget = contributionTargetHelper.getTarget(target);
    List<Comment> onlineComments = commentService.getOnlineComments(contributionTargetHelper.getTarget(target), null, ASCENDING, COMMENT_FETCH_LIMIT);
    if (author != null) {
      List<Comment> ownComments = commentService.getComments(realTarget, author, ASCENDING, COMMENT_FETCH_LIMIT);
      onlineComments.addAll(ownComments);
      Set<Comment> allComments = new HashSet<>(onlineComments);
      onlineComments = new ArrayList<>(allComments);
      commentService.sortThreadedDiscussion(onlineComments, ASCENDING);
    }
    return onlineComments.size() > COMMENT_FETCH_LIMIT ? onlineComments.subList(0, COMMENT_FETCH_LIMIT) : onlineComments;
  }

  public List<Review> getOnlineOrOwnReviews(@NonNull Object target, @Nullable CommunityUser author) {
    Object realTarget = contributionTargetHelper.getTarget(target);

    List<Review> onlineReviews = reviewService.getOnlineReviews(contributionTargetHelper.getTarget(target), null, ASCENDING, REVIEW_FETCH_LIMIT);
    if (author != null) {
      List<Review> ownReviews = reviewService.getReviews(realTarget, author, ASCENDING, REVIEW_FETCH_LIMIT);
      onlineReviews.addAll(ownReviews);
      // make sure no review is in the result twice
      Set<Review> allReviews = new HashSet<>(onlineReviews);
      onlineReviews = new ArrayList<>(allReviews);
    }
    return onlineReviews.size() > COMMENT_FETCH_LIMIT ? onlineReviews.subList(0, REVIEW_FETCH_LIMIT) : onlineReviews;
  }


  /**
   * Retrieves the sorted list of all {@link Comment}s on a given target.
   * Only {@link Comment}s, which are not ignored will be returned.
   * This method should only be used in the Preview.
   *
   * @param commentTarget the target for the comments
   * @return a list of online {@link Comment}s
   */
  @NonNull
  public List<Comment> getNotIgnoredComments(@NonNull Object commentTarget) {
    Object target = contributionTargetHelper.getTarget(commentTarget);
    List<Comment> comments = commentService.getCommentsForPreview(target, ASCENDING, COMMENT_FETCH_LIMIT);
    commentService.sortThreadedDiscussion(comments, ASCENDING);
    for (Comment comment : comments) {
      stagingService.applyChanges(comment);
    }
    return comments;
  }

  public List<Comment> getComments(Object target, CommunityUser user) {
    Object realTarget = contributionTargetHelper.getTarget(target);
    List<Comment> comments;

    // ignored users are only considered in the production side
    if (isPreview()) {
      comments = getNotIgnoredComments(realTarget);
    } else {
      comments = getOnlineOrOwnComments(realTarget, user);
    }
    LOG.debug("found {} comments for target {}", comments.size(), realTarget);

    return comments;
  }

  public List<Review> getReviews(@NonNull Object target, CommunityUser user) {
    Object realTarget = contributionTargetHelper.getTarget(target);
    List<Review> reviews;
    if (isPreview()) {
      reviews = reviewService.getReviewsForPreview(realTarget, ASCENDING, REVIEW_FETCH_LIMIT);
    } else {
      reviews = getOnlineOrOwnReviews(realTarget, user);
    }

    LOG.debug("found {} reviews for target {}", reviews == null ? 0 : reviews.size(), realTarget);
    return reviews;
  }

  public Review getReview(@Nullable CommunityUser user, @NonNull Object target) {
    if (user == null) {
      return null;
    }
    Object realTarget = contributionTargetHelper.getTarget(target);
    return reviewService.getReviewForUser(user, realTarget);
  }

  /**
   * Creates a new {@link Comment} with the <tt>author</tt> as the author,
   * the <tt>target</tt> as the bean the comment relates to and the given <tt>comment</tt> text.
   *
   * @param author           the author of the comment
   * @param authorName       the anonymous name of the author of the comment; might be {@code null}
   *                         for non-anonymous users
   * @param target           the bean this comment is about
   * @param navigation       the navigation on which the target is shown
   * @param text             the actual content as an XHTML 1.0 fragment (typically a list of {@literal <p>} elements)
   * @param moderationType   the moderation type
   * @param replyToCommentId the comment if of the comment to reply to
   * @param blobs            the blobs associated with the comment. May be null.
   * @return the newly created {@link Comment}
   */
  public Comment createComment(CommunityUser author,  // NOSONAR Method has too many parameters
                               @Nullable String authorName,
                               @NonNull Object target,
                               Navigation navigation,
                               String text,
                               ModerationType moderationType,
                               String replyToCommentId,
                               List<Blob> blobs) {
    validateUser(author);

    Object realTarget = contributionTargetHelper.getTarget(target);

    Comment comment = createComment(author, authorName, text, target, realTarget, navigation, replyToCommentId);
    comment.setLocale(getLocale(realTarget));
    if (blobs != null) {
      comment.setAttachments(blobs);
    }
    commentService.save(comment, moderationType);

    return comment;
  }

  /**
   * Creates a new {@link Review} with the <tt>author</tt> as the author,
   * the <tt>target</tt> as the bean the review relates to, the text, title and rating.
   *
   * @param author           the author of the comment
   * @param target           the bean this comment is about
   * @param text             the actual content as an XHTML 1.0 fragment (typically a list of {@literal <p>} elements)
   * @param title            the title
   * @param rating           the rating
   * @param moderationType   the moderation type
   * @param blobs            the blobs associated with the review. May be null.
   * @return the newly created {@link Review}
   * @throws DuplicateReviewException if the given user has already written a review
   */
  public Review createReview(CommunityUser author,
                             @NonNull Object target,
                             String text,
                             String title,
                             int rating,
                             ModerationType moderationType,
                             List<Blob> blobs,
                             Navigation navigation) {

    validateUser(author);

    Object realTarget = contributionTargetHelper.getTarget(target);
    Review  review = createReview(author, text, realTarget, title, rating, navigation);

    if (blobs != null) {
      review.setAttachments(blobs);
    }

    review.setLocale(getLocale(realTarget));
    reviewService.save(review, moderationType);

    return review;
  }

  private Review createReview(CommunityUser author, String text, Object realTarget, String title, int rating, Navigation navigation) {
    Content navContent = navigation instanceof AbstractContentBean ? ((AbstractContentBean) navigation).getContent() : null;
    return reviewService.createReview(author, text, realTarget, getCategories(realTarget, navContent), title, rating);
  }

  private Comment createComment(CommunityUser author,
                                @Nullable String authorName,
                                String text,
                                Object target,
                                Object realTarget,
                                Navigation navigation,
                                String replyToCommentId) {
    Content navContent = navigation instanceof AbstractContentBean ? ((AbstractContentBean) navigation).getContent() : null;
    Comment replyToComment = null;
    if (!Strings.isNullOrEmpty(replyToCommentId)) {
      replyToComment = commentService.getComment(replyToCommentId);
    }
    Comment comment = commentService.createComment(author, text, realTarget, getCategories(realTarget, navContent), replyToComment);
    comment.setAuthorName(authorName);
    return comment;
  }

  private void validateUser(CommunityUser author) {
    if (author != null && author.isBlocked()) {
      throw new IllegalArgumentException("Error while creating review, user is blocked.");
    }
  }

  private Locale getLocale(@NonNull Object target) {
    Site siteForPage = contributionTargetHelper.getSite(target);
    return siteForPage == null ? Locale.ROOT : siteForPage.getLocale();
  }

  private Collection<String> getCategories(Object target, Content navigation) {
    Content content = contributionTargetHelper.getContentFromTarget(target);
    return categoryExtractor.getCategories(content, navigation);
  }

  /**
   * Retrieves the average rating value of the given target.
   *
   * @param target the target
   * @return the average value over all user ratings issued the given target
   */
  public double getAverageRating(@NonNull Object target) {
    return ratingService.getAverageRating(contributionTargetHelper.getTarget(target));
  }

  /**
   * Retrieves the average rating value of the given target.
   *
   * @param target the target
   * @return the average value over all user ratings issued the given target
   */
  public double getAverageReviewRating(@NonNull Object target) {
    return reviewService.getAverageRating(contributionTargetHelper.getTarget(target));
  }

  /**
   * Retrieves the rating state for the given {@link CommunityUser} and target.
   *
   * @param user   the {@link CommunityUser} who rated
   * @param target the rated {@link com.coremedia.blueprint.common.contentbeans.CMTeasable}
   * @return the rating state
   */
  public int getRating(CommunityUser user, @NonNull Object target) {
    Rating rating = ratingService.getRatingForUser(user, contributionTargetHelper.getTarget(target));
    return rating != null ? rating.getValue() : 0;
  }

  /**
   * Returns the total number of ratings issued for given bean.
   *
   * @param target the target bean
   * @return the number of ratings issued for the given bean by all users
   */
  public long getNumberOfRatings(@NonNull Object target) {
    return ratingService.getNumberOfRatings(contributionTargetHelper.getTarget(target));
  }

  /**
   * Returns the total number of online comments issued for given bean.
   *
   * @param target the target bean
   * @return the number of online comments issued for the given bean by all users
   */
  public long getNumberOfComments(@NonNull Object target) {
    return commentService.getNumberOfComments(contributionTargetHelper.getTarget(target));
  }

  public long getNumberOfReviews(@NonNull Object target) {
    return reviewService.getNumberOfReviews(contributionTargetHelper.getTarget(target));
  }

  /**
   * Updates the rating state for the given {@link CommunityUser} and target bean
   *
   * @param author     the {@link com.coremedia.elastic.social.api.users.CommunityUser} updating the rating
   * @param target     the rated target bean
   * @param navigation the navigation where the target is shown
   * @param rating     the rating value
   * @return true if the rating was created, false otherwise
   */
  public boolean updateRating(CommunityUser author, @NonNull Object target, CMNavigation navigation, int rating) {
    Object realTarget = contributionTargetHelper.getTarget(target);
    return ratingService.updateRating(author, realTarget, getCategories(realTarget, navigation.getContent()), rating);
  }

  /**
   * Returns the total number of likes issued for the given target bean   *
   *
   * @param target the target bean
   * @return the number of likes issued for the given target by all users
   */
  public long getNumberOfLikes(@NonNull Object target) {
    return likeService.getNumberOfLikes(contributionTargetHelper.getTarget(target));
  }

  /**
   * Retrieves the like state for the given {@link CommunityUser} and the target.
   *
   * @param author the {@link com.coremedia.elastic.social.api.users.CommunityUser} who liked
   * @param target the liked target bean
   * @return true, if the {@link CommunityUser} likes the target bean
   */
  public boolean hasLiked(@Nullable CommunityUser author, @NonNull Object target) {
    if (author == null) {
      return false;
    }
    Like like = likeService.getLikeForUser(author, contributionTargetHelper.getTarget(target));
    return like != null;
  }


  /**
   * Removes or creates the like state for the given {@link CommunityUser} and target.
   *
   * @param author     the {@link CommunityUser} updating the like
   * @param target     the liked {@link com.coremedia.blueprint.common.contentbeans.CMTeasable}
   * @param navigation the navigation the target is shown on
   * @param like       true, if the given {@link CommunityUser} likes the given target
   * @return true if the like was created, false otherwise
   */
  public boolean updateLike(@NonNull CommunityUser author, @NonNull Object target, CMNavigation navigation, boolean like) {
    try {
      Object realTarget = contributionTargetHelper.getTarget(target);
      return likeService.updateLike(author, realTarget, getCategories(realTarget, navigation.getContent()), like);
    } catch (RuntimeException e) {
      LOG.error("Error while liking this target: " + target, e);
      throw e;
    }
  }

  /**
   * Retrieves the complain state for the given {@link CommunityUser} and {@link Model}.
   *
   * @param author     the {@link com.coremedia.elastic.core.api.users.User} who complained about
   * @param id         the ID of the {@link com.coremedia.elastic.core.api.models.Model} that was complained about
   * @param collection the collection of the {@link com.coremedia.elastic.core.api.models.Model} that was complained about
   * @return true, if the {@link CommunityUser} complained about the {@link Comment}
   */
  public boolean hasComplaint(CommunityUser author, String id, String collection) {
    if (collection.equals(COMMENTS_COLLECTION)) {
      return hasComplaintForComment(author, commentService.getComment(id));
    } else if (collection.equals(USERS_COLLECTION)) {
      return hasComplaintForUser(author, communityUserService.getUserById(id));
    }
    return false;
  }

  /**
   * Retrieves the complain state for the given {@link CommunityUser} and {@link Comment}.
   *
   * @param author the {@link CommunityUser} who complained about
   * @param target the target of the complaint
   * @return true, if the {@link CommunityUser} complained about the {@link Comment}
   */
  public boolean hasComplaint(CommunityUser author, @NonNull Object target) {
    if (target instanceof Model) {
      Model model = (Model) target;
      return hasComplaint(author, model.getId(), model.getCollection());
    }
    return false;
  }

  public void updateComplaint(CommunityUser author, Object target, boolean complaint) {
    if (target instanceof Model) {
      Model model = (Model) target;
      updateComplaint(author, model.getId(), model.getCollection(), complaint);
    }
  }

  /**
   * Updates the complain state for the given {@link CommunityUser} and {@link Model}.
   *
   * @param author     the {@link CommunityUser} updating the complain
   * @param modelId    the ID of the {@link com.coremedia.elastic.core.api.models.Model} that was complained about
   * @param collection the collection of the {@link com.coremedia.elastic.core.api.models.Model} that was complained about
   * @param complaint  true, if the given {@link com.coremedia.elastic.core.api.users.User} complains the {@link com.coremedia.elastic.social.api.comments.Comment}
   */
  private void updateComplaint(CommunityUser author, String modelId, String collection, boolean complaint) {
    // check what to do for reviews
    if (collection.equals(COMMENTS_COLLECTION)) {
      updateComplaintForComment(author, modelId, complaint);
    } else if (collection.equals(USERS_COLLECTION)) {
      updateComplaintAboutUser(author, modelId, complaint);
    }
  }

  public boolean hasComplaintForComment(CommunityUser author, Comment comment) {
    return commentService.hasComplaintForUser(author, comment);
  }

  public void updateComplaintForComment(CommunityUser author, String commentId, boolean complaint) {
    Comment comment = commentService.getComment(commentId);
    if (complaint) {
      commentService.addComplaint(author, comment);
    } else {
      commentService.removeComplaint(author, comment);
    }
  }

  public boolean hasComplaintForUser(CommunityUser author, CommunityUser user) {
    return communityUserService.hasComplaintForUser(author, user);
  }

  public void updateComplaintAboutUser(CommunityUser author, String userId, boolean complaint) {
    CommunityUser user = communityUserService.getUserById(userId);
    if (complaint) {
      communityUserService.addComplaint(author, user);
    } else {
      communityUserService.removeComplaint(author, user);
    }
  }

  /**
   * Call this method, if a user has shared the content to a social network.
   *
   * @param user       the {@link com.coremedia.elastic.core.api.users.User} who shared the content
   * @param item       the target of the share
   * @param navigation the navigation the target is shown on
   * @param provider   the provider of the social network
   */
  public void share(CommunityUser user, Object item, CMNavigation navigation, String provider) {
    Object target = contributionTargetHelper.getTarget(item);
    shareService.updateShare(user, target, provider, getCategories(target, navigation.getContent()));
  }

  public boolean isPreview() {
    return preview;
  }

  @Value("${cae.is.preview}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }


  public Comment getComment(String id) {
    return commentService.getComment(id);
  }

  public CommunityUser getUser(String id) {
    return communityUserService.getUserById(id);
  }
}
