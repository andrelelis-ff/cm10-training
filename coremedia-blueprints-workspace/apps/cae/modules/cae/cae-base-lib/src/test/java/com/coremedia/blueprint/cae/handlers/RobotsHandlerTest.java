package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.view.RobotsView;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.robots.RobotsBean;
import com.coremedia.blueprint.common.robots.RobotsEntry;
import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.web.HttpError;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.cae.handlers.RobotsHandlerTest.LocalConfig.PROFILE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.LINK_FORMATTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * Tests for RobotsHandler and RobotsBean to ensure a proper Robots.txt is generated
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RobotsHandlerTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
@DirtiesContext(classMode = AFTER_CLASS)
@TestPropertySource(properties = "cae.is.standalone=false")
public class RobotsHandlerTest {

  @Configuration
  @ImportResource(
          value = {
                  LINK_FORMATTER,
                  "classpath:/framework/spring/blueprint-contentbeans.xml",
                  "classpath:/com/coremedia/cae/view-error-services.xml",
                  "classpath:/spring/test/dummy-views.xml",
                  "classpath:/framework/spring-test/blueprint-robots-test.xml",
                  "classpath:/framework/spring/blueprint-handlers.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "RobotsHandlerTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/testing/robots-test-content.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }

  }

  private static final String SEGMENT_MEDIA = "media";
  private static final String SEGMENT_INVALID = "invalid";

  private ModelAndView mavMedia;
  private ModelAndView mavInvalid;
  
  @Inject
  private ContentTestHelper contentTestHelper;
  @Inject
  private SettingsService settingsService;

  @Inject
  private RobotsHandler robotsHandler;

  @Inject
  private RobotsView robotsView;

  @Inject
  private MockHttpServletRequest request;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private MockHttpServletResponse response;

  @Before
  public void setUp() throws Exception {
    mavMedia = robotsHandler.handleRequest(SEGMENT_MEDIA);
    mavInvalid = robotsHandler.handleRequest(SEGMENT_INVALID);
  }

  @Test
  public void testHandleRequest() {

    RobotsBean robotsMedia = getValidRobotsBeanFromModel(mavMedia);
    assertEquals("expecting default view for media", "DEFAULT", mavMedia.getViewName());
    assertEquals("expecting 3 robots nodes for media", 3, robotsMedia.getRobotsEntries().size());

    Object self = mavInvalid.getModel().get("self");
    assertTrue("self object of invalid should be HttpError", self instanceof HttpError);
  }

  @Test
  public void testRobotsBean() {
    // test for root channel "Media":
    RobotsBean robotsMedia = getValidRobotsBeanFromModel(mavMedia);
    assertEquals("expecting 3 robots nodes for media", 3, robotsMedia.getRobotsEntries().size());

    CMChannel channelMedia = contentTestHelper.getContentBean(2);
    List<Map> settingsList = settingsService.settingAsList(RobotsBean.SETTINGS_NAME, Map.class, channelMedia);
    assertEquals("test content for robots settings expected to have 3 nodes configured!", 3, settingsList.size());

    RobotsEntry nodeOne = robotsMedia.getRobotsEntries().get(0);
    RobotsEntry nodeTwo = robotsMedia.getRobotsEntries().get(1);

    assertEquals("node one: user-agent", "*", nodeOne.getUserAgent());
    assertEquals("node one: disallow", 2, nodeOne.getDisallowed().size());
    assertEquals("node one: allow", 1, nodeOne.getAllowed().size());
    assertEquals("node one: custom", 2, nodeOne.getCustom().size());

    assertEquals("node two: user-agent", "Googlebot", nodeTwo.getUserAgent());
    assertEquals("node two: disallow", 2, nodeTwo.getDisallowed().size());
    assertEquals("node two: allow", 0, nodeTwo.getAllowed().size());
    assertEquals("node two: custom", 0, nodeTwo.getCustom().size());
  }

  @Test
  @DirtiesContext
  public void testRobotsView() throws Exception {
    RobotsBean bean = getValidRobotsBeanFromModel(mavMedia);
    Writer writer = new StringWriter();
    CMChannel channel = contentTestHelper.getContentBean(2);
    request.setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, channel);
    request.setContextPath("/context");
    request.setServletPath("/servlet");

    robotsView.render(bean, null, writer, request, response);
    List<String> lines = new ArrayList<>();

    try (BufferedReader bufferedReader = new BufferedReader(new StringReader(writer.toString()))) {
      String line;

      while ((line = bufferedReader.readLine()) != null) {

        // empty lines do not matter for crawlers so no check on them required:
        if (!line.isEmpty()) {
          lines.add(line);
        }
      }
    }

    assertEquals("result size", 12, lines.size());
    int i = -1;
    assertEquals("user agent 1", "User-agent: *", lines.get(++i));
    assertEquals("disallow 1.1", "Disallow: /media/lifestyle", lines.get(++i));
    assertEquals("disallow 1.2", "Disallow: /media/lifestyle/", lines.get(++i));
    assertEquals("disallow 1.3", "Disallow: /media/sports", lines.get(++i));
    assertEquals("disallow 1.4", "Disallow: /media/sports/", lines.get(++i));
    assertEquals("allow 1.1", "Allow: /media/sports/title-beachsoccer-14", lines.get(++i));
    assertEquals("custom 1.1", "Crawl-delay: 10", lines.get(++i));
    assertEquals("custom 1.2", "Disallow: /*.asp$", lines.get(++i));
    assertEquals("user agent 2", "User-agent: Googlebot", lines.get(++i));
    assertEquals("disallow 2.1", "Disallow: /media/africa", lines.get(++i));
    assertEquals("disallow 2.2", "Disallow: /media/africa/", lines.get(++i));
    assertEquals("disallow 2.3", "Disallow: /media/africa/title-sanfrancisco-16", lines.get(++i));
//    assertEquals("sitemap", "Sitemap: http://localhost/sitemap_index.xml", lines.get(++i));
  }

  private RobotsBean getValidRobotsBeanFromModel(ModelAndView modelAndView) {

    assertNotNull("modelAndView must not be null", modelAndView);
    Object self = modelAndView.getModel().get("self");
    assertTrue("self expected to be of type RobotsBean", self instanceof RobotsBean);
    return (RobotsBean) self;
  }

}
