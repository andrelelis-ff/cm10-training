package com.coremedia.blueprint.sfmc.studio.lib;

import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.SFMCContentBuilderPushService;
import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.blob.images.BaseSFMCContentBuilderBlobConfiguration;
import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.blob.images.ContentTransformationOperationsResolver;
import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.markup.BaseSFMCContentBuilderMarkupConfiguration;
import com.coremedia.blueprint.base.sfmc.contentlib.contentbuilder.push.string.BaseSFMCContentBuilderStringConfiguration;
import com.coremedia.blueprint.base.sfmc.studio.rest.BaseSFMCStudioRestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BaseSFMCStudioRestConfiguration.class,
         BaseSFMCContentBuilderBlobConfiguration.class,
         BaseSFMCContentBuilderStringConfiguration.class,
         BaseSFMCContentBuilderMarkupConfiguration.class})
public class SFMCStudioLibConfiguration {

  @Bean
  public ContentTransformationOperationsResolver cmPictureTransformationOperationsResolver() {
    return new CMPictureTransformationOperationsResolver();
  }

  @Bean
  public SFMCContentBuilderPushService picturesSFMCContentBuilderPushService() {
    return new SFMCContentBuilderPicturesPushService();
  }
}
