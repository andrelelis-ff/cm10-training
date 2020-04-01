package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.base.elastic.common.AggregationType;
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.content.Content;
import com.coremedia.elastic.core.api.counters.AverageCounter;
import com.coremedia.elastic.core.api.counters.Counter;
import com.coremedia.elastic.core.api.counters.Interval;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.ratings.ShareService;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.objectserver.beans.ContentBeanDefinition;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.coremedia.elastic.core.api.counters.Interval.INFINITY;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ESDynamicListImplTest {
  private ESDynamicListImpl list;

  @Mock
  private Content content;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private ContentBeanDefinition definition;

  @Mock
  private CapType type;

  @Mock
  private AverageCounter averageCounter;

  @Mock
  private RatingService ratingService;

  @Mock
  private Counter counter;

  @Mock
  private CommentService commentService;


  @Mock
  private ReviewService reviewService;

  @Mock
  private CMNavigation navigation;

  @Mock
  private LikeService likeService;

  @Mock
  private ShareService shareService;

  @Mock
  private CMTeasable cmTeasable;

  @Mock
  private Content teasableContent;

  @Mock
  private CountTargetPredicate predicate1;

  @Mock
  private CountTargetPredicate predicate2;

  @Before
  public void setup() throws IllegalAccessException {
    when(predicate1.getType()).thenReturn(CMTeasable.class);
    when(predicate2.getType()).thenReturn(CMTeasable.class);
    when(predicate1.apply(any(CMTeasable.class))).thenReturn(true);
    when(predicate2.apply(any(CMTeasable.class))).thenReturn(true);

    list = new ESDynamicListImpl();
    list.setRatingService(ratingService);
    list.setLikeService(likeService);
    list.setCommentService(commentService);
    list.setReviewService(reviewService);
    list.setShareService(shareService);
    list.setCountTargetPredicates(ImmutableList.of(predicate1, predicate2));
    FieldUtils.writeField(list, "content", content, true);
    when(content.isInProduction()).thenReturn(true);
    FieldUtils.writeField(list, "definition", definition, true);
    when(definition.getContentBeanFactory()).thenReturn(contentBeanFactory);
  }
  @Test
  public void getItemsUnknownSearchField() {
    List<Count> result = list.getItems();

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void getItemsWithoutAggregationType() throws Exception {
    when(content.getString("aggregationType")).thenReturn(null);
    when(content.getInt("maxLength")).thenReturn(5);
    when(ratingService.getTopRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void getItemsTopRated() throws Exception {
    prepareConfiguration(AggregationType.TOP_RATED.name(), null, 5, null);
    when(ratingService.getTopRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(ratingService).getTopRated(null, INFINITY, 5);
  }

  @Test
  public void getItemsMostRated() throws Exception {
    prepareConfiguration(AggregationType.MOST_RATED.name(), null, 5, null);
    when(ratingService.getMostRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(ratingService).getMostRated(null, INFINITY, 5);
  }

  @Test
  public void getItemsMostCommentedNoIntervalProperty() throws Exception {
    prepareConfiguration(AggregationType.MOST_COMMENTED.name(), null, 5, null);
    when(commentService.getMostCommented(null, INFINITY, 5)).thenReturn(singletonList(counter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(commentService).getMostCommented(null, INFINITY, 5);
  }

  @Test
  public void getItemsMostReviewedNoIntervalProperty() throws Exception {
    prepareConfiguration(AggregationType.MOST_REVIEWED.name(), null, 5, null);
    when(reviewService.getMostReviewed(null, INFINITY, 5)).thenReturn(singletonList(counter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(reviewService).getMostReviewed(null, INFINITY, 5);
  }

  @Test
  public void getItemsTopReviewed() throws Exception {
    prepareConfiguration(AggregationType.TOP_REVIEWED.name(), null, 5, null);
    when(reviewService.getTopRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(reviewService).getTopRated(null, INFINITY, 5);
  }

  @Test
  public void getItemsMostCommentedUnknownIntervalProperty() throws Exception {
    prepareConfiguration(AggregationType.MOST_COMMENTED.name(), "other", 5, null);
    when(commentService.getMostCommented(null, INFINITY, 5)).thenReturn(singletonList(counter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(commentService).getMostCommented(null, INFINITY, 5);
  }

  @Test
  public void getItemsMostCommentedLastWeek() throws Exception {
    prepareConfiguration(AggregationType.MOST_COMMENTED.name(), Interval.LAST_WEEK.name(), 5, null);
    when(commentService.getMostCommented(null, Interval.LAST_WEEK, 5)).thenReturn(singletonList(counter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);
    assertEquals(Interval.LAST_WEEK.name(), list.getInterval());

    verify(commentService).getMostCommented(null, Interval.LAST_WEEK, 5);
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void getItemsMostLiked() throws Exception {
    prepareConfiguration(AggregationType.MOST_LIKED.name(), null, 5, null);
    when(content.getLinks("channel")).thenReturn((List) singletonList(navigation));
    when(likeService.getMostLiked(null, INFINITY, 5)).thenReturn(singletonList(counter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(likeService).getMostLiked(null, INFINITY, 5);
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void getItemsMostShared() throws Exception {
    prepareConfiguration(AggregationType.MOST_SHARED.name(), null, 5, singletonList(navigation));
    when(shareService.getMostShared(null, INFINITY, 5)).thenReturn(singletonList(counter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, null, 0L, 0L, 0L, 0.0);

    verify(shareService).getMostShared(null, INFINITY, 5);
  }

  @Test
  public void getItemsTopRatedValidTeasable() throws Exception {
    prepareConfiguration(AggregationType.TOP_RATED.name(), null, 5, null);
    when(averageCounter.getTarget()).thenReturn(cmTeasable);
    when(cmTeasable.getContent()).thenReturn(teasableContent);
    when(teasableContent.isInProduction()).thenReturn(true);
    when(ratingService.getTopRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItemsUnfiltered();

    assertNotNull(result);
    assertEquals(1, result.size());
    validateResult(result.get(0), null, cmTeasable, 0L, 0L, 0L, 0.0);

    verify(ratingService).getTopRated(null, INFINITY, 5);
  }

  @Test
  public void getItemsTopRatedInvalidTeasable() throws Exception {
    prepareConfiguration(AggregationType.TOP_RATED.name(), null, 5, null);
    when(predicate2.apply(cmTeasable)).thenReturn(false);
    when(averageCounter.getTarget()).thenReturn(cmTeasable);
    when(ratingService.getTopRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItems();

    assertNotNull(result);
    assertEquals(0, result.size());

    verify(ratingService).getTopRated(null, INFINITY, 5);
  }

  @Test
  public void getItemsTopRatedFiltered() throws Exception {
    prepareConfiguration(AggregationType.TOP_RATED.name(), null, 5, null);
    when(averageCounter.getTarget()).thenReturn(cmTeasable);
    when(ratingService.getTopRated(null, INFINITY, 5)).thenReturn(singletonList(averageCounter));

    List<Count> result = list.getItems();

    assertNotNull(result);
    assertEquals(1, result.size());

    verify(ratingService).getTopRated(null, INFINITY, 5);
  }

  @Test
  public void getDefaultMaxLength() {
    when(content.getInt("maxLength")).thenReturn(Integer.MIN_VALUE);
    int maxLength = list.getMaxLength();
    assertEquals(5, maxLength);
  }

  @Test
  public void getMaxLength() {
    when(content.getInt("maxLength")).thenReturn(10);
    int maxLength = list.getMaxLength();
    assertEquals(10, maxLength);
  }

  @Test
  public void getNullMaxLength() {
    when(content.getInt("maxLength")).thenReturn(0);
    int maxLength = list.getMaxLength();
    assertEquals(0, maxLength);
  }

  private void prepareConfiguration(String aggregationType, String intervalName, int maxLength, List<CMNavigation> channels) {
    when(content.getString("aggregationType")).thenReturn(aggregationType);
    when(content.getString("interval")).thenReturn(intervalName);
    when(content.getInt("maxLength")).thenReturn(maxLength);
    when(content.getLinks("channel")).thenReturn((List)channels);
  }

  private void validateResult(Count count, String name, Object target, long sum, long quantity, long value, double average) {
    assertEquals(name, count.getName());
    assertEquals(target, count.getTarget());
    assertEquals(sum, count.getSum());
    assertEquals(quantity, count.getQuantity());
    assertEquals(value, count.getValue());
    assertEquals(average, count.getAverage(), 0.0);
  }

}
