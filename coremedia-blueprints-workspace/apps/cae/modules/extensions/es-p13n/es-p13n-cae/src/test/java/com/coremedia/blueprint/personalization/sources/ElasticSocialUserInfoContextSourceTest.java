package com.coremedia.blueprint.personalization.sources;

import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.elastic.core.test.Injection;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class ElasticSocialUserInfoContextSourceTest {

  private ElasticSocialUserInfoContextSource elasticSocialUserInfoContextSource;
  private ContextCollection contextCollection;

  @Mock
  private CommentService commentService;

  @Mock
  private RatingService ratingService;

  @Mock
  private LikeService likeService;

  @Mock
  private CommunityUser communityUser;

  @Before
  public void setUp() {
    contextCollection = new ContextCollectionImpl();
    elasticSocialUserInfoContextSource = new ElasticSocialUserInfoContextSource();
    elasticSocialUserInfoContextSource.setContextName("es_check");
    UserContext.setUser(communityUser);

    when(commentService.getNumberOfApprovedComments(communityUser)).thenReturn(10L);
    when(ratingService.getNumberOfRatingsFromUser(communityUser)).thenReturn(10L);
    Injection.inject(elasticSocialUserInfoContextSource, commentService);
    Injection.inject(elasticSocialUserInfoContextSource, ratingService);
    Injection.inject(elasticSocialUserInfoContextSource, likeService);
  }


  @Test
  public void testPreHandle() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    elasticSocialUserInfoContextSource.preHandle(request, response, contextCollection);
    PropertyProvider propertyProfile = (PropertyProvider) contextCollection.getContext("es_check");
    Assert.assertNotNull(propertyProfile);
    Assert.assertTrue(propertyProfile.getPropertyNames().contains("numberOfComments"));
    Assert.assertTrue(propertyProfile.getPropertyNames().contains("userLoggedIn"));
    Assert.assertTrue(propertyProfile.getPropertyNames().contains("numberOfRatings"));
    Assert.assertEquals(10L, propertyProfile.getProperty("numberOfComments"));
    Assert.assertTrue((Boolean) propertyProfile.getProperty("userLoggedIn"));
    Assert.assertEquals(10L, propertyProfile.getProperty("numberOfComments"));
  }
}
