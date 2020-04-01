package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {IbmServiceTestBase.LocalConfig.class, AbstractWrapperServiceTestCase.LocalConfig.class})
@ActiveProfiles({IbmServiceTestBase.LocalConfig.PROFILE})
@TestPropertySource(properties = "livecontext.cache.invalidation.enabled:false")
public abstract class AbstractWrapperServiceTestCase {
  @Configuration
  @ImportResource(
          value = {
                  "classpath:/com.coremedia.livecontext.ecommerce.ibm.service/test-commerce-services-bod-customizers.xml",
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  public static class LocalConfig {

  }

  public static final String TEST_USER = "gstevens"; // Frequent Buyer, Male Customer

  @Inject
  protected IbmTestConfig testConfig;

  @Inject
  protected WcRestConnector wcRestConnector;

  @Inject
  protected StoreInfoService storeInfoService;

  public IbmTestConfig getTestConfig() {
    return testConfig;
  }

}
