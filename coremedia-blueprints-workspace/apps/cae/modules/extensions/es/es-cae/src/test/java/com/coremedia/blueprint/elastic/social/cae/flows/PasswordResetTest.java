package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.webflow.execution.RequestContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetTest {

  @Mock
  private RequestContext requestContext;

  @Mock
  private DefaultMessageContext messageContext;

  @Before
  public void setup() {
    when(requestContext.getMessageContext()).thenReturn(messageContext);
  }
  
  @Test
  public void testEmailAddress() {
    PasswordReset passwordReset = new PasswordReset();
    assertNull(passwordReset.getEmailAddress());
    passwordReset.setEmailAddress("horst@coremedia.com");
    assertEquals("horst@coremedia.com", passwordReset.getEmailAddress());
  }

  @Test
  public void testPassword() {
    PasswordReset passwordReset = new PasswordReset();
    assertNull(passwordReset.getPassword());
    passwordReset.setPassword("secret");
    assertEquals("secret", passwordReset.getPassword());
  }

  @Test
  public void testConfirmPassword() {
    PasswordReset passwordReset = new PasswordReset();
    assertNull(passwordReset.getConfirmPassword());
    passwordReset.setConfirmPassword("secret");
    assertEquals("secret", passwordReset.getConfirmPassword());
  }

  @Test
  public void testPasswordPolicy() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    PasswordReset passwordReset = new PasswordReset();
    assertNull(passwordReset.getPasswordPolicy());
    passwordReset.setPasswordPolicy(passwordPolicy);
    assertSame(passwordPolicy, passwordReset.getPasswordPolicy());
  }

  @Test
  public void testValidatePasswordResetSuccess() {
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setEmailAddress("horst@coremedia.com");

    passwordReset.validate(requestContext);

    verify(messageContext, never()).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidatePasswordResetFailure() {
    PasswordReset passwordReset = new PasswordReset();

    passwordReset.validate(requestContext);

    verify(messageContext).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidateResetFormSuccess() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("secret")).thenReturn(true);
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setPassword("secret");
    passwordReset.setConfirmPassword("secret");
    passwordReset.setPasswordPolicy(passwordPolicy);

    passwordReset.validateResetForm(requestContext);

    verify(messageContext, never()).addMessage(any(MessageResolver.class));
    verify(passwordPolicy).verify("secret");
  }

  @Test
  public void testValidateResetFormFailureEmptyPassword() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setPasswordPolicy(passwordPolicy);
    passwordReset.setPassword("");
    passwordReset.setConfirmPassword("");

    passwordReset.validateResetForm(requestContext);

    verify(messageContext).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidateResetFormFailureWeakAndDifferentPasswords() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("yyy")).thenReturn(false);
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setPasswordPolicy(passwordPolicy);
    passwordReset.setPassword("yyy");
    passwordReset.setConfirmPassword("xxx");

    passwordReset.validateResetForm(requestContext);

    verify(messageContext, times(2)).addMessage(any(MessageResolver.class));
  }
}
