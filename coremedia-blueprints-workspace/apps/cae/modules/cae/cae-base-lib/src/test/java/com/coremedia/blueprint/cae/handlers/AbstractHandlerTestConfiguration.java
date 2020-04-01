package com.coremedia.blueprint.cae.handlers;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;

@ImportResource(
        value = {
                "classpath:/com/coremedia/cae/handler-services.xml",
                "classpath:/com/coremedia/mimetype/mimetype-service.xml",
                "classpath:/framework/spring/blueprint-contentbeans.xml",
        },
        reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class
)
abstract class AbstractHandlerTestConfiguration {

  @Bean
  MockMvc mockMvc(WebApplicationContext wac) {
    return MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Bean
  static BeanPostProcessor converterCustomizer(ApplicationContext applicationContext) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if ("bindingConverters".equals(beanName) && bean instanceof Collection) {
          //noinspection unchecked
          ((Collection)bean).add(applicationContext.getBean("idGenericContentBeanConverter"));
        }
        return bean;
      }
    };
  }

}
