package com.coremedia.blueprint.boot.studio.packagesproxy.dev;

import com.coremedia.ui.dynamicpackages.servlet.RestServiceProxyServlet;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
@Profile("local")
public class ProxyRestServiceConfiguration {

  @Bean
  public ServletRegistrationBean<RestServiceProxyServlet> restServiceProxyServletServletRegistration() {
    ServletRegistrationBean<RestServiceProxyServlet> restServiceProxyServletServletRegistrationBean = new ServletRegistrationBean<>(new RestServiceProxyServlet(), RestServiceProxyServlet.URL_PATTERN + "*");

    Map<String, String> initParams = ImmutableMap.of(
            RestServiceProxyServlet.STUDIO_REST_PROXY_INIT_PARAM_FORWARDER, "true",
            RestServiceProxyServlet.STUDIO_REST_PROXY_INIT_PARAM_LOG, "false",
            RestServiceProxyServlet.STUDIO_REST_PROXY_INIT_PARAM_PRESERVE_COOKIES, "true");
    restServiceProxyServletServletRegistrationBean.setInitParameters(initParams);

    return restServiceProxyServletServletRegistrationBean;
  }
}
