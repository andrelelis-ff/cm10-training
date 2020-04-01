package com.coremedia.blueprint.headlessserver;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration enables the json preview client depending on the property "caas.preview"
 */
@Configuration
@ConditionalOnProperty(name = "caas.preview", havingValue = "true")
@ComponentScan("com.coremedia.blueprint.caas.preview")
public class JsonPreviewClientConfig {
}
