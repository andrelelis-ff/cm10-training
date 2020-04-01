package com.coremedia.blueprint.taxonomies.cycleprevention;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaxonomyCyclePreventionConfiguration {

  @Bean
  TaxonomyCycleValidator taxonomyCycleValidator() {
    return new TaxonomyCycleValidatorImpl();
  }
}
