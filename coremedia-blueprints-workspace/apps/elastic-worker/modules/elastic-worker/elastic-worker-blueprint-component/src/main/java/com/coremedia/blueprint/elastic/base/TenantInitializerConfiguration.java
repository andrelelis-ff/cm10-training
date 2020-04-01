package com.coremedia.blueprint.elastic.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
class TenantInitializerConfiguration {
  @Bean
  TenantInitializer tenantInitializer(){
    return new TenantInitializer();
  }
}
