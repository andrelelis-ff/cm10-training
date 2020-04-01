package com.coremedia.blueprint.studio.topicpages.rest;

import com.coremedia.blueprint.base.rest.BlueprintBaseStudioRestConfiguration;
import com.coremedia.blueprint.base.rest.config.ConfigurationService;
import com.coremedia.blueprint.taxonomies.TaxonomyConfiguration;
import com.coremedia.blueprint.taxonomies.TaxonomyResolver;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.rest.cap.config.StudioConfigurationProperties;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration
@ImportResource(
        value = {
                "classpath:/com/coremedia/cap/common/uapi-services.xml",
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
@Import({
        BlueprintBaseStudioRestConfiguration.class,
        TaxonomyConfiguration.class,
})
public class CustomTopicPagesConfiguration {

  @Bean
  @Scope("prototype")
  TopicPagesResource topicPagesResource(CapConnection connection,
                                        ConfigurationService configurationService,
                                        SitesService sitesService,
                                        TaxonomyResolver strategyResolver,
                                        StudioConfigurationProperties studioConfigurationProperties) {

    TopicPagesResource topicPagesResource = new TopicPagesResource(connection,
            configurationService,
            strategyResolver,
            sitesService,
            studioConfigurationProperties.getSiteConfigurationPath(),
            studioConfigurationProperties.getGlobalConfigurationPath());
    topicPagesResource.setIgnoredTaxonomies("Asset Download Portal");

    return topicPagesResource;
  }
}
