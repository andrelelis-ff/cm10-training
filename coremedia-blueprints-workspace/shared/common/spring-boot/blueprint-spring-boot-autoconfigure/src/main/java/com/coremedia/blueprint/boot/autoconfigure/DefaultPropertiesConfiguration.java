package com.coremedia.blueprint.boot.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/com/coremedia/blueprint/boot/autoconfigure/default.properties")
public class DefaultPropertiesConfiguration {
}
