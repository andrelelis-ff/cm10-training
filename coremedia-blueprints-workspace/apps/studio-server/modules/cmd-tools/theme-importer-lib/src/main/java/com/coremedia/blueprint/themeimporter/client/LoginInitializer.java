package com.coremedia.blueprint.themeimporter.client;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

class LoginInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private String iorUrl;
  private String name;
  private String domain;
  private String password;

  LoginInitializer(String iorUrl, String name, String domain, String password) {
    this.iorUrl = iorUrl;
    this.name = name;
    this.domain = domain;
    this.password = password;
  }

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    Map<String, Object> credentials = new HashMap<>();
    optionalPut(credentials, "repository.url", iorUrl);
    optionalPut(credentials, "repository.user", name);
    optionalPut(credentials, "repository.domain", domain);
    optionalPut(credentials, "repository.password", password);
    MapPropertySource propertySource = new MapPropertySource("credentials", credentials);
    configurableApplicationContext.getEnvironment().getPropertySources().addLast(propertySource);
  }


  // --- internal ---------------------------------------------------

  private static void optionalPut(Map<String, Object> map, String key, String value) {
    if (value!=null) {
      map.put(key, value);
    }
  }
}
