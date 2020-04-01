package com.coremedia.ecommerce.studio.rest.cache;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesAutoConfiguration;
import com.coremedia.rest.cap.config.StudioConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        BaseCommerceServicesAutoConfiguration.class
})
@EnableConfigurationProperties({
        StudioConfigurationProperties.class
})
public class CommerceCacheConfiguration {

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CommerceCacheInvalidationSource commerceCacheInvalidationSource(StudioConfigurationProperties studioConfigurationProperties) {
    CommerceCacheInvalidationSource commerceCacheInvalidationSource = new CommerceCacheInvalidationSource();
    commerceCacheInvalidationSource.setId("commerceInvalidationSource");
    commerceCacheInvalidationSource.setCapacity(studioConfigurationProperties.getRest().getCommerceCache().getCapacity());
    return commerceCacheInvalidationSource;
  }
}
