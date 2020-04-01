package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserContextHelperTest {

  @Test
  void testNullContext() {
    assertThat(UserContextHelper.getForUserName(null)).isNull();
  }

  @Test
  void testInvalidValues() {
    UserContext userContext = mock(UserContext.class);
    when(userContext.getUserName()).thenReturn(null);

    assertThat(UserContextHelper.getForUserName(userContext)).isNull();
  }

  @Test
  void testCreateContext() {
    UserContext userContext = UserContext.builder().withUserName("forUser").build();
    assertThat(UserContextHelper.getForUserName(userContext)).isEqualTo("forUser");
  }

  @Test
  void testCurrentContext() {
    UserContext userContext = UserContext.builder().withUserName("forUser").build();
    CurrentUserContext.set(userContext);
    assertThat(CurrentUserContext.get()).isEqualTo(userContext);

    UserContext userContext2 = UserContext.builder().withUserName("forUser2").build();
    CurrentUserContext.set(userContext2);
    assertThat(CurrentUserContext.get()).isEqualTo(userContext2);

    CurrentUserContext.remove();
  }
}
