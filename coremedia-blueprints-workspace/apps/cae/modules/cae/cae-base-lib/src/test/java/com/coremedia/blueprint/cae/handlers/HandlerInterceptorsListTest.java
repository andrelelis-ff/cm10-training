package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.web.ExposeCurrentNavigationInterceptor;
import com.coremedia.blueprint.cae.web.i18n.ResourceBundleInterceptor;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests sorting of interceptors that use the page or current navigation content for their work.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = HandlerInterceptorsListTest.LocalConfig.class)
@ActiveProfiles("HandlerInterceptorsListTest")
public class HandlerInterceptorsListTest {

  @Configuration
  @ImportResource(
          value = {
                  "classpath:/framework/spring/blueprint-handlers.xml",
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(XmlRepoConfiguration.class)
  @Profile("HandlerInterceptorsListTest")
  static class LocalConfig {
  }

  @Autowired
  private List<HandlerInterceptor> handlerInterceptors;

  @Autowired
  private ExposeCurrentNavigationInterceptor exposeCurrentNavigationInterceptor;

  @Autowired
  private ResourceBundleInterceptor resourceBundleInterceptor;

  @Test
  public void sorting() {
    assertThat(handlerInterceptors).containsOnlyOnce(resourceBundleInterceptor, exposeCurrentNavigationInterceptor);

    // resourceBundleInterceptor must have smaller index than exposeCurrentNavigationInterceptor
    assertThat(handlerInterceptors.indexOf(resourceBundleInterceptor)).
            isLessThan(handlerInterceptors.indexOf(exposeCurrentNavigationInterceptor));
  }
}
