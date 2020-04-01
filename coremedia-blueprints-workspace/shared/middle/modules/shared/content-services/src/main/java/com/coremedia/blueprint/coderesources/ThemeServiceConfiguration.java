package com.coremedia.blueprint.coderesources;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cap.content.Content;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(
        value = {
                "classpath:/com/coremedia/blueprint/base/tree/bpbase-treerelation-services-defaults.properties"
        }
)
@ImportResource(
        value = {
                "classpath:/com/coremedia/blueprint/base/tree/bpbase-treerelation-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class ThemeServiceConfiguration {

  @Bean
  public ThemeService themeService(TreeRelation<Content> navigationTreeRelation) {
    return new ThemeService(navigationTreeRelation);
  }

}
