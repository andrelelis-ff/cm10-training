package com.coremedia.ecommerce.studio.rest.configuration;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesAutoConfiguration;
import com.coremedia.ecommerce.studio.rest.filter.EcStudioFilters;
import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.rest.invalidations.SimpleInvalidationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        EcStudioFilters.class,
        CapRestServiceConfiguration.class,
        BaseCommerceServicesAutoConfiguration.class
})
@ComponentScan(basePackages = "com.coremedia.ecommerce.studio.rest")
public class ECommerceStudioConfiguration {

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  SimpleInvalidationSource pushStateInvalidationSource() {
    SimpleInvalidationSource simpleInvalidationSource = new SimpleInvalidationSource();
    simpleInvalidationSource.setId("pushStateInvalidationSource");
    simpleInvalidationSource.setCapacity(1000);
    return simpleInvalidationSource;
  }
}
