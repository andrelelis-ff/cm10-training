package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.cae.WcsUrlProvider;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for Search REST interface.
 */
@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_B2BCatalogServiceSearchBasedIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = "shop-ref.ecommerce.coremedia.com"
        )
)
@ContextConfiguration(classes = {
        IbmServiceTestBase.LocalConfig.class,
        StoreFrontConfiguration.class
})
public class B2BCatalogServiceSearchBasedIT extends AbstractB2BCatalogServiceIT {

  @SuppressWarnings("unused")
  @MockBean
  private WcsUrlProvider wcsUrlProvider;

  @Test
  @Override
  public void testFindProductVariantByExternalIdWithContractSupport() throws Exception {
    super.testFindProductVariantByExternalIdWithContractSupport();
  }

}
