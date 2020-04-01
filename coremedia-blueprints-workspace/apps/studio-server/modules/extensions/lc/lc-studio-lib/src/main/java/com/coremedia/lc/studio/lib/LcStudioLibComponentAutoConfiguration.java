package com.coremedia.lc.studio.lib;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ComponentScan
@Import({LcStudioValidatorsConfiguration.class, LcStudioPlacementsConfiguration.class})
@ImportResource(value = {
        "classpath:/META-INF/coremedia/lc-services.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LcStudioLibComponentAutoConfiguration {
}
