package com.coremedia.blueprint.studio.rest.externalpreview;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalPreviewConfiguration {

  @Value("${externalpreview.restUrl}")
  String restUrl;

  @Value("${externalpreview.previewUrl}")
  String previewUrl;

  @Value("${externalpreview.urlPrefix}")
  String urlPrefix;

  @Bean
  ExternalPreviewResource externalPreviewResource() {
    return new ExternalPreviewResource(restUrl, previewUrl, urlPrefix);
  }
}
