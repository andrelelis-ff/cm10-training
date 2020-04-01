package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LoginFormTest {
  @Test
  public void testName() {
    LoginForm loginForm = new LoginForm();
    assertNull(loginForm.getName());
    loginForm.setName("test");
    assertEquals("test", loginForm.getName());
  }

  @Test
  public void testPassword() {
    LoginForm loginForm = new LoginForm();
    assertNull(loginForm.getPassword());
    loginForm.setPassword("test");
    assertEquals("test", loginForm.getPassword());
  }
}
