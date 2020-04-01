package com.coremedia.livecontext.ecommerce.ibm.contract;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.Vendor;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collection;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static com.coremedia.livecontext.ecommerce.ibm.contract.ContractServiceImpl.toContractId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_ContractServiceImplIT.json"
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
public class ContractServiceImplIT extends IbmServiceTestBase {

  @Inject
  private ContractServiceImpl testling;

  private StoreContextImpl storeContext;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();

    storeContext = testConfig.getB2BStoreContext(connection);
  }

  @Test
  void testFindContractIdsForUser() {
    UserContext userContext = UserContext.builder().withUserName(testConfig.getUser2Name()).build();
    CurrentUserContext.set(userContext);

    Collection<Contract> contractIdsForUser = testling.findContractIdsForUser(CurrentUserContext.get(), storeContext);
    assertNotNull(contractIdsForUser);

    if (WCS_VERSION_7_7.lessThan(StoreContextHelper.getWcsVersion(storeContext))) {
      assertFalse("number of eligible contracts should be more than zero", contractIdsForUser.isEmpty());
    } else {
      assertTrue("number of eligible contracts should be zero", contractIdsForUser.isEmpty());
    }
  }

  @Test
  void testFindContractIdsForPreviewUser() {
    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }

    UserContext userContext = UserContext.builder().withUserName(testConfig.getPreviewUserName()).build();
    CurrentUserContext.set(userContext);

    Collection<Contract> contracts = testling.findContractIdsForUser(CurrentUserContext.get(), storeContext);
    assertNotNull(contracts);

    if (WCS_VERSION_7_7.lessThan(StoreContextHelper.getWcsVersion(storeContext))) {
      assertEquals(3, contracts.size());

      for (Contract contract : contracts) {
        CommerceId contractId = contract.getId();
        assertEquals("contract id has wrong format: " + contract.getId(), Vendor.of("ibm"), contractId.getVendor());
        assertEquals("contract id has wrong format: " + contract.getId(), "contract", contractId.getCommerceBeanType().type());
        assertTrue("contract id has wrong format: " + contract.getId(), contractId.getExternalId().map(e -> e.startsWith("4000")).orElse(false));
      }
    }
  }

  @Test
  void testFindContractIdsForServiceUserWithNoServiceUser() {
    String contractPreviewServiceUserName = testling.getContractPreviewServiceUserName();
    try {
      testling.setContractPreviewServiceUserName(null);
      Collection<Contract> contracts = testling.findContractIdsForServiceUser(storeContext);
      assertNotNull(contracts);
      assertTrue(contracts.isEmpty());
    } finally {
      testling.setContractPreviewServiceUserName(contractPreviewServiceUserName);
    }
  }

  @Test
  void testFindContractById() {
    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      return;
    }

    StoreContextImpl storeContext = testConfig.getB2BStoreContext(connection);
    CurrentStoreContext.set(storeContext);

    UserContext userContext = UserContext.builder().withUserName(testConfig.getPreviewUserName()).build();
    CurrentUserContext.set(userContext);

    Collection<Contract> contracts = testling.findContractIdsForUser(CurrentUserContext.get(), storeContext);
    assertNotNull(contracts);

    if (WCS_VERSION_7_7.lessThan(StoreContextHelper.getWcsVersion(storeContext))) {
      for (Contract contract : contracts) {
        CommerceId contractId = toContractId(contract.getExternalId());
        Contract contractById = testling.findContractById(contractId, storeContext);
        assertNotNull(contractById);
        assertEquals(contract.getExternalId(), contractById.getExternalId());
      }
    }
  }

  @Test
  void testInvalidContract() {
    CommerceId contractId = toContractId("xxxx");
    Contract testcontract = testling.findContractById(contractId, storeContext);
    assertNull(testcontract);
  }
}
