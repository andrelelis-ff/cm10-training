package com.coremedia.livecontext.ecommerce.ibm.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * expose the library's default configuration
 */
@Configuration
@PropertySource("classpath:framework/spring/lc-ecommerce-ibm.properties")
class LcEcommerce_IBM_Configuration {

}


