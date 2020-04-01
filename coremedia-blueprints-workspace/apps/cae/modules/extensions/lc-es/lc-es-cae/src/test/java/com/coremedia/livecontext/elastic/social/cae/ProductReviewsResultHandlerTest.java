package com.coremedia.livecontext.elastic.social.cae;

import com.coremedia.blueprint.base.elastic.social.common.ContributionTargetHelper;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceIdProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.cae.web.i18n.PageResourceBundleFactory;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMPlaceholder;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.blueprint.elastic.social.cae.controller.ContributionMessageKeys;
import com.coremedia.blueprint.elastic.social.cae.controller.HandlerInfo;
import com.coremedia.blueprint.elastic.social.cae.guid.GuidCookieHandler;
import com.coremedia.blueprint.elastic.social.cae.user.ElasticSocialUserHelper;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.user.User;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.elastic.social.api.ModerationType;
import com.coremedia.elastic.social.api.reviews.Review;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.fragment.FragmentContext;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceIdOrThrow;
import static com.coremedia.elastic.social.api.ContributionType.ANONYMOUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductReviewsResultHandlerTest {

  private String contextId = "5678";
  private String targetId = "vendor:///catalog/product/1234";
  private String text = "test test test test test test test";
  private String title = "title";
  private int rating = 5;

  @InjectMocks
  private ProductReviewsResultHandler handler;

  @Mock
  private ElasticSocialService elasticSocialService;

  @Mock
  private ElasticSocialPlugin elasticSocialPlugin;

  @Mock
  private ElasticSocialConfiguration elasticSocialConfiguration;

  @Mock
  private PageResourceBundleFactory resourceBundleFactory;

  // @Mock   Möööp... ResourceBundle is all final, cannot be mocked
  private MockResourceBundle resourceBundle = new MockResourceBundle();

  @Mock
  private CommunityUserService communityUserService;

  @Mock
  private Content navigationContent;

  @Mock
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  @Mock
  private CommunityUser user;

  @Mock
  private Review review;

  @Mock
  private CMContext context;

  @Mock
  private ContextHelper contextHelper;

  @Mock
  private Site site;

  @Mock
  private CMPlaceholder page;

  @Mock
  private UriTemplate uriTemplate;

  @Mock
  private HttpServletRequest request;

  @Mock
  private Product product;

  @Mock
  private CatalogService catalogService;

  @Mock
  private StoreContext storeContext;

  @Mock
  private ContributionTargetHelper contributionTargetHelper;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private CMNavigation cmNavigation;

  @Before
  public void setup() {
    handler.setElasticSocialUserHelper(new ElasticSocialUserHelper(communityUserService));
    handler.setContextHelper(contextHelper);
    handler.setElasticSocialPlugin(elasticSocialPlugin);
    handler.setContributionTargetHelper(contributionTargetHelper);
    handler.setNavigationSegmentsUriHelper(navigationSegmentsUriHelper);

    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;productId=1234";
    FragmentContext fragmentContext = new FragmentContext();
    FragmentParameters fragmentParameters = FragmentParametersFactory.create(url);
    fragmentContext.setParameters(fragmentParameters);
    fragmentContext.setFragmentRequest(true);
    when(request.getAttribute("CM_FRAGMENT_CONTEXT")).thenReturn(fragmentContext);

    GuidCookieHandler.setCurrentGuid("1234+5678");
    when(communityUserService.getUserById("1234")).thenReturn(user);

    when(contentRepository.getContent(IdHelper.formatContentId(contextId))).thenReturn(navigationContent);
    when(contentBeanFactory.createBeanFor(navigationContent, ContentBean.class)).thenReturn(cmNavigation);
    when(cmNavigation.getContext()).thenReturn(context);

    when(elasticSocialPlugin.getElasticSocialConfiguration(any())).thenReturn(elasticSocialConfiguration);
    when(elasticSocialConfiguration.isFeedbackEnabled()).thenReturn(true);
    when(elasticSocialConfiguration.getReviewType()).thenReturn(ANONYMOUS);
    when(elasticSocialConfiguration.isWritingReviewsEnabled()).thenReturn(true);
    when(elasticSocialConfiguration.isAnonymousReviewingEnabled()).thenReturn(true);

    resourceBundle = new MockResourceBundle();
    when(resourceBundleFactory.resourceBundle(any(Navigation.class), nullable(User.class))).thenReturn(resourceBundle);

    when(catalogService.findProductById(any(), any(StoreContext.class))).thenReturn(product);

    when(request.getAttribute(SiteHelper.SITE_KEY)).thenReturn(site);

    BaseCommerceIdProvider idProvider = TestVendors.getIdProvider("vendor");

    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    when(commerceConnection.getIdProvider()).thenReturn(idProvider);
    when(commerceConnection.getCatalogService()).thenReturn(catalogService);

    when(storeContext.getConnection()).thenReturn(commerceConnection);

    CurrentStoreContext.set(storeContext);
  }

  @After
  public void cleanUp() {
    UserContext.clear();
    CurrentStoreContext.remove();
  }

  @Test
  public void getReviews() {
    String view = "default";
    List<Review> reviews = new ArrayList<>();
    reviews.add(review);
    when(elasticSocialService.getReviews(product, user)).thenReturn(reviews);
    ModelAndView result = handler.getReviews(contextId, targetId, view, request);

    assertEquals(view, result.getViewName());
    assertEquals(cmNavigation, result.getModelMap().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));

    ProductReviewsResult productReviewsResult = getModel(result, ProductReviewsResult.class);
    Product target = (Product) productReviewsResult.getTarget();
    assertEquals(product, target);

    List<Review> reviewsResult = productReviewsResult.getReviews();

    assertEquals(reviews, reviewsResult);
    // check if configuration is what configured in setup
    assertTrue(productReviewsResult.isEnabled());
    assertTrue(productReviewsResult.isWritingContributionsEnabled());
    assertTrue(productReviewsResult.isAnonymousContributingEnabled());
    verify(elasticSocialService).getReviews(any(Product.class), any(CommunityUser.class));

    // make sure reviews are loaded lazily when getReviews is called
    productReviewsResult.getReviews();
    verify(elasticSocialService).getReviews(target, user);
  }

  @Test
  public void createReview() {
    when(elasticSocialService.createReview(eq(user), any(Product.class), eq(text), eq(title), eq(rating), eq(ModerationType.POST_MODERATION), nullable(List.class), any(Navigation.class))).thenReturn(review);
    when(elasticSocialConfiguration.getReviewModerationType()).thenReturn(ModerationType.POST_MODERATION);
    ModelAndView modelAndView = handler.createReview(contextId, targetId, text, title, rating, request);

    HandlerInfo resultModel = getModel(modelAndView, HandlerInfo.class);
    assertTrue(resultModel.getErrors().isEmpty());
    assertEquals(1, resultModel.getMessages().size());
    assertTrue(resultModel.isSuccess());
    verify(elasticSocialService).createReview(eq(user), any(Product.class), eq(text), eq(title), eq(rating), eq(ModerationType.POST_MODERATION), nullable(List.class), any(Navigation.class));
    verifyMessage(ContributionMessageKeys.REVIEW_FORM_SUCCESS);
  }

  @Test
  public void createReviewRatingNull() {
    ModelAndView modelAndView = handler.createReview(contextId, targetId, text, title, null, request);

    HandlerInfo resultModel = getModel(modelAndView, HandlerInfo.class);
    assertTrue(resultModel.getErrors().isEmpty());
    assertEquals(1, resultModel.getMessages().size());
    assertFalse(resultModel.isSuccess());
    verify(elasticSocialService, never()).createReview(any(CommunityUser.class), any(Product.class), anyString(), anyString(), anyInt(), any(ModerationType.class), anyList(), any(Navigation.class));
    verifyNotMessage(ContributionMessageKeys.REVIEW_FORM_SUCCESS);
  }

  @Test
  public void createReviewDisabled() {
    when(elasticSocialConfiguration.isWritingReviewsEnabled()).thenReturn(false);
    ModelAndView modelAndView = handler.createReview(contextId, targetId, text, title, rating, request);

    HandlerInfo resultModel = getModel(modelAndView, HandlerInfo.class);
    assertTrue(resultModel.getErrors().isEmpty());
    assertEquals(1, resultModel.getMessages().size());
    assertFalse(resultModel.isSuccess());
    verify(elasticSocialService, never()).createReview(any(CommunityUser.class), any(Product.class), anyString(), anyString(), anyInt(), any(ModerationType.class), anyList(), any(Navigation.class));
    verifyNotMessage(ContributionMessageKeys.REVIEW_FORM_SUCCESS);
  }

  @Test
  public void createReviewAnonymousDisabled() {
    when(elasticSocialConfiguration.isAnonymousReviewingEnabled()).thenReturn(false);
    when(user.isAnonymous()).thenReturn(true);
    ModelAndView modelAndView = handler.createReview(contextId, targetId, text, title, rating, request);

    HandlerInfo resultModel = getModel(modelAndView, HandlerInfo.class);
    assertTrue(resultModel.getErrors().isEmpty());
    assertEquals(1, resultModel.getMessages().size());
    assertFalse(resultModel.isSuccess());
    verify(elasticSocialService, never()).createReview(any(CommunityUser.class), any(Product.class), anyString(), anyString(), anyInt(), any(ModerationType.class), anyList(), any(Navigation.class));
    verifyNotMessage(ContributionMessageKeys.REVIEW_FORM_SUCCESS);
  }

  @Test
  public void getReviewsPlaceholder() {
    ProductReviewsResult reviewsResult = handler.getReviews(page, request);

    assertNotNull(reviewsResult);
    assertEquals(product, reviewsResult.getTarget());
  }

  @Test
  public void buildLink() throws URISyntaxException {
    Map<String, Object> linkParameters = new HashMap<>();
    List<String> pathList = new ArrayList<>();
    String path = "path/" + contextId;
    pathList.add(path);
    when(contextHelper.currentSiteContext()).thenReturn(cmNavigation);
    when(cmNavigation.getContext()).thenReturn(context);
    when(context.getContentId()).thenReturn(Integer.parseInt(contextId));
    when(navigationSegmentsUriHelper.getPathList(cmNavigation)).thenReturn(pathList);
    URI uri = new URI(path);
    when(uriTemplate.expand(any(String.class), any(Integer.class), any())).thenReturn(uri);

    when(product.getReference()).thenReturn(parseCommerceIdOrThrow(targetId));
    ProductReviewsResult productReviewsResult = new ProductReviewsResult(product);
    UriComponents uriComponents = handler.buildLink(productReviewsResult, uriTemplate, linkParameters, request);

    assertNotNull(uriComponents);
    assertEquals(path, uriComponents.getPath());
  }

  private <T> T getModel(ModelAndView modelAndView, Class<T> type) {
    return getModel(modelAndView, "self", type);
  }

  private <T> T getModel(ModelAndView modelAndView, String key, Class<T> type) {
    Map<String, Object> modelMap = modelAndView.getModel();
    Object model = modelMap.get(key);
    // assertTrue(model instanceof type);
    return (T) model; // NO_SONAR
  }

  private void verifyMessage(String key) {
    assertTrue(resourceBundle.invokedFor(key));
  }

  private void verifyNotMessage(String key) {
    assertFalse(resourceBundle.invokedFor(key));
  }
}
