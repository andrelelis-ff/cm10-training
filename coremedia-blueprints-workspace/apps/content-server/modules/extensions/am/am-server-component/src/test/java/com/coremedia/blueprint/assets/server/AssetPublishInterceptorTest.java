package com.coremedia.blueprint.assets.server;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cms.server.plugins.PublishRequest;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressWarnings({"SpringJavaAutowiringInspection", "DuplicateStringLiteralInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AssetPublishInterceptorTest.LocalConfig.class)
@ActiveProfiles(AssetPublishInterceptorTest.LocalConfig.PROFILE)
public class AssetPublishInterceptorTest {

  @Inject
  private ContentRepository repository;

  @Mock
  private PublishRequest publishRequest;

  @Inject
  private AssetPublishInterceptor testling;

  private void setUp(String contentPath) throws Exception {
    MockitoAnnotations.initMocks(this);

    Content testContent = repository.getChild(contentPath);
    Map<String, Object> properties = new HashMap<>(testContent.getProperties());
    Mockito.when(publishRequest.getVersion()).thenReturn(testContent.getCheckedInVersion());
    Mockito.when(publishRequest.getProperties()).thenReturn(properties);
  }

  @Test
  public void testMarkedPublishable() throws Exception {
    setUp("AssetAllTrue");

    testling.intercept(publishRequest);
    assertPublished("web");
  }

  @Test
  public void testMarkedNotPublishable() throws Exception {
    setUp("Asset");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  public void testMarkedNotPublishableThumbnailOverride() throws Exception {
    setUp("AssetAllFalse");

    testling.intercept(publishRequest);
    assertPublished("thumbnail");
  }

  @Test
  public void testNoSuchRendition() throws Exception {
    setUp("Asset");

    testling.intercept(publishRequest);
    assertNotPublished("original");
  }

  @Test
  @DirtiesContext
  public void testNoSuchRenditionDefaultFalse() throws Exception {
    setUp("Asset");
    testling.setRemoveDefault(false);

    testling.intercept(publishRequest);
    assertPublished("original");
  }

  @Test
  public void testNotMarked() throws Exception {
    setUp("Asset");

    testling.intercept(publishRequest);
    assertNotPublished("print");
  }

  @Test
  @DirtiesContext
  public void testNotMarkedDefaultFalse() throws Exception {
    setUp("Asset");
    testling.setRemoveDefault(false);

    testling.intercept(publishRequest);
    assertPublished("print");
  }

  @Test
  public void testNoRenditions() throws Exception {
    setUp("AssetWithoutRenditions");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  public void testNoRenditionsThumbnailOverride() throws Exception {
    setUp("AssetWithoutRenditions");

    testling.intercept(publishRequest);
    Map<String, Object> properties = publishRequest.getProperties();
    assertNotNull(properties.get("thumbnail"));
  }

  @Test
  public void testNoMetadata() throws Exception {
    setUp("AssetWithoutMetadata");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  public void testNoMetadataThumbnailOverride() throws Exception {
    setUp("AssetWithoutMetadata");

    testling.intercept(publishRequest);
    Map<String, Object> properties = publishRequest.getProperties();
    assertNotNull(properties.get("thumbnail"));
  }

  @Test
  public void testBadMetadata() throws Exception {
    setUp("AssetWithBadRenditions");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  public void testBadMetadataThumbnailOverride() throws Exception {
    setUp("AssetWithBadRenditions");

    testling.intercept(publishRequest);
    Map<String, Object> properties = publishRequest.getProperties();
    assertNotNull(properties.get("thumbnail"));
  }

  private void assertPublished(String rendition) {
    Map<String, Object> properties = publishRequest.getProperties();
    assertEquals(publishRequest.getVersion().get(rendition), properties.get(rendition));
  }

  private void assertNotPublished(String rendition) {
    Map<String, Object> properties = publishRequest.getProperties();
    assertNull(properties.get(rendition));
  }

  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(
          value = "classpath:/META-INF/coremedia/component-am-server.xml",
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Profile(LocalConfig.PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "AssetPublishInterceptorTest";

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/assets/server/AssetPublishInterceptorTest-content.xml");
    }
  }
}
