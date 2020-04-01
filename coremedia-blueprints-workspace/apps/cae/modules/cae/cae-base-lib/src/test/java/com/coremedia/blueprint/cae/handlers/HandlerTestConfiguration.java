package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Configuration for handler tests. Requires test class to be annotated with
 * {@link org.springframework.test.context.web.WebAppConfiguration}.
 */
@Configuration
@ImportResource(
        value = {
                "classpath:/framework/spring/blueprint-handlers.xml",
        },
        reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class
)
@Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
public class HandlerTestConfiguration extends AbstractHandlerTestConfiguration {

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  LinkFormatterTestHelper linkFormatterTestHelper() {
    return new LinkFormatterTestHelper();
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  RequestTestHelper requestTestHelper() {
    return new RequestTestHelper();
  }

}
