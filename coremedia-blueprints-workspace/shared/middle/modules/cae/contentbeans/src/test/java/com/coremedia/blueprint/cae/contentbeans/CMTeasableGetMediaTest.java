package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.blueprint.common.contentbeans.CMTeaser;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CMTeasableGetMediaTest.TestConfiguration.class)
public class CMTeasableGetMediaTest {
  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/blueprint/cae/contentbeans/teasablegetmedia/content.xml";

  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContentBeanFactory contentBeanFactory;

  @After
  public void teardown() {
    // make sure that tests do not interfere with each other via thread locals!
    RequestContextHolder.resetRequestAttributes();
  }


  // --- Tests ------------------------------------------------------

  @Test
  public void testDefaultTeasableGetMedia() {
    // CMArticleImpl inherits getMedia and fetchMediaWithRecursionDetection from
    // CMTeasableImpl.  If you override that, use an other subtype of
    // CMTeasable for this test.
    CMArticle article = getContentBean(30);
    assumeNotNull(article);
    List<CMMedia> media = article.getMedia();
    assertEquals(2, media.size());
    List<CMMedia> mediaWithRecursionDetection = article.fetchMediaWithRecursionDetection(new HashSet<>());
    assertEquals("Contract violation: getMedia() and fetchMediaWithRecursionDetection(<empty set>) must be equivalent.", media, mediaWithRecursionDetection);
  }

  /**
   * Test the recursion check.
   * <p>
   * Currently CMChannelImpl and CMTeaserImpl have getMedia implementations
   * with fallbacks to other content beans.
   */
  @Test
  public void testGetMediaRecursionCheck() {
    // These channel and teaser both have no media and therefore
    // fallback to each other vice versa.
    CMChannel channel = getContentBean(32);
    CMTeaser teaser = getContentBean(34);

    // Make sure that the pagegrid works, otherwise you would not encounter
    // the potential recursion.
    // S. CMChannelImpl.getMediaWithRecursionDetection
    List<?> mainItems = channel.getPageGrid().getMainItems();
    assumeTrue(mainItems.size() == 1);

    List<CMMedia> media = channel.getMedia();
    // Success: We are still here and did not end up in a stack overflow error.
    // Nothing particular to assert, though.
    // media should be empty, because if we found something, we would not have
    // encountered the potential infinite recursion anyway, which would make
    // this test meaningless.
    assumeTrue(media.isEmpty());

    // Same test for CMTeaser
    media = teaser.getMedia();
    assumeTrue(media.isEmpty());
  }


  // --- internal ---------------------------------------------------

  private <T> T getContentBean(int id) {
    Content content = contentRepository.getContent(IdHelper.formatContentId(id));
    return (T) contentBeanFactory.createBeanFor(content, ContentBean.class);
  }


  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(value = {
          "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
          "classpath:/framework/spring/blueprint-contentbeans.xml"
  },
          reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class)
  static class TestConfiguration {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY_URL)  ;
    }
  }
}
