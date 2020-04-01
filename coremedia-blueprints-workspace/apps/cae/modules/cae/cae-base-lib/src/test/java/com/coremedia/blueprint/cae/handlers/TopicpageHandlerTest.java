package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.test.BlueprintMockRequestUtil;
import com.coremedia.blueprint.common.contentbeans.CMCSS;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.common.services.context.CurrentContextService;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
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
import java.util.List;

import static com.coremedia.blueprint.cae.handlers.TopicpageHandlerTest.LocalConfig.PROFILE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TopicpageHandlerTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class TopicpageHandlerTest {

  @Configuration
  @ImportResource(
          value = {
                  "classpath:/framework/spring/blueprint-page.xml",
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(HandlerTestConfiguration.class)
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "TopicpageHandlerTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/blueprint/cae/handlers/topicpages/topicpages.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }
  private String idSeparator;
  private String hamburgUrl;
  private String holidayUrl;
  private String lifestyleUrl;

  private CMChannel media;
  private CMChannel topicpageChannel;

  @Inject
  private ContentTestHelper contentTestHelper;

  @Inject
  private PageHandler pageHandler;

  @Inject
  private ContextHelper contextHelper;

  @Inject
  private LinkFormatter linkFormatter;

  @Inject
  private RequestTestHelper requestTestHelper;

  @Inject
  private MockHttpServletRequest request;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private MockHttpServletResponse response;

  // --- Setup ------------------------------------------------------

  @Before
  public void setUp() throws Exception {
    idSeparator = "-";
    hamburgUrl = "/media/topic/hamburg" + idSeparator + "4250";
    holidayUrl = "/media/topic/holiday" + idSeparator + "304";
    // Even though lifestyle has a custom topic page with segment "600-topic",
    // the URL is built with the segment "topic" of the default topic page.
    lifestyleUrl = "/media/topic/lifestyle" + idSeparator + "306";

    media = contentTestHelper.getContentBean(124);
    topicpageChannel = contentTestHelper.getContentBean(4280);
  }

  /**
   * <p>
   * Link building for global taxonomies needs the "current context"
   * of the request.
   * </p>
   * <p>
   * This mock must not be active during request handling tests, because during
   * URL resolving the current context is not yet available.
   * </p>
   * <dl>
   * <dt><strong>Note:</strong></dt>
   * <dd>
   * Using this method in your test requires to mark the test as {@code DirtiesContext}.
   * </dd>
   * </dl>
   */
  private void mockCurrentContext() {
    CurrentContextService ccs = mock(CurrentContextService.class);
    when(ccs.getContext()).thenReturn(media);
    contextHelper.setCurrentContextService(ccs);
  }

  // --- Link Building ----------------------------------------------

  @Test
  public void testSiteTaxonomyLink() throws Exception {
    CMLinkable siteTaxonomy = contentTestHelper.getContentBean(4250);
    String url = createRelativeShortUrl(siteTaxonomy);
    assertEquals("Bad topicpage url for site taxonomy", hamburgUrl, url);
  }

  @Test
  @DirtiesContext
  public void testGlobalTaxonomyLink() throws Exception {
    mockCurrentContext();

    // test
    CMLinkable globalTaxonomy = contentTestHelper.getContentBean(304);
    String url = createRelativeShortUrl(globalTaxonomy);
    assertEquals("Bad topicpage url for global taxonomy", holidayUrl, url);
  }

  @Test
  @DirtiesContext
  public void testCustomTopicpage() throws Exception {
    mockCurrentContext();

    // test
    CMLinkable globalTaxonomy = contentTestHelper.getContentBean(306);
    String url = createRelativeShortUrl(globalTaxonomy);
    assertEquals("Bad topicpage url for custom topicpage", lifestyleUrl, url);
  }


  // --- Request Handling -------------------------------------------

  @Test
  public void testSiteTopicpageRequest() throws Exception {
    ModelAndView mav = requestTestHelper.request(hamburgUrl);
    HandlerTestUtil.checkPage(mav, 4280, 4280); //the topic page channel is rendered for taxonomies
  }

  @Test
  public void testGlobalTopicpageRequest() throws Exception {
    ModelAndView mav = requestTestHelper.request(holidayUrl);
    HandlerTestUtil.checkPage(mav, 4280, 4280); //the topic page channel is rendered for taxonomies
  }

  @Test
  public void testCustomTopicpageRequest() throws Exception {
    ModelAndView mav = requestTestHelper.request(lifestyleUrl);
    HandlerTestUtil.checkPage(mav, 600, 600);
  }

  @Test
  public void testBadSegmentRequest() throws Exception {
    ModelAndView mav = requestTestHelper.request("/media/topic/badsegment" + idSeparator + "4250");
    HandlerTestUtil.checkModelAndView(mav, "redirect:DEFAULT", CMTaxonomy.class);
  }

  @Test
  public void testBadIdRequest() throws Exception {
    ModelAndView mav = requestTestHelper.request("/media/topic/hamburg" + idSeparator + "4252");
    HandlerTestUtil.checkError(mav, 404);
  }

  @Test
  public void testBadTopicRequest() throws Exception {
    ModelAndView mav = requestTestHelper.request("/media/badtopic/hamburg" + idSeparator + "4250");
    HandlerTestUtil.checkError(mav, 404);
  }


  // --- Channel ----------------------------------------------------

  // This section should rather be part of CMChannelImplTest, but here
  // we have the appropriate content to test the inheritance fallback
  // to the site's root context.

  @Test
  public void testCssInheritance() {
    List<? extends CMCSS> css = topicpageChannel.getCss();
    assertEquals("wrong number of csss", 1, css.size());
    assertEquals("wrong css", 584, css.get(0).getContentId());
  }

  @Test
  public void testJsInheritance() {
    List<? extends CMJavaScript> javaScript = topicpageChannel.getJavaScript();
    assertEquals("wrong number of javascripts", 1, javaScript.size());
    assertEquals("wrong css", 552, javaScript.get(0).getContentId());
  }


  // --- internal ---------------------------------------------------

  private String createRelativeShortUrl(Object bean) {
    BlueprintMockRequestUtil.setRequestWithContext(request, media);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    return linkFormatter.formatLink(bean, null, request, response, false);
  }

}
