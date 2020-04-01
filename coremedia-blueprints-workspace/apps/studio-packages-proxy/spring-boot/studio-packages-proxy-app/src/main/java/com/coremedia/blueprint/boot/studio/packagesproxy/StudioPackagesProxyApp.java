package com.coremedia.blueprint.boot.studio.packagesproxy;

import com.coremedia.blueprint.boot.studio.packagesproxy.dev.ProxyRestServiceConfiguration;
import com.coremedia.ui.dynamicpackages.servlet.DynamicPackagesListServlet;
import com.coremedia.ui.dynamicpackages.servlet.PackagesFilter;
import com.coremedia.ui.dynamicpackages.servlet.RemoteDynamicPackagesServlet;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Map;

@SpringBootApplication
@Import(ProxyRestServiceConfiguration.class)
public class StudioPackagesProxyApp {

  @Value("${studio.client.url:http://localhost:80}")
  private String studioClientUrl;

  @Bean
  public ServletRegistrationBean<RemoteDynamicPackagesServlet> remotePackagesServletRegistration() {
    ServletRegistrationBean<RemoteDynamicPackagesServlet> remoteDynamicPackagesServletServletRegistrationBean = new ServletRegistrationBean<>(new RemoteDynamicPackagesServlet(), RemoteDynamicPackagesServlet.URL_PATTERN + "*");

    Map<String, String> initParams = ImmutableMap.of(
            RemoteDynamicPackagesServlet.STUDIO_REST_PROXY_INIT_PARAM_FORWARDER, "true",
            RemoteDynamicPackagesServlet.STUDIO_REST_PROXY_INIT_PARAM_LOG, "false",
            RemoteDynamicPackagesServlet.STUDIO_REST_PROXY_INIT_PARAM_PRESERVE_COOKIES, "true",
            RemoteDynamicPackagesServlet.STUDIO_REST_PROXY_INIT_PARAM_CONNECT_TIMEOUT, "3000",
            RemoteDynamicPackagesServlet.STUDIO_REST_PROXY_INIT_PARAM_READ_TIMEOUT, "5000");
    remoteDynamicPackagesServletServletRegistrationBean.setInitParameters(initParams);

    return remoteDynamicPackagesServletServletRegistrationBean;
  }

  @Bean
  @ConditionalOnExpression("!'${studio.client.url}'.isEmpty()")
  public ServletRegistrationBean localPackagesServletRegistration() {
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean<>(new StudioClientResourcesProxyServlet(), "/*");
    servletRegistrationBean.addInitParameter("targetUri", studioClientUrl);
    return servletRegistrationBean;
  }

  @Bean
  public ServletRegistrationBean<DynamicPackagesListServlet> dynamicPackagesListServletRegistration() {
    String clientUrl = studioClientUrl != null && !studioClientUrl.isEmpty() ? studioClientUrl : null;
    return new ServletRegistrationBean<>(new DynamicPackagesListServlet(clientUrl), DynamicPackagesListServlet.URL_PATTERN);
  }

  @Bean
  public FilterRegistrationBean packagesFilterRegistration() {
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean<>(new PackagesFilter());
    filterRegistrationBean.addUrlPatterns(PackagesFilter.URL_PATTERN + "*");
    return filterRegistrationBean;
  }

  public static void main(String[] args) {
    SpringApplication.run(StudioPackagesProxyApp.class, args);
  }
}
