package com.coremedia.blueprint.studio.blueprint.component.preview;

import com.coremedia.rest.cap.configuration.ConfigurationPublisher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@EnableConfigurationProperties({
        PreviewUrlServiceConfigurationProperties.class
})
public class PreviewUrlServiceConfiguration {

  @Bean
  public ConfigurationPublisher previewUrlServicesConfigurationPublisher(PreviewUrlServiceConfigurationProperties properties) {
    ConfigurationPublisher configurationPublisher = new ConfigurationPublisher();
    configurationPublisher.setName("previewUrlServices");
    configurationPublisher.setConfiguration(properties.getConfig());
    return configurationPublisher;
  }

  @Bean
  public ConfigurationPublisher previewUrlServiceEnabledConfigurationPublisher(PreviewUrlServiceConfigurationProperties properties) {
    ConfigurationPublisher configurationPublisher = new ConfigurationPublisher();
    configurationPublisher.setName("previewUrlService");
    configurationPublisher.setConfiguration(Collections.singletonMap("enable", properties.isEnabled()));
    return configurationPublisher;
  }
}
