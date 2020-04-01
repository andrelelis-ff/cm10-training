package com.coremedia.blueprint.sfmc.preview.cae.p13n;

import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.customizer.Customize;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(reader = ResourceAwareXmlBeanDefinitionReader.class,
                value = {
                        "classpath:/com/coremedia/cae/contentbean-services.xml",
                }
)
public class SFMCP13NPreviewCAEConfiguration {

  @Bean
  @Customize("testContextExtractors")
  public JourneyTestContextExtractor journeyTestContextExtractor(@NonNull ContentBeanFactory contentBeanFactory) {
    return new JourneyTestContextExtractor(contentBeanFactory);
  }
}
