package com.coremedia.blueprint.boot.autoconfigure;

import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({LegacyCookieProcessor.class})
public class LegacyCookieProcessorConfiguration {

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
    return container -> container.addContextCustomizers(context -> context.setCookieProcessor(new LegacyCookieProcessor()));
  }
}
