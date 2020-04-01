package com.coremedia.livecontext.ecommerce.ibm.event;

import com.coremedia.livecontext.ecommerce.event.InvalidationService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(value = {
        "classpath:/framework/spring/livecontext-services.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@ComponentScan
class IbmEventConfiguration {

  @Value("${livecontext.ibm.wcs.cache.invalidation.maxWaitInMilliseconds:0}")
  private long maxWaitInMilliseconds;

  @Value("${livecontext.ibm.wcs.cache.invalidation.chunkSize:500}")
  private long chunkSize;

  @Bean
  @Autowired
  public InvalidationService invalidationService(WcRestConnector wcRestConnector) {
    WcInvalidationWrapperService wcCacheWrapperService = new WcInvalidationWrapperService();
    wcCacheWrapperService.setRestConnector(wcRestConnector);
    return new InvalidationServiceImpl(wcCacheWrapperService, maxWaitInMilliseconds, chunkSize);
  }

}
