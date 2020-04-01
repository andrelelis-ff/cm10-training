package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_UserServiceImplIT.json"
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
class UserServiceImplIT extends IbmServiceTestBase {

  @Inject
  private UserServiceImpl testling;

  @Test
  void testFindPerson() {
    UserContext userContext = UserContext.builder().build();
    CurrentUserContext.set(userContext);
    User user = testling.findCurrentUser();
    assertThat(user).isNotNull();
    assertThat(user.getLogonId()).isNotNull();

    user.getChallengeAnswer();
    user.getChallengeQuestion();
    assertThat(user.getCity()).isEqualTo("Hamburg");
    assertThat(user.getCountry()).isEqualTo("D");
    assertThat(user.getEmail1()).isNotNull();
    assertThat(user.getEmail2()).isNullOrEmpty();
    assertThat(user.getEmail3()).isNullOrEmpty();
    assertThat(user.getFirstName()).isEqualTo("Cm");
    assertThat(user.getLastName()).isEqualTo("Admin");
    assertThat(user.getLogonId()).isEqualTo("cmadmin");
    assertThat(user.getLogonPassword()).isNullOrEmpty();
    assertThat(user.getLogonPasswordVerify()).isNullOrEmpty();
    assertThat(user.getUserId()).isEqualTo("2");
  }
}
