package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@ComponentScan
@Import({CapRestServiceConfiguration.class})
@ImportResource(value = {
        "classpath:META-INF/coremedia/elastic-social-rest-extension.xml",
        "classpath:/com/coremedia/blueprint/base/links/bpbase-links-services.xml",
        "classpath:/com/coremedia/blueprint/base/links/bpbase-links-postprocessors.xml",
        "classpath:com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@ComponentScan(basePackages = {
        "com.coremedia.elastic.core",
        "com.coremedia.elastic.social",
        "com.coremedia.blueprint.elastic.social"
})
public class ESBlueprintStudioConfiguration {
}
