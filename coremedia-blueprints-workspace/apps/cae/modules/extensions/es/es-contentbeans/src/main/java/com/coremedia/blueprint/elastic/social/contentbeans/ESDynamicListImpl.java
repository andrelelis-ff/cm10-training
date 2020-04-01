package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.blueprint.base.elastic.common.AggregationType;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cap.common.NoSuchPropertyDescriptorException;
import com.coremedia.cap.content.Content;
import com.coremedia.elastic.core.api.counters.AverageCounter;
import com.coremedia.elastic.core.api.counters.Counter;
import com.coremedia.elastic.core.api.counters.Interval;
import com.coremedia.elastic.core.cms.ContentWithSite;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.ratings.ShareService;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.base.elastic.common.AggregationType.MOST_COMMENTED;
import static com.coremedia.blueprint.base.elastic.common.AggregationType.MOST_LIKED;
import static com.coremedia.blueprint.base.elastic.common.AggregationType.MOST_RATED;
import static com.coremedia.blueprint.base.elastic.common.AggregationType.MOST_REVIEWED;
import static com.coremedia.blueprint.base.elastic.common.AggregationType.MOST_SHARED;
import static com.coremedia.blueprint.base.elastic.common.AggregationType.TOP_RATED;
import static com.coremedia.blueprint.base.elastic.common.AggregationType.TOP_REVIEWED;
import static com.coremedia.elastic.core.api.counters.Interval.INFINITY;
import static java.util.Collections.emptyList;

/**
 * Generated extension class for beans of document type "ESDynamicList".
 */
public class ESDynamicListImpl extends ESDynamicListBase {

  /*
   * DEVELOPER NOTE
   * You are invited to change this class by adding additional methods here.
   * Add them to the interface {@link com.coremedia.blueprint.elastic.social.blueprint.contentbean.ESDynamicList} to make them public.
   */

  private static final Logger LOG = LoggerFactory.getLogger(ESDynamicListImpl.class);

  private static final int DEFAULT_MAX_LENGTH = 5;

  private CommentService commentService;
  private LikeService likeService;
  private RatingService ratingService;
  private ShareService shareService;
  private ReviewService reviewService;

  /**
   * List of predicates,
   */
  private List<CountTargetPredicate> countTargetPredicates;

  public void setCommentService(CommentService commentService) {
    this.commentService = commentService;
  }

  public void setLikeService(LikeService likeService) {
    this.likeService = likeService;
  }

  public void setRatingService(RatingService ratingService) {
    this.ratingService = ratingService;
  }

  public void setReviewService(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  public void setShareService(ShareService shareService) {
    this.shareService = shareService;
  }

  public void setCountTargetPredicates(List<CountTargetPredicate> countTargetPredicates) {
    this.countTargetPredicates = countTargetPredicates;
  }

  @Override
  public List<Count> getItemsUnfiltered() {
    int maxLength = getMaxLength();
    AggregationType aggregationType = getEnumAggregationType();
    List<Count> items;
    if (aggregationType == TOP_RATED) {
      items = getCountItemsForAverage(ratingService.getTopRated(getCategory(), getFeedbackInterval(), maxLength));
    } else if (aggregationType == MOST_RATED) {
      items = getCountItemsForAverage(ratingService.getMostRated(getCategory(), getFeedbackInterval(), maxLength));
    } else if (aggregationType == MOST_COMMENTED) {
      items = getCountItems(commentService.getMostCommented(getCategory(), getFeedbackInterval(), maxLength));
    } else if (aggregationType == MOST_REVIEWED) {
      items = getCountItems(reviewService.getMostReviewed(getCategory(), getFeedbackInterval(), maxLength));
    } else if (aggregationType == MOST_LIKED) {
      items = getCountItems(likeService.getMostLiked(getCategory(), getFeedbackInterval(), maxLength));
    } else if (aggregationType == MOST_SHARED) {
      items = getCountItems(shareService.getMostShared(getCategory(), getFeedbackInterval(), maxLength));
    } else if (aggregationType == TOP_REVIEWED) {
      items = getCountItemsForAverage(reviewService.getTopRated(getCategory(), getFeedbackInterval(), maxLength));
    } else {
      items = emptyList();
    }
    items = transformTargets(items);
    LOG.debug("unfiltered count items are {}", items);
    return items;
  }

  @Override
  protected List<Count> filterItems(List<Count> itemsUnfiltered) {
    List<Count> filtered = itemsUnfiltered.stream().filter(input -> {
      if (input != null) {
        final Object target = input.getTarget();
        for (CountTargetPredicate countTargetPredicate : countTargetPredicates) {
          if (countTargetPredicate.getType().isInstance(target)) {
            //noinspection unchecked
            if (!countTargetPredicate.apply(target)) {
              return false;
            }
          }
        }
      }
      return true;
    }).collect(Collectors.toList());
    LOG.debug("filtered count items {}: {}", itemsUnfiltered, filtered);
    return filtered;
  }

  @Override
  public int getMaxLength() {
    int result = super.getMaxLength();
    if (result < 0) {
      result = DEFAULT_MAX_LENGTH;
    }
    return result;
  }

  private List<Count> getCountItems(List<Counter> counters) {
    List<Count> result = new ArrayList<>(counters.size());
    for (Counter counter : counters) {
      result.add(new Count(counter));
    }
    return result;
  }

  private List<Count> getCountItemsForAverage(List<AverageCounter> averageCounters) {
    List<Count> result = new ArrayList<>(averageCounters.size());
    for (AverageCounter averageCounter : averageCounters) {
      result.add(new Count(averageCounter));
    }
    return result;
  }

  private AggregationType getEnumAggregationType() {
    return getEnum(getAggregationType(), AggregationType.class, null);
  }

  private Interval getFeedbackInterval() {
    return getEnum(getInterval(), Interval.class, INFINITY);
  }

  public String getCategory() {
    List<? extends CMChannel> channels = getChannel();
    return channels == null || channels.size() == 0 ? null : channels.get(0).getSegment();
  }

  private <T extends Enum<T>> T getEnum(String value, Class<T> type, T defaultValue) {
    T result = defaultValue;
    try {
      if (value != null) {
        result = Enum.valueOf(type, value);
      } else {
        LOG.debug("Falling back to default '{}'", defaultValue);
      }
    } catch (NoSuchPropertyDescriptorException e) {
      LOG.debug("Falling back to default '{}'", defaultValue);
    } catch (IllegalArgumentException e) {
      LOG.debug("Invalid {}, falling back to default '{}'", value, defaultValue);
    }
    return result;
  }

  private List<Count> transformTargets(List<Count> itemsUntransformed) {
    // transform targets if necessary and make sure there are no null values
    return itemsUntransformed.stream()
      .filter(Objects::nonNull)
      .map(count -> {
        Object target = count.getTarget();
        // we need to have ContentBeans not ContentWithSite, transform it
        // this manual transformation in this class is not elegant and should change in the future
        if (target instanceof ContentWithSite) {
          Content content = ((ContentWithSite) target).getContent();
          target = getContentBeanFactory().createBeanFor(content, ContentBean.class);
        }
        return new Count(count, target);
      }).collect(ImmutableList.toImmutableList());
  }
}
