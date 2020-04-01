package com.coremedia.blueprint.elastic.social.cae.user;

import com.coremedia.elastic.social.api.users.CommunityUser;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class UserContextTest {
  @Test
  public void testSetAndClear() {
    CommunityUser user = mock(CommunityUser.class);
    UserContext.setUser(user);

    assertEquals(user, UserContext.getUser());

    UserContext.clear();

    assertEquals(null, UserContext.getUser());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    UserContext.setUser(null);
  }

  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<?>[] constructors = UserContext.class.getDeclaredConstructors();
    constructors[0].setAccessible(true);
    constructors[0].newInstance((Object[]) null);
  }
}
