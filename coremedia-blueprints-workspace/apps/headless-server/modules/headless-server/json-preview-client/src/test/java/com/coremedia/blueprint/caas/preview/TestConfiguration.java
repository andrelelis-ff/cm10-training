package com.coremedia.blueprint.caas.preview;

import com.coremedia.blueprint.caas.preview.client.JsonPreviewApplication;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:json-preview-client.properties")
@Import({JsonPreviewApplication.class})
public class TestConfiguration {

  @Bean
  public CloseableHttpClient httpClient() {
    return Mockito.mock(CloseableHttpClient.class);
  }
}
