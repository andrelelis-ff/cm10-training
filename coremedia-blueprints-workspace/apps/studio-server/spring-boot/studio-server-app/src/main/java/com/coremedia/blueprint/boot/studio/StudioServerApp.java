package com.coremedia.blueprint.boot.studio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * we need to exclude the autoconfiguration for the default springSecurityFilterChain to enable ours
 */
@EnableWebSecurity
@SpringBootApplication(exclude = { SecurityFilterAutoConfiguration.class, MongoAutoConfiguration.class})
  public class StudioServerApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(StudioServerApp.class, args);
  }
}
