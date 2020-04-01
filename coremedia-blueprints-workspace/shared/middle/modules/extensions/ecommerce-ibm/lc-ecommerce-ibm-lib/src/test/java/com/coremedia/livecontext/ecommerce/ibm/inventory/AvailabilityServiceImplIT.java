package com.coremedia.livecontext.ecommerce.ibm.inventory;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.inventory.AvailabilityInfo;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.coremedia.livecontext.ecommerce.ibm.inventory.AvailabilityServiceImpl}
 */
@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_AvailabilityServiceImplIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class AvailabilityServiceImplIT extends IbmServiceTestBase {

  private static final String PRODUCT2_CODE = "AuroraWMDRS-1";

  @Inject
  private  AvailabilityServiceImpl testling;

  @Inject
  private  CatalogService catalogService;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @Test
  void testGetAvailabilityInfo() {
    CommerceId productId = connection.getIdProvider().formatProductId(null, PRODUCT2_CODE);
    List<ProductVariant> variants = catalogService.findProductById(productId, storeContext).getVariants();
    ProductVariant productVariant = variants.get(0);
    CommerceId commerceId = ibmCommerceIdProvider.formatProductVariantTechId(storeContext.getCatalogAlias(), productVariant.getExternalTechId());
    ProductVariant productVariant2 = (ProductVariant) testling.getCommerceBeanFactory().createBeanFor(commerceId, storeContext);
    assertEquals(productVariant, productVariant2);

    //test multiple variants
    Map<ProductVariant, AvailabilityInfo> inventoryAvailabilityMap = testling.getAvailabilityInfo(variants);
    assertTrue(inventoryAvailabilityMap.size() > 1);
    AvailabilityInfo availabilityInfo = inventoryAvailabilityMap.get(productVariant2);
    assertNotNull(availabilityInfo);

    //test single AvailabilityInfo
    assertEquals(AvailabilityInfo.STATUS_AVAILABLE, availabilityInfo.getInventoryStatus());
    assertTrue(availabilityInfo.getQuantity() >= 1.0F);

    //test identity of Availability
    assertEquals(availabilityInfo, testling.getAvailabilityInfo(productVariant2));
    assertEquals(availabilityInfo, productVariant2.getAvailabilityInfo());
  }
}
