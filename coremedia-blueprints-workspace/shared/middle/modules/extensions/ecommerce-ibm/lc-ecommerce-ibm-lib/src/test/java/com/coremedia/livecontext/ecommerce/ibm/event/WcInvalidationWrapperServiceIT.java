package com.coremedia.livecontext.ecommerce.ibm.event;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Map;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static org.junit.Assert.assertNotNull;

@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_WcInvalidationWrapperServiceIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = {
        WcInvalidationWrapperServiceIT.LocalConfig.class,
        IbmServiceTestBase.LocalConfig.class
})
@ActiveProfiles({
        WcInvalidationWrapperServiceIT.PROFILE,
        IbmServiceTestBase.LocalConfig.PROFILE
})
public class WcInvalidationWrapperServiceIT extends IbmServiceTestBase {
  private static long TIME_STAMP = 1399641181799L;
  static final String PROFILE = "WcCacheWrapperServiceIT";

  @Configuration
  static class LocalConfig {

    @Bean
    @Autowired
    WcInvalidationWrapperService cacheWrapperService(WcRestConnector wcRestConnector) {
      WcInvalidationWrapperService wcCacheWrapperService = new WcInvalidationWrapperService();
      wcCacheWrapperService.setRestConnector(wcRestConnector);
      return wcCacheWrapperService;
    }
  }

  @Inject
  WcInvalidationWrapperService testling;

  String origServiceEndpoint;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();
    origServiceEndpoint = testling.getRestConnector().getServiceEndpoint(storeContext);
  }

  @AfterEach
  void reset() {
    testling.getRestConnector().setServiceEndpoint(origServiceEndpoint);
  }

  @Test
  void testGetCacheInvalidations() {
    if (useTapes()) {
      return;
    }

    Map<String, Object> cacheInvalidations = testling.getCacheInvalidations(TIME_STAMP, 20000, 500, storeContext);
    assertNotNull(cacheInvalidations);
  }

  @Test
  void testGetCacheInvalidationsUnknownHost() {
    if (testConfig.getWcsVersion().lessThan(WCS_VERSION_9_0)) {
      testling.getRestConnector().setServiceEndpoint("http://does.not.exists/blub");
    } else {
      testling.getRestConnector().setServiceSslEndpoint("https://does.not.exists/blub");
    }
    Assertions.assertThrows(CommerceException.class, () -> {
      testling.getCacheInvalidations(TIME_STAMP, 20000, 500, storeContext);
    });
  }

  @Test
  void testGetCacheInvalidationsError404() {
    if (testConfig.getWcsVersion().lessThan(WCS_VERSION_9_0)) {
      testling.getRestConnector().setServiceEndpoint(testling.getRestConnector().getServiceEndpoint(storeContext) + "/blub");
    } else {
      testling.getRestConnector().setServiceSslEndpoint(testling.getRestConnector().getServiceSslEndpoint(storeContext) + "/blub");
    }
    Assertions.assertThrows(CommerceException.class, () -> {
    testling.getCacheInvalidations(TIME_STAMP, 20000, 500, this.storeContext);
    });
  }
}
