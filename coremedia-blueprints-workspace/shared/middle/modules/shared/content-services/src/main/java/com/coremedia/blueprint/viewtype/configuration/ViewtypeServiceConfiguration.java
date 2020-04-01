package com.coremedia.blueprint.viewtype.configuration;

import com.coremedia.blueprint.viewtype.ViewtypeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViewtypeServiceConfiguration {
  @Bean(name="viewtypeService")
  public ViewtypeService viewtypeService() {
    return new ViewtypeService();
  }
}
