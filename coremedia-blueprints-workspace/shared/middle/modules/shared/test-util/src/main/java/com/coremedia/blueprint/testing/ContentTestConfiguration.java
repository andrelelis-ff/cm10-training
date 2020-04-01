package com.coremedia.blueprint.testing;

import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration
@ImportResource(
        value = "classpath:/framework/spring/blueprint-contentbeans.xml",
        reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class
)
@Import(XmlRepoConfiguration.class)
public class ContentTestConfiguration {
  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public ContentTestHelper contentTestHelper() {
    return new ContentTestHelper();
  }
}
