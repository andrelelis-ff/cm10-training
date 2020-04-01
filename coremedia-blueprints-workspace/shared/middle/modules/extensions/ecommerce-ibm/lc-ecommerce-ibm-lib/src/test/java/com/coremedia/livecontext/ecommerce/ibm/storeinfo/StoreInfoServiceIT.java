package com.coremedia.livecontext.ecommerce.ibm.storeinfo;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService}
 */
@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_StoreInfoServiceIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = "shop-ref.ecommerce.coremedia.com",
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class StoreInfoServiceIT extends IbmServiceTestBase {
  @Inject
  private StoreInfoService testling;

  private String storeName;


  @BeforeEach
  public void setup() {
    super.setup();

    storeName = testConfig.getStoreContext(connection).getStoreName();
  }

  @Test
  void testGetStoreId() {
    Optional<String> storeId = testling.getStoreId(storeName);

    assertThat(storeId).contains(storeContext.getStoreId());
  }

  @Test
  void testDefaultCatalogId() {
    Optional<CatalogId> catalogId = testling.getDefaultCatalogId(storeName);

    assertThat(catalogId).isPresent();
  }

  @Test
  void testCatalogId() {
    Optional<CatalogId> catalogId = testling.getCatalogId(storeName, testConfig.getCatalogName());

    assertThat(catalogId).isPresent();
  }
}
