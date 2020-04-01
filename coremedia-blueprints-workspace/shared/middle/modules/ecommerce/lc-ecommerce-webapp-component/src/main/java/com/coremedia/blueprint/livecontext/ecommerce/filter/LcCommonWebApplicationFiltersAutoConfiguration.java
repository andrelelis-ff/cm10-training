package com.coremedia.blueprint.livecontext.ecommerce.filter;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.springframework.boot.web.servlet.RegistrationBeanBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Set up filter proxy for commerce connection filter
 */
@Configuration
public class LcCommonWebApplicationFiltersAutoConfiguration {

  private static final String COMMERCE_CONNECTION_FILTER = "commerceConnectionFilter";

  @Bean
  public FilterRegistrationBean createRegistrationBeans(CommerceConnectionInitializer commerceConnectionInitializer) {
    return RegistrationBeanBuilder
            .forFilter(new CommerceConnectionFilter(commerceConnectionInitializer))
            .name(COMMERCE_CONNECTION_FILTER)
            .order(2000)
            .build();
  }

}
