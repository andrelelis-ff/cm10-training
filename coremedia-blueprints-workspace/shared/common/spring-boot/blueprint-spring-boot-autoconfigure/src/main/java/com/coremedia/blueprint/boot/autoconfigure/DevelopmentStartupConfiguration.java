package com.coremedia.blueprint.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This configuration class can be used to add directories outside of the maven module/spring-boot jar at runtime
 */
@Component
@Profile("local")
public class DevelopmentStartupConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentStartupConfiguration.class);

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("http://localhost:${server.port:8080}${server.servlet.context-path:}")
  private String url;

  @EventListener
  public void handleContextRefresh(ContextRefreshedEvent event) {
    LOG.info("{} successfully started at {}", applicationName, url);
  }

}
