package com.coremedia.livecontext.elastic.social.cae;


import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.users.CommunityUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductReviewsResultTest {

  @Mock
  private CommunityUser user;

  @Mock
  private ElasticSocialService elasticSocialService;

  @Mock
  private ElasticSocialConfiguration elasticSocialConfiguration;

  @Mock
  private Object target;

  @Test
  public void testProductReviewsResult() {
    List<Review> resultList = Collections.emptyList();
    when(elasticSocialService.getReviews(target, user)).thenReturn(resultList);

    ProductReviewsResult result = new ProductReviewsResult(target, user, elasticSocialService, true, ContributionType.ANONYMOUS, elasticSocialConfiguration);

    assertEquals(target, result.getTarget());
    assertEquals(user, result.getUser());
    assertEquals(resultList, result.getReviews());
    verify(elasticSocialService).getReviews(target, user);
  }
}
