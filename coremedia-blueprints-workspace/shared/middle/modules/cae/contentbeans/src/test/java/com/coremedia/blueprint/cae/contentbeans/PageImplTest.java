package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
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
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PageImplTest.PageImplTestConfiguration.class)
public class PageImplTest {
  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/blueprint/cae/contentbeans/pageimpl/pageimpltest-content.xml";

  private static final int ARTICLE_ID = 2;
  private static final int PARENT_CHANNEL_ID = 10;
  private static final int CHILD_CHANNEL_ID = 20;

  private CMChannel parentChannel;
  private CMChannel childChannel;
  private CMArticle article;

  @Inject
  private SitesService sitesService;
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Before
  public void setup() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(11));
    System.out.println("content = " + content.getName() + " - " + content.getPath());

    parentChannel = getContentBean(PARENT_CHANNEL_ID);
    childChannel = getContentBean(CHILD_CHANNEL_ID);
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

  @Test(expected = IllegalArgumentException.class)
  public void testNavigationNull() {
    new PageImpl(null, parentChannel, true, null, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testContentNull() {
    new PageImpl(parentChannel, null, true, null, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSitesServiceNull() {
    new PageImpl(parentChannel, parentChannel, true, null, null, null, null, null);
  }

  @Test
  public void testGetInstance() throws Exception {
    assertEquals(new PageImpl(parentChannel, parentChannel, true, sitesService, null, null, null, null),
                 new PageImpl(parentChannel, parentChannel, true, sitesService, null, null, null, null));
  }

  @Test
  public void testAssumeIdentity() throws Exception {
    PageImpl page = new PageImpl(parentChannel, parentChannel, true, sitesService, null, null, null, null);
    PageImpl anotherPage = new PageImpl(childChannel, childChannel, true, sitesService, null, null, null, null);
    anotherPage.assumeIdentity(page);

    assertTrue(anotherPage.equals(page));
  }

  @Test
  public void testGetNavigation() throws Exception {
    PageImpl page = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertEquals(parentChannel, page.getNavigation());
  }

  @Test
  public void testGetContent() throws Exception {
    PageImpl page = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertEquals(childChannel, page.getContent());
  }

  @Test
  public void testGetAspects() throws Exception {
    PageImpl page = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertEquals(0, page.getAspects().size());

  }

  @Test
  public void testGetAspectByName() throws Exception {
    PageImpl page = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertEquals(0, page.getAspectByName().size());
  }

  @Test
  public void testEquals() throws Exception {
    PageImpl page1 = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);
    PageImpl page2 = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertEquals("page1#equals(page2) must be true", page1, page2);
    assertEquals("page2#equals(page1) must be true", page2, page1);

    assertNotEquals("PageImpl#equals(null) must never be true",page1, null);
  }

  @Test
  public void testHashCode() throws Exception {
    PageImpl page1 = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);
    PageImpl page2 = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertEquals(page1.hashCode(), page2.hashCode());
  }

  @Test
  public void testIsDetailView() throws Exception {
    PageImpl page = new PageImpl(parentChannel, article, true, sitesService, null, null, null, null);

    assertTrue(page.isDetailView());

    page = new PageImpl(parentChannel, childChannel, true, sitesService, null, null, null, null);

    assertFalse(page.isDetailView());
  }

  @Test
  public void testGetLocale() throws Exception {
    PageImpl page = new PageImpl(parentChannel, article, true, sitesService, null, null, null, null);

    assertEquals(Locale.GERMAN, page.getLocale());

    page = new PageImpl(parentChannel, parentChannel, true, sitesService, null, null, null, null);

    assertEquals(Locale.GERMAN, page.getLocale());
  }

  @Test
  public void testGetKeywords() throws Exception {
    PageImpl page = new PageImpl(childChannel, childChannel, true, sitesService, null, null, null, null);
    assertEquals("key,word", page.getKeywords());

    page.setKeywords("content-Key,content-word");
    assertEquals("content-Key,content-word", page.getKeywords());
  }

  @Test
  public void testGetFavicon() throws Exception {
    PageImpl page = new PageImpl(parentChannel, article, true, sitesService, null, null, null, null);
    assertNotNull(page.getFavicon());
    Blob favicon = page.getFavicon();
    page = new PageImpl(childChannel, childChannel, true, sitesService, null, null, null, null);
    assertNotNull(page.getFavicon());
    assertEquals(favicon, page.getFavicon());
  }

  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(value = {
          "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
          "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
          "classpath:/framework/spring/blueprint-contentbeans.xml",
          "classpath:/framework/spring/blueprint-contentbeans-settings.xml"
  },
          reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class)
  static class PageImplTestConfiguration {

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY_URL)  ;
    }
  }
}
