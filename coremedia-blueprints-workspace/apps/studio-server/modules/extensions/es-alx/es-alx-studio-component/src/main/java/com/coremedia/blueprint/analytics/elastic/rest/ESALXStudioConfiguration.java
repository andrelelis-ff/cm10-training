package com.coremedia.blueprint.analytics.elastic.rest;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(value = {
        "classpath:/com/coremedia/cap/common/uapi-services.xml",
        "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
        "classpath:/META-INF/coremedia/es-alx-common.xml",
        "classpath:/META-INF/coremedia/es-alx-studio-component-context.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@ComponentScan(basePackages = {"com.coremedia.blueprint.analytics.elastic.rest", "com.coremedia.elastic.core.rest.impl"})
public class ESALXStudioConfiguration {}
