package com.coremedia.blueprint.assets.studio;
import com.coremedia.blueprint.assets.studio.intercept.LinkedAssetMetadataExtractorInterceptor;
import com.coremedia.blueprint.assets.studio.intercept.UpdateAssetMetadataWriteInterceptor;
import com.coremedia.blueprint.assets.studio.upload.AMDoctypeRewriteUploadInterceptor;
import com.coremedia.blueprint.assets.studio.validation.AssetMetadataValidator;
import com.coremedia.blueprint.base.rest.BlueprintBaseStudioRestConfiguration;
import com.coremedia.blueprint.base.rest.config.ConfigurationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.rest.cap.config.StudioConfigurationProperties;
import com.coremedia.rest.cap.configuration.ConfigurationPublisher;
import com.coremedia.rest.cap.validation.ContentTypeValidator;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.lang.NonNull;

import java.util.Collections;

@Configuration
@ImportResource(value = {"classpath:/com/coremedia/cap/common/uapi-services.xml"},
        reader = ResourceAwareXmlBeanDefinitionReader.class)
@Import({BlueprintBaseStudioRestConfiguration.class})
@EnableConfigurationProperties({
        StudioConfigurationProperties.class
})
public class AMStudioRestConfiguration {

  private static final String CM_PICTURE_DOCTYPE = "CMPicture";
  private static final String CM_VIDEO_DOCTYPE = "CMVideo";
  private static final String CM_ARTICLE_DOCTYPE = "CMArticle";
  private static final String AM_PICTURE_ASSET_DOCTYPE = "AMPictureAsset";
  private static final String AM_VIDEO_ASSET_DOCTYPE = "AMVideoAsset";
  private static final String AM_DOCUMENT_ASSET_DOCTYPE = "AMDocumentAsset";

  /**
   * A write interceptor that reacts to writes on asset blob properties (renditions) by storing parsed rendition metadata.
   */
  @Bean
  UpdateAssetMetadataWriteInterceptor updateAssetMetadataWriteInterceptor(ContentRepository contentRepository) {
    UpdateAssetMetadataWriteInterceptor updateAssetMetadataWriteInterceptor = new UpdateAssetMetadataWriteInterceptor();
    updateAssetMetadataWriteInterceptor.setType(contentRepository.getContentType("AMAsset"));
    updateAssetMetadataWriteInterceptor.setInterceptingSubtypes(true);
    updateAssetMetadataWriteInterceptor.setMetadataProperty("metadata");
    updateAssetMetadataWriteInterceptor.setMetadataSourceProperty("original");
    return updateAssetMetadataWriteInterceptor;
  }

  /**
   * A write interceptor that reacts to writes on pictures that link to a picture asset.
   * The picture asset metadata are extracted and provided for subsequent interceptors
   */
  @Bean
  LinkedAssetMetadataExtractorInterceptor updatePictureMetadataWriteInterceptor(ContentRepository contentRepository) {
    LinkedAssetMetadataExtractorInterceptor linkedAssetMetadataExtractorInterceptor = new LinkedAssetMetadataExtractorInterceptor();

    linkedAssetMetadataExtractorInterceptor.setPriority(-1);
    linkedAssetMetadataExtractorInterceptor.setType(contentRepository.getContentType("CMPicture"));
    linkedAssetMetadataExtractorInterceptor.setInterceptingSubtypes(true);
    linkedAssetMetadataExtractorInterceptor.setAssetMetadataProperty("metadata");
    linkedAssetMetadataExtractorInterceptor.setAssetLinkProperty("asset");
    linkedAssetMetadataExtractorInterceptor.setLinkedAssetType("AMPictureAsset");

    return linkedAssetMetadataExtractorInterceptor;
  }


  /**
   * A write interceptor that reacts to writes on videos that link to a video asset.
   * The video asset metadata are extracted and provided for subsequent interceptors
   */
  @Bean
  LinkedAssetMetadataExtractorInterceptor updateVideoMetadataWriteInterceptor(ContentRepository contentRepository) {
    LinkedAssetMetadataExtractorInterceptor linkedAssetMetadataExtractorInterceptor = new LinkedAssetMetadataExtractorInterceptor();

    linkedAssetMetadataExtractorInterceptor.setPriority(-1);
    linkedAssetMetadataExtractorInterceptor.setType(contentRepository.getContentType("CMVideo"));
    linkedAssetMetadataExtractorInterceptor.setInterceptingSubtypes(true);
    linkedAssetMetadataExtractorInterceptor.setAssetMetadataProperty("metadata");
    linkedAssetMetadataExtractorInterceptor.setAssetLinkProperty("asset");
    linkedAssetMetadataExtractorInterceptor.setLinkedAssetType("AMVideoAsset");

    return linkedAssetMetadataExtractorInterceptor;
  }

  @Bean
  AssetManagementConfiguration assetManagementConfiguration(StudioConfigurationProperties studioConfigurationProperties) {
    AssetManagementConfiguration assetManagementConfiguration = new AssetManagementConfiguration();
    assetManagementConfiguration.setSettingsDocument(studioConfigurationProperties.getAssets().getSettingsDocument());
    return assetManagementConfiguration;
  }

  /**
   * Validator for AMAsset content.
   */
  @Bean
  ContentTypeValidator amAssetValidator(ConfigurationService configurationService,
                                        CapConnection connection,
                                        StudioConfigurationProperties studioConfigurationProperties) {
    ContentTypeValidator contentTypeValidator = new ContentTypeValidator();
    contentTypeValidator.setConnection(connection);
    contentTypeValidator.setContentType("AMAsset");
    contentTypeValidator.setValidatingSubtypes(true);

    AssetMetadataValidator assetMetadataValidator = new AssetMetadataValidator();
    assetMetadataValidator.setMetadataProperty("metadata");
    assetMetadataValidator.setConfigurationService(configurationService);
    assetMetadataValidator.setAssetManagementConfiguration(assetManagementConfiguration(studioConfigurationProperties));
    contentTypeValidator.setValidators(Collections.singletonList(assetMetadataValidator));

    return contentTypeValidator;
  }

  /**
   * Makes the asset management configuration available on client at
   * editorContext.getConfiguration().assetManagement.
   */
  @Bean
  ConfigurationPublisher assetManagementConfigurationPublisher(StudioConfigurationProperties studioConfigurationProperties) {
    ConfigurationPublisher configurationPublisher = new ConfigurationPublisher();
    configurationPublisher.setName("assetManagement");
    configurationPublisher.setConfiguration(assetManagementConfiguration(studioConfigurationProperties));
    return configurationPublisher;
  }

  @Bean
  public AMDoctypeRewriteUploadInterceptor amPictureUploadInterceptor(@NonNull ContentRepository contentRepository) {
    ContentType pictureDoctype = contentRepository.getContentType(CM_PICTURE_DOCTYPE);
    ContentType amPictureDoctype = contentRepository.getContentType(AM_PICTURE_ASSET_DOCTYPE);

    if (pictureDoctype == null || amPictureDoctype == null) {
      throw new IllegalStateException("Can not initialize UploadInterceptor. Missing Doctype " + CM_PICTURE_DOCTYPE + " or " + AM_PICTURE_ASSET_DOCTYPE);
    }

    return new AMDoctypeRewriteUploadInterceptor(pictureDoctype, amPictureDoctype);
  }

  @Bean
  public AMDoctypeRewriteUploadInterceptor amVideoUploadInterceptor(@NonNull ContentRepository contentRepository) {
    ContentType videoDoctype = contentRepository.getContentType(CM_VIDEO_DOCTYPE);
    ContentType amVideoDoctype = contentRepository.getContentType(AM_VIDEO_ASSET_DOCTYPE);
    if (videoDoctype == null || amVideoDoctype == null) {
      throw new IllegalStateException("Can not initialize UploadInterceptor. Missing Doctype " + CM_VIDEO_DOCTYPE + " or " + AM_VIDEO_ASSET_DOCTYPE);
    }

    return new AMDoctypeRewriteUploadInterceptor(videoDoctype, amVideoDoctype);
  }

  @Bean
  public AMDoctypeRewriteUploadInterceptor amDocumentUploadInterceptor(@NonNull ContentRepository contentRepository) {
    ContentType articleDoctype = contentRepository.getContentType(CM_ARTICLE_DOCTYPE);
    ContentType amDocumentDoctype = contentRepository.getContentType(AM_DOCUMENT_ASSET_DOCTYPE);
    if (articleDoctype == null || amDocumentDoctype == null) {
      throw new IllegalStateException("Can not initialize UploadInterceptor. Missing Doctype " + CM_ARTICLE_DOCTYPE + " or " + AM_DOCUMENT_ASSET_DOCTYPE);
    }

    return new AMDoctypeRewriteUploadInterceptor(articleDoctype, amDocumentDoctype);
  }
}
