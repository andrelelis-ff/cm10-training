package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.xml.Markup;
import org.junit.After;
import org.junit.Before;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CMTeasableImplTest.TestConfiguration.class)
public class CMTeasableImplTest {
  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/blueprint/cae/contentbeans/teasableimpl/content.xml";

  private static final int ARTICLE_ID = 30;

  private CMArticle article;

  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Before
  public void setup() {
    article = getContentBean(ARTICLE_ID);
  }

  private <T> T getContentBean(int id) {
    Content content = contentRepository.getContent(IdHelper.formatContentId(id));
    return (T) contentBeanFactory.createBeanFor(content, ContentBean.class);
  }

  @After
  public void teardown() {
    // make sure that tests do not interfere with each other via thread locals!
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testTextAsParagraphs() throws Exception {
    assertNotNull(article);
    List<Markup> paragraphs = article.getTextAsParagraphs();
    assertEquals(2, paragraphs.size());
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
