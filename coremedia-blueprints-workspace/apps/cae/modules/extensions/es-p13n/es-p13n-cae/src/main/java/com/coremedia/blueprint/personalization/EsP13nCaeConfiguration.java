package com.coremedia.blueprint.personalization;

import com.coremedia.springframework.boot.web.servlet.RegistrationBeanBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class EsP13nCaeConfiguration {

  // user filter is initially registered in com.coremedia.blueprint.elastic.social.cae.EsCaeFilters with order 130
  private static final int ORDER_USER_FILTER = 130;

  @Bean
  public FilterRegistrationBean userFilterRegistrationCustomizer(Filter userFilter) {
    return RegistrationBeanBuilder
            .forFilter(userFilter)
            .urlPatterns("/servlet/userdetails/*")
            .order(ORDER_USER_FILTER + 10)
            .build();
  }
}
