package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMAbstractCode;
import com.coremedia.blueprint.common.contentbeans.CMJavaScript;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.MergeableResources;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
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

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PageCodeResourcesTest.PageCodeResourcesTestConfiguration.class)
public class PageCodeResourcesTest {
  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/blueprint/cae/contentbeans/pagecoderesources/pagecoderesources-content.xml";

  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(value = {
          "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
          "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
          "classpath:/framework/spring/blueprint-contentbeans.xml",
          "classpath:/framework/spring/blueprint-contentbeans-settings.xml",
          "classpath:/com/coremedia/blueprint/base/tree/bpbase-treerelation-services.xml"
  }, reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class)
  static class PageCodeResourcesTestConfiguration {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY_URL)  ;
    }
  }

  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContentBeanFactory contentBeanFactory;
  @Inject
  private Cache cache;
  @Inject
  private SitesService sitesService;
  @Resource(name="navigationTreeRelation")
  private TreeRelation<Content> treeRelation;

  @Before
  public void setup() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(11));
  }

  @After
  public void teardown() {
    // make sure that tests do not interfere with each other via thread locals!
    // (Just C&P'ed from another test, not sure if it is necessary.)
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testDirectHeadJs() {
    PageImpl page = createPage(20);
    List<?> headJavaScript = page.getHeadJavaScript();
    assertEquals(1, headJavaScript.size());
    Object code = headJavaScript.get(0);
    assertTrue(code instanceof MergeableResources);
    List<CMAbstractCode> resources = ((MergeableResources) code).getMergeableResources();
    assertEquals(1, resources.size());
    assertEquals(50, resources.get(0).getContentId());
  }

  @Test
  public void testDirectJs() {
    PageImpl page = createPage(20);
    List<?> javaScript = page.getJavaScript();
    assertEquals(2, javaScript.size());
    // Order by contract: external links first.
    assertEquals(58, ((CMJavaScript)javaScript.get(0)).getContentId());
    List<CMAbstractCode> mergeableResources = ((MergeableResources) javaScript.get(1)).getMergeableResources();
    assertEquals(2, mergeableResources.size());
    // Order preserved from linklist.
    assertEquals(52, (mergeableResources.get(0)).getContentId());
    assertEquals(54, (mergeableResources.get(1)).getContentId());
  }

  @Test
  public void testDirectIeJs() {
    PageImpl page = createPage(20);
    List<?> javaScript = page.getInternetExplorerJavaScript();
    assertEquals(1, javaScript.size());
    assertEquals(56, ((CMJavaScript)javaScript.get(0)).getContentId());
  }

  @Test
  public void doNotInheritDirectCode() {
    assumeTrue(20 == contentBean(22, CMNavigation.class).getParentNavigation().getContext().getContentId());
    assumeTrue(!createPage(20).getJavaScript().isEmpty());
    PageImpl page = createPage(22);
    assertTrue(page.getJavaScript().isEmpty());
  }


  // --- internal ---------------------------------------------------

  private PageImpl createPage(int id) {
    CMNavigation channel = contentBean(id, CMNavigation.class);
    return new PageImpl(channel, channel, false, sitesService, cache, treeRelation, contentBeanFactory, null);
  }

  private <T> T contentBean(int id, Class<T> expectedType) {
    Content content = contentRepository.getContent(IdHelper.formatContentId(id));
    return contentBeanFactory.createBeanFor(content, expectedType);
  }
}
