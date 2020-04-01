package com.coremedia.blueprint.assets.studio.intercept;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.impl.ContentWriteRequestImpl;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
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
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UpdateAssetMetadataWriteInterceptorTest.LocalConfig.class)
@ActiveProfiles(UpdateAssetMetadataWriteInterceptorTest.LocalConfig.PROFILE)
public class UpdateAssetMetadataWriteInterceptorTest {

  @Inject
  private ContentRepository repository;
  @Inject
  private CapConnection connection;

  private UpdateAssetMetadataWriteInterceptor interceptor;
  private Content testAsset;

  @Before
  public void setUp() throws Exception {
    interceptor = new UpdateAssetMetadataWriteInterceptor();
    interceptor.setMetadataProperty("metadata");
    interceptor.setMetadataSourceProperty("original");
    interceptor.setType(repository.getContentType("AMAsset"));
    interceptor.setInterceptingSubtypes(true);
    interceptor.setPriority(1);

    testAsset = repository.createChild("/Assets/testAsset", "AMPictureAsset", Collections.<String, Object>emptyMap());
  }

  @After
  public void tearDown() throws Exception {
    interceptor = null;

    testAsset.destroy();
    testAsset = null;
  }

  @Test
  public void addProductIdsToNullStruct() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put("original", createBlobWithProductIds());
    ContentWriteRequest writeRequest = createWriteRequest(properties);

    interceptor.intercept(writeRequest);

    Struct metadataStruct = (Struct) properties.get("metadata");
    List<String> productIds = CapStructHelper.getStrings(metadataStruct, "productIds");
    assertThat(productIds, Matchers.containsInAnyOrder("PC_RED_DRESS", "PC_GREEN_DRESS"));
  }

  @Test
  public void addProductIdsButDoNotOverrideOtherProperties() throws Exception {
    testAsset.set("metadata", createStructWithoutProductIds());

    Map<String, Object> properties = new HashMap<>();
    properties.put("original", createBlobWithProductIds());
    ContentWriteRequest writeRequest = createWriteRequest(properties);

    interceptor.intercept(writeRequest);

    Struct metadataStruct = (Struct) properties.get("metadata");
    List<String> productIds = CapStructHelper.getStrings(metadataStruct, "productIds");
    assertThat(productIds, Matchers.containsInAnyOrder("PC_RED_DRESS", "PC_GREEN_DRESS"));

    String Lorem = CapStructHelper.getString(metadataStruct, "Lorem");
    assertThat(Lorem, Matchers.equalTo("Ipsum"));
  }

  @Test
  public void doNotOverrideProductIds() throws Exception {
    testAsset.set("metadata", createStructWithProductIds());

    Map<String, Object> properties = new HashMap<>();
    properties.put("original", createBlobWithProductIds());
    ContentWriteRequest writeRequest = createWriteRequest(properties);

    interceptor.intercept(writeRequest);

    assertFalse(properties.containsKey("metadata"));
  }

  @Test
  public void ignoreNullBlob() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put("original", null);
    ContentWriteRequest writeRequest = createWriteRequest(properties);

    interceptor.intercept(writeRequest);

    assertFalse(properties.containsKey("metadata"));
  }

  @Test
  public void ignoreBlobWithoutMetadata() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put("original", createBlobWithoutProductIds());
    ContentWriteRequest writeRequest = createWriteRequest(properties);

    interceptor.intercept(writeRequest);

    assertFalse(properties.containsKey("metadata"));
  }

  @Test
  public void doNotCreateStructForBlobWithoutMetadata() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put("original", createBlobWithoutProductIds());
    ContentWriteRequest writeRequest = createWriteRequest(properties);

    interceptor.intercept(writeRequest);

    assertFalse(properties.containsKey("metadata"));
  }

  @Test (expected = IllegalStateException.class)
  public void afterPropertiesSet() throws Exception {
    UpdateAssetMetadataWriteInterceptor metadataWriteInterceptor = new UpdateAssetMetadataWriteInterceptor();
    metadataWriteInterceptor.setType(repository.getContentType("AMAsset"));
    metadataWriteInterceptor.afterPropertiesSet();
  }

  @Test (expected = IllegalStateException.class)
  public void afterPropertiesSetWithMetadata() throws Exception {
    UpdateAssetMetadataWriteInterceptor metadataWriteInterceptor = new UpdateAssetMetadataWriteInterceptor();
    metadataWriteInterceptor.setType(repository.getContentType("AMAsset"));
    metadataWriteInterceptor.setMetadataProperty("test");
    metadataWriteInterceptor.afterPropertiesSet();
  }

  @Test
  public void afterPropertiesSetWithAllMetadata() throws Exception {
    interceptor.afterPropertiesSet();
  }

  private Struct createStructWithProductIds() throws Exception {
    return createStructFromFile(new File(getClass().getResource("metadataWithProductIdStruct.xml").toURI()));
  }

  private Struct createStructWithoutProductIds() throws Exception {
    return createStructFromFile(new File(getClass().getResource("metadataWithoutProductIdStruct.xml").toURI()));
  }

  private Struct createStructFromFile(File file) throws Exception {
    byte[] bytes = Files.readAllBytes(file.toPath());
    Markup markup = MarkupFactory.fromString(new String(bytes));
    return connection.getStructService().fromMarkup(markup);
  }

  private Blob createBlobWithProductIds() throws Exception {
    return createBlobFromFile(new File(getClass().getResource("image-with-xmp-product-reference.jpg").toURI()), "image/jpeg");
  }

  private Blob createBlobWithoutProductIds() throws Exception {
    return createBlobFromFile(new File(getClass().getResource("image-without-xmp-product-reference.jpg").toURI()), "image/jpeg");
  }

  private Blob createBlobFromFile(File file, String mimeType) throws Exception {
    return connection.getBlobService().fromFile(file, mimeType);
  }

  private ContentWriteRequestImpl createWriteRequest(Map<String, Object> properties) {
    return new ContentWriteRequestImpl(testAsset, testAsset.getParent(), testAsset.getName(),
            testAsset.getType(), properties, null);
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
  }
}
