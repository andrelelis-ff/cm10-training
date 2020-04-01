package com.coremedia.blueprint.component.cae;

import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.blueprint.cae.filter.SecurityHeadersFilter;
import com.coremedia.blueprint.cae.filter.SiteFilter;
import com.coremedia.blueprint.cae.filter.UnknownMimetypeCharacterEncodingFilter;
import com.coremedia.springframework.boot.web.servlet.RegistrationBeanBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.Filter;
import java.nio.charset.StandardCharsets;

@Configuration
public class CaeBaseComponentConfiguration {

  public static final int ORDER_SITE_FILTER = 100;

  @Bean
  @ConditionalOnProperty(value="cae.set-unknown-mime-type")
  public FilterRegistrationBean characterEncodingFilterRegistration() {
    CharacterEncodingFilter filter = new UnknownMimetypeCharacterEncodingFilter();
    filter.setEncoding(StandardCharsets.UTF_8.toString());
    filter.setForceEncoding(true);
    return RegistrationBeanBuilder.forFilter(filter)
            .order(Ordered.HIGHEST_PRECEDENCE)
            .build();
  }

  @Bean
  public Filter siteFilter(SiteResolver siteResolver) {
    SiteFilter siteFilter = new SiteFilter();
    siteFilter.setSiteResolver(siteResolver);
    return siteFilter;
  }

  @Bean
  public FilterRegistrationBean siteFilterRegistration(Filter siteFilter) {
    return RegistrationBeanBuilder.forFilter(siteFilter)
            .urlPatterns("/servlet/*")
            .order(ORDER_SITE_FILTER)
            .build();
  }

  @Bean
  public Filter securityHeadersFilter(@Value("${cae.is.preview:false}") boolean caeIsPreview) {
    return new SecurityHeadersFilter(caeIsPreview);
  }

}
