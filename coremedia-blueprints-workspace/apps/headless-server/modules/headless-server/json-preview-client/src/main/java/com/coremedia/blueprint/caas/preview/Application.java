package com.coremedia.blueprint.caas.preview;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@DefaultAnnotation(NonNull.class)
@Configuration
@PropertySource("json-preview-client.properties")
public class Application {

  @Bean
  public CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }
}