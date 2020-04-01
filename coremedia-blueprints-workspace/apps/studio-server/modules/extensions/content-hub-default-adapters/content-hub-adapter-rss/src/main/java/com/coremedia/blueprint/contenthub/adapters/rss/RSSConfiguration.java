package com.coremedia.blueprint.contenthub.adapters.rss;

import com.coremedia.contenthub.api.ContentHubAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSSConfiguration {
  @Bean
  public ContentHubAdapterFactory rssContentHubAdapterFactory() {
    return new RSSContentHubAdapterFactory();
  }
}
