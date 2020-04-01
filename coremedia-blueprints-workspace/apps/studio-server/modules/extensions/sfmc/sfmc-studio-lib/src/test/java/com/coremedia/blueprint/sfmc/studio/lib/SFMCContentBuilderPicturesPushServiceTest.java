package com.coremedia.blueprint.sfmc.studio.lib;

import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.ContentBuilderPushStateListener;
import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.SFMCContentBuilderPushService;
import com.coremedia.blueprint.base.sfmc.libservices.contentbuilder.documents.SFMCCategory;
import com.coremedia.blueprint.base.sfmc.libservices.context.SFMCContext;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SFMCContentBuilderPicturesPushServiceTest.LocalConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
class SFMCContentBuilderPicturesPushServiceTest {

  private static final SFMCContext CONTEXT = new SFMCContext("CLIENT_ID", "CLIENT_SECRET", "CUSTOMER_ID", "true");
  private static final String PICTURES_PROPERTY_NAME = "pictures";
  private static final String EXISTING_PROPERTY_NAME = "EXISTING_PROPERTY";
  private static final String NON_EXISTING_PROPERTY_NAME = "NON_EXISTING_PROPERTY";

  @Autowired
  @Qualifier("sfmcContentBuilderImagePushService")
  private SFMCContentBuilderPushService sfmcContentBuilderImagePushService;

  @Autowired
  @Qualifier("picturesSFMCContentBuilderPushService")
  private SFMCContentBuilderPushService testling;

  @Configuration
  @Profile("test")
  static class LocalConfig {

    @Bean
    @Primary
    @Qualifier("sfmcContentBuilderImagePushService")
    public SFMCContentBuilderPushService sfmcContentBuilderImagePushService() {
      return Mockito.mock(SFMCContentBuilderPushService.class);
    }

    @Bean
    @Primary
    @Qualifier("picturesSFMCContentBuilderPushService")
    public SFMCContentBuilderPushService picturesSFMCContentBuilderPushService() {
      return new SFMCContentBuilderPicturesPushService();
    }
  }

  @Test
  void isPushableExistingProperty() {
    Content inputContent = mock(Content.class);
    ContentType contentType = mock(ContentType.class);
    mockWithDescriptorWithType(inputContent, contentType, CapPropertyDescriptorType.LINK);

    boolean pushable = testling.isPushable(inputContent, PICTURES_PROPERTY_NAME);

    assertThat(pushable).isTrue();
  }

  @Test
  void isPushableNoExistingProperty() {
    Content inputContent = mock(Content.class);
    boolean pushable = testling.isPushable(inputContent, NON_EXISTING_PROPERTY_NAME);
    assertThat(pushable).isFalse();
  }

  @Test
  void isPushableNoBlobDescriptor() {
    Content inputContent = mock(Content.class);
    boolean pushable = testling.isPushable(inputContent, EXISTING_PROPERTY_NAME);
    assertThat(pushable).isFalse();
  }

  @Test
  void calculateAdditionalParts() {
    Content inputContent = mock(Content.class);
    int additionalPartsCount = testling.calculateAdditionalPartsCount(inputContent, EXISTING_PROPERTY_NAME);
    assertThat(additionalPartsCount).isEqualTo(0);
  }

  @Test
  void push() {
    testling = spy(testling);

    SFMCCategory inputCategory = mock(SFMCCategory.class);
    ContentBuilderPushStateListener pushListener = mock(ContentBuilderPushStateListener.class);
    Content inputContent = mock(Content.class);
    Blob blob = mock(Blob.class);
    when(inputContent.getBlob(PICTURES_PROPERTY_NAME)).thenReturn(blob);
    doReturn(true).when(testling).isPushable(inputContent, PICTURES_PROPERTY_NAME);

    testling.push(CONTEXT, inputCategory, inputContent, PICTURES_PROPERTY_NAME, pushListener);

    verify(testling, times(1)).push(CONTEXT, inputCategory, inputContent, PICTURES_PROPERTY_NAME, pushListener);
  }

  private void mockWithDescriptorWithType(@NonNull Content inputContent,
                                          @NonNull ContentType contentType,
                                          @NonNull CapPropertyDescriptorType descriptorType) {

    when(inputContent.getType()).thenReturn(contentType);
    CapPropertyDescriptor propertyDescriptor = mock(CapPropertyDescriptor.class);
    when(contentType.getDescriptor(PICTURES_PROPERTY_NAME)).thenReturn(propertyDescriptor);
    when(propertyDescriptor.getType()).thenReturn(descriptorType);
  }
}
