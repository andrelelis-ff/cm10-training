package com.coremedia.blueprint.boot.autoconfigure;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This configuration class can be used to add directories outside of the maven module/spring-boot jar at runtime
 */
@Configuration
@ConditionalOnClass({WebResourceRoot.class})
@Profile("local")
public class DevelopmentResourcesConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentResourcesConfiguration.class);

  @Bean
  @ConditionalOnProperty(name = "spring.boot.tomcat.extraResources")
  WebServerFactoryCustomizer<TomcatServletWebServerFactory> developmentResourcesConfigurer(@Value("${spring.boot.tomcat.extraResources:}") final String[] extraResources) {
    return container -> container.addContextCustomizers(context -> {
      WebResourceRoot resources = context.getResources();
      if (null == resources) {
        resources = new StandardRoot();
        context.setResources(resources);
      }

      // Allow linking to account for symlink resources
      resources.setAllowLinking(true);

      for (String dir : extraResources) {
        try {
          resources.createWebResourceSet(WebResourceRoot.ResourceSetType.PRE, "/", dir, null, "/");
          LOG.info("Added local web resource dir {}", dir);
        } catch (IllegalArgumentException e) {
          LOG.warn("Local web resource dir {} does not exist", dir);
        }
      }
    });
  }

}
