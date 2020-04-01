package com.coremedia.blueprint.studio.rest.intercept;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.base.rest.intercept.ChannelWriteInterceptor;
import com.coremedia.blueprint.base.rest.intercept.SiteWriteInterceptor;
import com.coremedia.blueprint.themeimporter.MapToStructAdapter;
import com.coremedia.blueprint.themeimporter.SettingsJsonToMapAdapter;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SiteModel;
import com.coremedia.cap.themeimporter.ThemeImporter;
import com.coremedia.image.ImageDimensionsExtractor;
import com.coremedia.rest.cap.configuration.ConfigurationPublisher;
import com.coremedia.rest.cap.intercept.BlobFilenameWriteInterceptor;
import com.coremedia.rest.cap.intercept.PictureUploadInterceptor;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.coremedia.transform.BlobTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(
        value = {
                "classpath:/com/coremedia/blueprint/base/links/bpbase-links-services.xml",
                "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
                "classpath:/com/coremedia/cap/transform/transform-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
class InterceptorsStudioConfiguration {

  @Bean
  public ChannelWriteInterceptor channelWriteInterceptor(@Value("CMLinkable") ContentType contentType,
                                                         UrlPathFormattingHelper urlPathFormattingHelper) {
    ChannelWriteInterceptor channelWriteInterceptor = new ChannelWriteInterceptor();
    channelWriteInterceptor.setType(contentType);
    channelWriteInterceptor.setInterceptingSubtypes(true);
    channelWriteInterceptor.setPropertyName("segment");
    channelWriteInterceptor.setUrlPathFormattingHelper(urlPathFormattingHelper);
    return channelWriteInterceptor;
  }

  @Bean
  public SiteWriteInterceptor siteWriteInterceptor(@Value("CMSite") ContentType contentType,
                                                   SiteModel siteModel) {
    SiteWriteInterceptor siteWriteInterceptor = new SiteWriteInterceptor();
    siteWriteInterceptor.setType(contentType);
    siteWriteInterceptor.setSiteModel(siteModel);
    return siteWriteInterceptor;
  }

  @Bean
  public PictureUploadInterceptor pictureUploadInterceptor(@Value("CMPicture") ContentType contentType,
                                                           BlobTransformer blobTransformer,
                                                           ImageDimensionsExtractor imageDimensionsExtractor) {
    PictureUploadInterceptor pictureUploadInterceptor = new PictureUploadInterceptor();
    pictureUploadInterceptor.setPriority(2);
    pictureUploadInterceptor.setType(contentType);
    pictureUploadInterceptor.setImageProperty("data");
    pictureUploadInterceptor.setWidthProperty("width");
    pictureUploadInterceptor.setHeightProperty("height");
    // uploadLimit: max image size (width * height) in pixels. Images are not uploaded if too big to prevent
    // OutOfMemoryExceptions.
    pictureUploadInterceptor.setUploadLimit(100000000);
    // maxDimension: max width and height in pixels of stored images in the database. Images are scaled down
    // if too big.
    pictureUploadInterceptor.setMaxDimension(4000);
    pictureUploadInterceptor.setBlobTransformer(blobTransformer);
    pictureUploadInterceptor.setExtractor(imageDimensionsExtractor);
    return pictureUploadInterceptor;
  }

  @Bean
  ThemeUploadInterceptor themeUploadInterceptor(@Value("CMDownload") ContentType contentType,
                                                ThemeImporter themeImporter) {
    ThemeUploadInterceptor themeUploadInterceptor = new ThemeUploadInterceptor("data", themeImporter);
    themeUploadInterceptor.setType(contentType);
    return themeUploadInterceptor;
  }

  @Bean
  BlobFilenameWriteInterceptor blobFilenameWriteInterceptor(@Value("CMDownload") ContentType contentType) {
    BlobFilenameWriteInterceptor blobFilenameWriteInterceptor = new BlobFilenameWriteInterceptor();
    blobFilenameWriteInterceptor.setType(contentType);
    blobFilenameWriteInterceptor.setBlobProperty("data");
    blobFilenameWriteInterceptor.setFilenameProperty("filename");
    return blobFilenameWriteInterceptor;
  }

  @Bean
  WordUploadInterceptor wordUploadInterceptor(@Value("CMArticle") ContentType contentType,
                                              ContentRepository contentRepository) {
    WordUploadInterceptor wordUploadInterceptor = new WordUploadInterceptor(contentRepository);
    wordUploadInterceptor.setType(contentType);
    wordUploadInterceptor.setPriority(1);
    return wordUploadInterceptor;
  }

  @Bean
  SettingsUploadInterceptor settingsUploadInterceptor(SettingsJsonToMapAdapter settingsJsonToMapAdapter,
                                                      MapToStructAdapter mapToStructAdapter,
                                                      @Value("CMDownload") ContentType contentType) {
    SettingsUploadInterceptor settingsUploadInterceptor = new SettingsUploadInterceptor(
            "data",
            "CMSettings",
            "settings",
            settingsJsonToMapAdapter,
            mapToStructAdapter
    );
    settingsUploadInterceptor.setType(contentType);
    return settingsUploadInterceptor;
  }

  /**
   * Makes the theme upload configuration available on client at editorContext.getConfiguration().themeUpload
   */
  @Bean
  ConfigurationPublisher themeUploadConfigurationPublisher(ThemeConfiguration themeUploadConfiguration) {
    ConfigurationPublisher themeUploadConfigurationPublisher = new ConfigurationPublisher();
    themeUploadConfigurationPublisher.setName("themeUpload");
    themeUploadConfigurationPublisher.setConfiguration(themeUploadConfiguration);
    return themeUploadConfigurationPublisher;
  }

  @Bean
  ThemeConfiguration themeUploadConfiguration(@Value("${themeImporter.themeDeveloperGroups:}") String developerGroups) {
    return new ThemeConfiguration(developerGroups);
  }
}
