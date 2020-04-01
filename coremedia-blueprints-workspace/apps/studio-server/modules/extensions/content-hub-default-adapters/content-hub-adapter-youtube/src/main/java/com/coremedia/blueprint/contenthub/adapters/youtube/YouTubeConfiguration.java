package com.coremedia.blueprint.contenthub.adapters.youtube;

import com.coremedia.contenthub.api.ContentHubAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YouTubeConfiguration {
  @Bean
  public ContentHubAdapterFactory youTubeContentHubAdapterFactory() {
    return new YouTubeContentHubAdapterFactory();
  }
}
