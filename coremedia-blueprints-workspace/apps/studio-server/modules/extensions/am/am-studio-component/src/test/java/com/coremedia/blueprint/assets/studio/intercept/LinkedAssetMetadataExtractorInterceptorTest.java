package com.coremedia.blueprint.assets.studio.intercept;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cms.assets.AssetConstants;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.impl.ContentWriteRequestImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LinkedAssetMetadataExtractorInterceptorTest.LocalConfig.class)
@ActiveProfiles(LinkedAssetMetadataExtractorInterceptorTest.LocalConfig.PROFILE)
public class LinkedAssetMetadataExtractorInterceptorTest {

  @Inject
  private ContentRepository repository;

  @Inject
  private LinkedAssetMetadataExtractorInterceptor pictureWriteInterceptor;
  @Inject
  private LinkedAssetMetadataExtractorInterceptor videoWriteInterceptor;

  @Test
  public void updatePictureWithLinkedAssetEnsureMetadataArePresent() throws Exception {
    Content asset = repository.getChild("/Assets/PicWithMetadataStruct");
    Content picture = repository.getChild("/Assets/PictureWithAssetLink");

    ContentWriteRequest contentWriteRequest = new ContentWriteRequestImpl(picture, picture.getParent(), picture.getName(),
            picture.getType(), Collections.EMPTY_MAP , null);

    pictureWriteInterceptor.intercept(contentWriteRequest);

    List<String> productIds = (List<String>)contentWriteRequest.getAttribute(AssetConstants.PRODUCT_IDS_ATTRIBUTE_NAME);
    assertNotNull(productIds);
    assertEquals(1, productIds.size());
    assertEquals("Hurz", productIds.get(0));
  }

  @Test
  public void updatePictureWithNewLinkedAsset() throws Exception {
    Content asset = repository.getChild("/Assets/Pic");
    Content picture = repository.getChild("/Assets/PictureWithAssetLink");

    Map<String, Object> properties = new HashMap<>();
    properties.put("asset", Collections.singletonList(asset));
    ContentWriteRequest contentWriteRequest = new ContentWriteRequestImpl(picture, picture.getParent(), picture.getName(),
            picture.getType(), properties , null);

    pictureWriteInterceptor.intercept(contentWriteRequest);

    List<String> productIds = (List<String>)contentWriteRequest.getAttribute(AssetConstants.PRODUCT_IDS_ATTRIBUTE_NAME);
    assertThat(productIds, is(empty()));
  }

  @Test
  public void updatePictureWithoutLinkedAsset() throws Exception {
    Content picture = repository.getChild("/Assets/PictureWithoutAssetLink");

    ContentWriteRequest contentWriteRequest = new ContentWriteRequestImpl(picture, picture.getParent(), picture.getName(),
            picture.getType(), Collections.EMPTY_MAP , null);

    pictureWriteInterceptor.intercept(contentWriteRequest);

    List<String> productIds = (List<String>)contentWriteRequest.getAttribute(AssetConstants.PRODUCT_IDS_ATTRIBUTE_NAME);
    assertNull(productIds);
  }

  @Test
  public void updateVideoWithLinkedVideoAsset() throws Exception {
    Content asset = repository.getChild("/Assets/VidWithMetadataStruct");
    Content video = repository.getChild("/Assets/VideoWithVideoAssetLink");

    ContentWriteRequest contentWriteRequest = new ContentWriteRequestImpl(
            video, video.getParent(), video.getName(), video.getType(), Collections.<String, Object>emptyMap(), null);

    videoWriteInterceptor.intercept(contentWriteRequest);

    List<String> productIds = (List<String>)contentWriteRequest.getAttribute(AssetConstants.PRODUCT_IDS_ATTRIBUTE_NAME);
    assertThat(productIds, contains("Hurz"));
  }

  @Test
  public void updatePictureWithLinkedVideoAsset() throws Exception {
    Content asset = repository.getChild("/Assets/VidWithMetadataStruct");
    Content picture = repository.getChild("/Assets/PictureWithVideoAssetLink");

    ContentWriteRequest contentWriteRequest = new ContentWriteRequestImpl(
            picture, picture.getParent(), picture.getName(), picture.getType(), Collections.<String, Object>emptyMap(), null);

    pictureWriteInterceptor.intercept(contentWriteRequest);

    List<String> productIds = (List<String>)contentWriteRequest.getAttribute(AssetConstants.PRODUCT_IDS_ATTRIBUTE_NAME);
    assertNull(productIds);
  }


  @Configuration
  @Import(XmlRepoConfiguration.class)
  @Profile(LocalConfig.PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "AssetWriteInterceptorTest";

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/assets/studio/intercept/AssetWriteInterceptorTest-content.xml");
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public LinkedAssetMetadataExtractorInterceptor pictureWriteInterceptor(ContentRepository repository) {
      LinkedAssetMetadataExtractorInterceptor linkedAssetMetadataExtractorInterceptor = new LinkedAssetMetadataExtractorInterceptor();
      linkedAssetMetadataExtractorInterceptor.setAssetMetadataProperty("metadata");
      linkedAssetMetadataExtractorInterceptor.setAssetLinkProperty("asset");
      linkedAssetMetadataExtractorInterceptor.setType(repository.getContentType("CMPicture"));
      linkedAssetMetadataExtractorInterceptor.setLinkedAssetType("AMPictureAsset");
      linkedAssetMetadataExtractorInterceptor.setInterceptingSubtypes(true);
      linkedAssetMetadataExtractorInterceptor.setPriority(-1);
      return linkedAssetMetadataExtractorInterceptor;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public LinkedAssetMetadataExtractorInterceptor videoWriteInterceptor(ContentRepository repository) {
      LinkedAssetMetadataExtractorInterceptor linkedAssetMetadataExtractorInterceptor = new LinkedAssetMetadataExtractorInterceptor();
      linkedAssetMetadataExtractorInterceptor.setAssetMetadataProperty("metadata");
      linkedAssetMetadataExtractorInterceptor.setAssetLinkProperty("asset");
      linkedAssetMetadataExtractorInterceptor.setType(repository.getContentType("CMVideo"));
      linkedAssetMetadataExtractorInterceptor.setLinkedAssetType("AMVideoAsset");
      linkedAssetMetadataExtractorInterceptor.setInterceptingSubtypes(true);
      linkedAssetMetadataExtractorInterceptor.setPriority(-1);
      return linkedAssetMetadataExtractorInterceptor;
    }
  }
}
