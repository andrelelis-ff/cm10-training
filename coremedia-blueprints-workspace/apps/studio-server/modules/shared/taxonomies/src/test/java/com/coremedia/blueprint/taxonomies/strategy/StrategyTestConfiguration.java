package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@Configuration
@Import(XmlRepoConfiguration.class)
@ImportResource(
        value = "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class
)
class StrategyTestConfiguration {
  @Bean
  @Scope(SCOPE_SINGLETON)
  public XmlUapiConfig xmlUapiConfig() {
    return XmlUapiConfig
            .builder()
            .withSchema("classpath:com/coremedia/blueprint/base/testing/blueprint-doctypes-xmlrepo.xml")
            .build();
  }

  @Bean
  @Scope(SCOPE_SINGLETON)
  public TaxonomyCreator taxonomyCreator(ContentRepository contentRepository,
                                         SitesService sitesService) {
    return new TaxonomyCreator(contentRepository, sitesService);
  }
}
