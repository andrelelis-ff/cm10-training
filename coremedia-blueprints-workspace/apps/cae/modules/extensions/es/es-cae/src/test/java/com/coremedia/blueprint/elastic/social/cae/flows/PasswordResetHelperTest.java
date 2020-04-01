package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.elastic.social.api.mail.MailTemplateNotFoundException;
import com.coremedia.elastic.social.api.registration.RegistrationService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;

import java.util.Locale;

import static com.coremedia.elastic.core.test.Injection.inject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetHelperTest {
  private static final String TOKEN = "1234321";
  private static final String PASSWORD = "secret";
  private PasswordResetHelper helper;

  @Mock
  private RegistrationService registrationService;

  @Mock
  private RequestContext requestContext;

  @Mock
  private DefaultMessageContext messageContext;

  @Mock
  private ExternalContext externalContext;

  @Mock
  private CommunityUser communityUser;

  @Before
  public void init() {
    when(requestContext.getMessageContext()).thenReturn(messageContext);
    when(requestContext.getExternalContext()).thenReturn(externalContext);
    CommunityUser user = mock(CommunityUser.class);
    when(registrationService.getUserByToken(TOKEN)).thenReturn(user);
    helper = new PasswordResetHelper();
    inject(helper, registrationService);
  }

  @Test
  public void resetPasswordSuccess() {
    when(registrationService.resetPassword("horst@coremedia.com")).thenReturn(true);
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setEmailAddress("horst@coremedia.com");

    boolean isPasswordReset = helper.reset(passwordReset, requestContext);
    assertTrue(isPasswordReset);
    verify(registrationService).resetPassword("horst@coremedia.com");
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void resetPasswordFails() {
    when(registrationService.resetPassword("horst@coremedia.com")).thenThrow(new MailTemplateNotFoundException("", Locale.ENGLISH));
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setEmailAddress("horst@coremedia.com");

    boolean isPasswordReset = helper.reset(passwordReset, requestContext);
    assertFalse(isPasswordReset);
    verify(registrationService).resetPassword("horst@coremedia.com");
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidateToken() {
    boolean isValid = helper.validateToken(TOKEN);
    assertTrue(isValid);
  }

  @Test
  public void testInvalidToken() {
    when(registrationService.getUserByToken(TOKEN)).thenReturn(null);
    boolean isValid = helper.validateToken(TOKEN);
    assertFalse(isValid);
  }

  @Test
  public void testConfirm() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setPassword(PASSWORD);
    passwordReset.setConfirmPassword(PASSWORD);
    passwordReset.setPasswordPolicy(passwordPolicy);

    helper.confirm(TOKEN, passwordReset, requestContext);
  }

  @Test
  public void redirectLoggedInUserToHomePage() {
    UserContext.setUser(communityUser);
    helper.redirectLoggedInUserToHomePage(requestContext);

    verify(externalContext).requestExternalRedirect("contextRelative:");
  }

  @Test
  public void redirectLoggedInUserToHomePageNoUser() {
    UserContext.clear();
    helper.redirectLoggedInUserToHomePage(requestContext);

    verify(externalContext, never()).requestExternalRedirect("contextRelative:");
  }

}