package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.test.BlueprintMockRequestUtil;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.common.services.context.CurrentContextService;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.coremedia.objectserver.web.links.LinkFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import static com.coremedia.blueprint.cae.handlers.PageRssHandlerTest.LocalConfig.PROFILE;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * Tests the {@link com.coremedia.blueprint.cae.handlers.PageRssHandler}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = PageRssHandlerTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class PageRssHandlerTest {

  @Configuration
  @Import(HandlerTestConfiguration.class)
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "PageRssHandlerTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/blueprint/cae/handlers/rss/rss.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }

  public static final String FEED_VIEW_NAME = "asFeed";
  public static final String FEED_URL = "/" + PREFIX_SERVICE + "/rss/media/124/feed.rss";
  public static final String FEED_URL_TOPIC = "/" + PREFIX_SERVICE + "/rss/media/4280/4250/feed.rss";

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private MockHttpServletResponse response;
  
  @Inject
  private RequestTestHelper requestTestHelper;
  
  @Inject
  private ContentTestHelper contentTestHelper;

  @Inject
  private LinkFormatter linkFormatter;

  @Inject
  private ContextHelper contextHelper;

  // --- tests ------------------------------------------------------

  /**
   * Test {@link PageRssHandler#handleRss}.
   */
  @Test
  public void testHandleRequest() throws Exception {
    ModelAndView modelAndView = requestTestHelper.request(FEED_URL);
    assertModel(modelAndView, 124);
    assertEquals(FEED_VIEW_NAME, modelAndView.getViewName());
  }

  /**
   * Test topicpage-request
   * {@link PageRssHandler#handleRssTopicPage}
   */
  @Test
  public void testHandleTopicPageRequest() throws Exception {
    ModelAndView modelAndView = requestTestHelper.request(FEED_URL_TOPIC);
    assertModel(modelAndView, 4280);
    assertEquals(FEED_VIEW_NAME, modelAndView.getViewName());
  }

  /**
   * Test "not found" for invalid taxonomy id of topicpage.
   * {@link PageRssHandler#handleRssTopicPage}.
   */
  @Test
  public void testNotFoundForInvalidTaxonomyId() throws Exception {
    ModelAndView modelAndView = requestTestHelper.request("/service/rss/media/124/8888/feed.rss");
    assertNotFound("Taxonomy not found", modelAndView);
  }

  /**
   * Test "not found" for non-FeedSource.
   * {@link PageRssHandler#handleRss}.
   */
  @Test
  public void testNotFoundForNonFeedSource() throws Exception {
    ModelAndView modelAndView = requestTestHelper.request("/service/rss/media/666/feed.rss");
    // 666 is not a navigation, but a regular linkable
    assertNotFound("not a FeedSource", modelAndView);
  }

  /**
   * Test "not found" if content does not exist.
   * {@link PageRssHandler#handleRss}.
   */
  @Test
  public void testNotFoundIfContentDoesNotExist() throws Exception {
    ModelAndView modelAndView = requestTestHelper.request("/service/rss/media/668/feed.rss");
    // 668 does not exist
    assertNotFound("null navigation", modelAndView);
  }

  /**
   * Test "not found" if the root segment does not match the root segment of the content found by ID.
   * {@link PageRssHandler#handleRss}.
   */
  @Test
  public void testNotFoundIfRootSegmentDoesNotMatchContent() throws Exception {
    ModelAndView modelAndView = requestTestHelper.request("/service/rss/notmedia/124/feed.rss");
    assertNotFound("wrong root segment", modelAndView);
  }

  /**
   * Test generation of RSS URL: /service/rootSegment/navigationId/asFeed/index.xml
   * <p/>
   * {@link PageRssHandler#buildLink(com.coremedia.blueprint.common.feeds.FeedSource, javax.servlet.http.HttpServletRequest)}
   */
  @Test
  public void testGenerateRssUrl() {
    CMNavigation channel = contentTestHelper.getContentBean(124);
    String url = createRelativeShortUrl(channel, channel);
    assertEquals("Bad rss URL for channel", FEED_URL, url);
  }

  /**
   * Test generation of RSS URL: /service/rootSegment/navigationId/taxonomyId/asFeed/index.xml
   * <p/>
   * {@link PageRssHandler#buildTaxonomyLink(com.coremedia.blueprint.common.contentbeans.CMTaxonomy, javax.servlet.http.HttpServletRequest)}
   */
  @Test
  @DirtiesContext
  public void testGenerateTopicPageRssUrl() {
    CMNavigation nav = contentTestHelper.getContentBean(124);
    CMTaxonomy taxonomy = contentTestHelper.getContentBean(4250);


    // Must mock a current context, otherwise topic pages for
    // global taxonomies don't work.
    CurrentContextService ccs = mock(CurrentContextService.class);
    when(ccs.getContext()).thenReturn((CMChannel) nav);
    contextHelper.setCurrentContextService(ccs);

    String url = createRelativeShortUrl(nav, taxonomy);
    // the resulting URL contains the ID of the topic page (4280), not of the root channel (124),
    // although the original context was the root channel
    assertEquals("Bad rss URL for topicchannel", FEED_URL_TOPIC, url);
  }

  // --- internal ---------------------------------------------------

  private String createRelativeShortUrl(CMNavigation navigation, Object bean) {
    MockHttpServletRequest request = BlueprintMockRequestUtil.createRequestWithContext(navigation);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    return linkFormatter.formatLink(bean, FEED_VIEW_NAME, request, response, false);
  }


  private void assertModel(ModelAndView modelAndView, int beanId) {
    ContentBean bean = contentTestHelper.getContentBean(beanId);
    assertNotNull("Model was not resolved correctly", modelAndView);
    assertEquals("Model does not match mocked content", bean, modelAndView.getModel().get("self"));
  }

  private void assertNotFound(String message, ModelAndView modelAndView) {
    assertHttpError(message, HttpServletResponse.SC_NOT_FOUND, modelAndView);
  }

  private void assertHttpError(String message, int expectedErrorCode, ModelAndView modelAndView) {
    assertSame(String.format("Unexpected root model (%s):", message), HttpError.class, HandlerHelper.getRootModel(modelAndView).getClass());
    assertEquals(String.format("Unexpected error code (%s):", message), expectedErrorCode, ((HttpError) HandlerHelper.getRootModel(modelAndView)).getErrorCode());
  }

}
