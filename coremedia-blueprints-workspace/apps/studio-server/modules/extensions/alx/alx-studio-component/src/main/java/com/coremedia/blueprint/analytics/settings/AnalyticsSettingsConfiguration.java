package com.coremedia.blueprint.analytics.settings;

import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
@Configuration
@ComponentScan(basePackages = "com.coremedia.blueprint.analytics.settings")
@Import({CapRestServiceConfiguration.class})
@ImportResource(value = {
        "classpath:/com/coremedia/cap/common/uapi-services.xml",
        "classpath:/com/coremedia/blueprint/base/links/bpbase-links-postprocessors.xml",
        "classpath:/com/coremedia/blueprint/base/links/bpbase-links-services.xml",
        "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
        "classpath:/com/coremedia/blueprint/segments/blueprint-segments.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
class AnalyticsSettingsConfiguration {
}
