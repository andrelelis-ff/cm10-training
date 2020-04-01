package com.coremedia.blueprint.elastic.social.cae;

import com.coremedia.springframework.boot.web.servlet.RegistrationBeanBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.Filter;

@Configuration
public class EsCaeFilters {
  private static final String SERVLET = "/servlet/*";
  private static final String SERVLET_DYNAMIC = "/servlet/dynamic/*";
  private static final String SERVLET_RESOURCE_ELASTIC = "/servlet/resource/elastic/*";

  // site filter is registered in com.coremedia.blueprint.component.cae.CaeBaseComponentConfiguration with order 100
  private static final int ORDER_SITE_FILTER = 100;

  @Bean
  public FilterRegistrationBean springSecurityFilterChainRegistration(Filter springSecurityFilterChain) {
    return RegistrationBeanBuilder
            .forFilter(springSecurityFilterChain)
            .order(Ordered.HIGHEST_PRECEDENCE)
            .build();
  }

  @Bean
  public FilterRegistrationBean sessionSiteFilterRegistration(Filter sessionSiteFilter) {
    return RegistrationBeanBuilder
            .forFilter(sessionSiteFilter)
            .urlPatterns(SERVLET_DYNAMIC)
            .order(ORDER_SITE_FILTER + 10)
            .build();
  }

  @Bean
  public FilterRegistrationBean tenantFilterRegistration(Filter tenantFilter) {
    return RegistrationBeanBuilder
            .forFilter(tenantFilter)
            .urlPatterns(SERVLET)
            .order(ORDER_SITE_FILTER + 20)
            .build();
  }

  @Bean
  public FilterRegistrationBean userFilterRegistration(Filter userFilter) {
    return RegistrationBeanBuilder
            .forFilter(userFilter)
            .urlPatterns(SERVLET_DYNAMIC, SERVLET_RESOURCE_ELASTIC)
            .order(ORDER_SITE_FILTER + 30)
            .build();
  }

}
