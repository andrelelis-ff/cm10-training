package com.coremedia.livecontext.ecommerce.ibm.login;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.common.InvalidLoginException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_LoginServiceImplIT.json"
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
public class LoginServiceImplIT extends IbmServiceTestBase {

  @Inject
  private LoginServiceImpl testling;

  private String origServiceUser;
  private String origServicePassword;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();
    origServiceUser = testling.getServiceUser();
    origServicePassword = testling.getServicePassword(storeContext);
  }

  @AfterEach
  void tearDown() throws Exception {
    testling.destroy();
    testling.setServiceUser(origServiceUser);
    testling.setServicePassword(origServicePassword);
  }

  @Test
  void testLoginSuccess() {
    WcCredentials credentials = testling.loginServiceIdentity(storeContext);
    assertThat(credentials).isNotNull();
    assertThat(credentials.getSession()).isNotNull();
  }

  /**
   * Attention: this test does not work with hoverfly tapes. It only works against the wcs system.
   * The test independently detects a
   * "hoverfly.ignoreTapes=true" configuration and runs only if such a java property is found.
   * <p>
   * The restriction is, the wcs system has to support workspaces permanently.
   */
  @Test
  void testLoginSuccessWithWorkspaces() {
    if (useTapes()) {
      return;
    }

    StoreContext storeContext = testConfig.getStoreContextWithWorkspace(connection);
    WcCredentials credentials = testling.loginServiceIdentity(storeContext);
    assertThat(credentials).isNotNull();
    assertThat(credentials.getSession()).isNotNull();
  }

  @Test
  void testLoginFailure() {
    testling.setServicePassword("wrong password");
    assertThrows(InvalidLoginException.class, () -> {
      testling.loginServiceIdentity(storeContext);
    });
  }

  @Test
  void testLogout() {
    WcCredentials credentials = testling.loginServiceIdentity(storeContext);
    assertThat(credentials).isNotNull();

    boolean success = testling.logoutServiceIdentity(storeContext);
    assertThat(success).isTrue();
  }
}
