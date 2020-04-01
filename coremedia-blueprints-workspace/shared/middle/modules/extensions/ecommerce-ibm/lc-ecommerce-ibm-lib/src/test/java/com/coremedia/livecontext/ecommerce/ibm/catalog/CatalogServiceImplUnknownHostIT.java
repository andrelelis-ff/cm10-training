package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.lc.test.CatalogServiceBaseTest;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmTestConfig;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.inject.Named;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This test has to run for its own (it manipulates the connector endpoint that interferes with other tests).
 */
@ExtendWith({SwitchableHoverflyExtension.class, SpringExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_CatalogServiceImplUnknownHostIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
public class CatalogServiceImplUnknownHostIT extends CatalogServiceBaseTest {

  private static final String BEAN_NAME_CATALOG_SERVICE = "catalogService";

  @Inject
  @Named(BEAN_NAME_CATALOG_SERVICE)
  private CatalogServiceImpl testling;

  @Inject
  private IbmTestConfig testConfig;

  @Inject
  private StoreInfoService storeInfoService;

  @BeforeEach
  @Override
  public void setup() {
    storeInfoService.getWcsVersion().ifPresent(testConfig::setWcsVersion);
    super.setup();
  }

  @Test
  void testFindProductByIdReturns502() {
    String endpoint = null;
    if (testConfig.getWcsVersion().lessThan(WCS_VERSION_9_0)) {
      endpoint = testling.getCatalogWrapperService().getCatalogConnector().getSearchServiceEndpoint(storeContext);
      testling.getCatalogWrapperService().getCatalogConnector().setSearchServiceEndpoint("http://unknownhost.unknowndomain/wcs/resources");
    } else {
      endpoint = testling.getCatalogWrapperService().getCatalogConnector().getSearchServiceSslEndpoint(storeContext);
      testling.getCatalogWrapperService().getCatalogConnector().setSearchServiceSslEndpoint("https://unknownhost.unknowndomain/wcs/resources");
    }

    Throwable exception = null;
    Product product = null;
    try {
      product = testling.findProductById(IbmCommerceIdProvider.commerceId(PRODUCT).withExternalId("UNCACHED_PRODUCT").build(), storeContext);
    } catch (Throwable e) {
      exception = e;
    } finally {
      if (testConfig.getWcsVersion().lessThan(WCS_VERSION_9_0)) {
        testling.getCatalogWrapperService().getCatalogConnector().setSearchServiceEndpoint(endpoint);
      } else {
        testling.getCatalogWrapperService().getCatalogConnector().setSearchServiceSslEndpoint(endpoint);
      }
    }
    assertNull(product);
    assertTrue("CommerceException expected", exception instanceof CommerceException);
  }
}
