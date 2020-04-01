package com.coremedia.blueprint.elastic.base;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.settings.impl.SettingsServiceImpl;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.elastic.core.api.tenant.TenantServiceListener;
import com.coremedia.elastic.core.api.tenant.TenantServiceListenerBase;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        TenantInitializerTest.LocalConfig.class,
        com.coremedia.elastic.core.impl.tenant.TenantConfiguration.class,
        TenantInitializerConfiguration.class,
        XmlRepoConfiguration.class
})
public class TenantInitializerTest {

  @Inject
  private MyTenantServiceListenerBase myTenantServiceListenerBase;

  @Inject
  private SettingsService settingsService;


  @Test
  public void tenantsRegistered() throws InterruptedException {
    final String tenant = "tenant";
    final String testTenant = "testTenant";
    for (int i = 0; i < 5; i++) {
      synchronized (myTenantServiceListenerBase.monitor) {
        if (!(containsTenant(tenant) && containsTenant(testTenant))) {
          myTenantServiceListenerBase.monitor.wait(1000);
        }
      }
    }
    assertTrue(tenant, containsTenant(tenant));
    assertTrue(testTenant, containsTenant(testTenant));
  }

  private boolean containsTenant(String tenant) {
    return myTenantServiceListenerBase.tenants.contains(tenant);
  }

  @Configuration
  @ImportResource(value = {"classpath:META-INF/coremedia/component-elastic-worker.xml",
          "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml"},
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  public static class LocalConfig {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/testing/contenttest.xml");
    }

    @Bean
    public TenantServiceListener tenantServiceListener() {
      return new MyTenantServiceListenerBase();
    }

    @Bean
    public SettingsService settingsService() {
      return new SettingsServiceImpl();
    }

  }

  private static class MyTenantServiceListenerBase extends TenantServiceListenerBase {

    final Object monitor = new Object();
    Collection<String> tenants = new ArrayList<>();

    @Override
    public void onTenantRegistered(String tenant) {
      synchronized (monitor) {
        tenants.add(tenant);
        monitor.notifyAll();
      }
    }

  }
}
