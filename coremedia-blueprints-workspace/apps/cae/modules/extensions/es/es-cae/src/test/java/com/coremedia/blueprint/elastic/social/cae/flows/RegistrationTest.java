package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.elastic.social.cae.controller.BlobRefImpl;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestContextHolder.class})
public class RegistrationTest {
  @Mock
  RequestContext requestContext;

  @Mock
  private ValidationContext validationContext;

  @Mock
  private ExternalContext externalContext;

  @Mock
  private HttpServletRequest request;

  @Mock
  private DefaultMessageContext messageContext;

  @Mock
  private LocalizationContext localizationContext;
  
  @Mock
  private Page page;


  @Before
  @SuppressWarnings("unchecked")
  public void setup() {
    mockStatic(RequestContextHolder.class);
    when(RequestContextHolder.getRequestContext()).thenReturn(requestContext);
    when(validationContext.getMessageContext()).thenReturn(messageContext);
    when(requestContext.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getNativeRequest()).thenReturn(request);
    when(request.getAttribute(javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT)).thenReturn(localizationContext);

    // make WebApplicationContextUtils happy
    ServletContext servletContext = mock(ServletContext.class);
    WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    ElasticSocialPlugin elasticSocialPlugin = mock(ElasticSocialPlugin.class);

    when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(webApplicationContext);
    when(elasticSocialPlugin.getElasticSocialConfiguration(any())).thenReturn(mock(ElasticSocialConfiguration.class));
    when(webApplicationContext.getBean(ElasticSocialPlugin.class)).thenReturn(elasticSocialPlugin);

    when(request.getServletContext()).thenReturn(servletContext);
    when(RequestAttributeConstants.getPage(request)).thenReturn(page);
  }

  @Test
  public void testUsername() {
    Registration registration = new Registration();
    assertNull(registration.getUsername());
    registration.setUsername("horst");
    assertEquals("horst", registration.getUsername());
  }

  @Test
  public void testGivenname() {
    Registration registration = new Registration();
    assertNull(registration.getGivenname());
    registration.setGivenname("horst");
    assertEquals("horst", registration.getGivenname());
  }

  @Test
  public void testSurname() {
    Registration registration = new Registration();
    assertNull(registration.getSurname());
    registration.setSurname("schneider");
    assertEquals("schneider", registration.getSurname());
  }

  @Test
  public void testPassword() {
    Registration registration = new Registration();
    assertNull(registration.getPassword());
    registration.setPassword("secret");
    assertEquals("secret", registration.getPassword());
  }

  @Test
  public void testConfirmPassword() {
    Registration registration = new Registration();
    assertNull(registration.getConfirmPassword());
    registration.setConfirmPassword("secret");
    assertEquals("secret", registration.getConfirmPassword());
  }

  @Test
  public void testEmailAddress() {
    Registration registration = new Registration();
    assertNull(registration.getEmailAddress());
    registration.setEmailAddress("horst@coremedia.com");
    assertEquals("horst@coremedia.com", registration.getEmailAddress());
  }

  @Test
  public void testPasswordPolicy() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    Registration registration = new Registration();
    assertNull(registration.getPasswordPolicy());
    registration.setPasswordPolicy(passwordPolicy);
    assertSame(passwordPolicy, registration.getPasswordPolicy());
  }

  @Test
  public void testValidateResetFormSuccess() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("secret")).thenReturn(true);
    Registration registration = new Registration();
    registration.setUsername("horst");
    registration.setGivenname("horstl");
    registration.setSurname("Schneider");
    registration.setEmailAddress("horst@coremedia.com");
    registration.setPassword("secret");
    registration.setConfirmPassword("secret");
    registration.setPasswordPolicy(passwordPolicy);
    registration.setAcceptTermsOfUse(true);

    registration.validateRegistration(validationContext);

    verify(messageContext, never()).addMessage(any(MessageResolver.class));
    verify(passwordPolicy).verify("secret");
  }

  @Test
  public void testValidateResetFormFailure() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("")).thenReturn(false);
    Registration registration = new Registration();
    registration.setUsername("");
    registration.setGivenname("");
    registration.setSurname("");
    registration.setEmailAddress("");
    registration.setPassword("");
    registration.setConfirmPassword(" ");
    registration.setPasswordPolicy(passwordPolicy);
    registration.setAcceptTermsOfUse(false);

    registration.validateRegistration(validationContext);

    verify(messageContext, times(7)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidateResetFormUsernameTooShortAndEmailInvalid() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("secret")).thenReturn(true);
    Registration registration = new Registration();
    registration.setUsername("ho");
    registration.setGivenname("horstl");
    registration.setSurname("Schneider");
    registration.setEmailAddress("horst@coremedia");
    registration.setPassword("secret");
    registration.setConfirmPassword("secret");
    registration.setPasswordPolicy(passwordPolicy);
    registration.setAcceptTermsOfUse(true);

    registration.validateRegistration(validationContext);

    verify(messageContext, times(2)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidateResetFormPasswordsNotEqual() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("secret")).thenReturn(true);
    Registration registration = new Registration();
    registration.setUsername("horst");
    registration.setGivenname("horstl");
    registration.setSurname("Schneider");
    registration.setEmailAddress("horst@coremedia.com");
    registration.setPassword("secret");
    registration.setConfirmPassword("notsecret");
    registration.setPasswordPolicy(passwordPolicy);
    registration.setAcceptTermsOfUse(true);

    registration.validateRegistration(validationContext);

    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
    verify(passwordPolicy).verify("secret");
  }

  @Test
  public void testValidateResetFormPasswordsInvalid() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    when(passwordPolicy.verify("secret")).thenReturn(false);
    Registration registration = new Registration();
    registration.setUsername("horst");
    registration.setGivenname("horstl");
    registration.setSurname("Schneider");
    registration.setEmailAddress("horst@coremedia.com");
    registration.setPassword("secret");
    registration.setConfirmPassword("secret");
    registration.setPasswordPolicy(passwordPolicy);
    registration.setAcceptTermsOfUse(true);

    registration.validateRegistration(validationContext);

    assertTrue(registration.isAcceptTermsOfUse());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
    verify(passwordPolicy).verify("secret");
  }

  @Test
  public void testProfileImageId() {
    Registration registration = new Registration();
    assertNull(registration.getProfileImage());
    registration.setProfileImage(new BlobRefImpl("23423424"));
    assertEquals("23423424", registration.getProfileImage().getId());
  }

  @Test
  public void testDeleteProfileImage() {
    Registration registration = new Registration();
    assertFalse(registration.isDeleteProfileImage());
    registration.setDeleteProfileImage(true);
    assertTrue(registration.isDeleteProfileImage());
  }

  @Test
  public void testRegisterWithProvider() {
    Registration registration = new Registration();
    assertFalse(registration.isRegisteringWithProvider());
    registration.setRegisteringWithProvider(true);
    assertTrue(registration.isRegisteringWithProvider());
  }

  @Test
  public void testTimeZoneId() {
    Registration registration = new Registration();
    assertNull(registration.getTimeZoneId());
    registration.setTimeZoneId(TimeZone.getTimeZone("UTC").getID());
    assertEquals(TimeZone.getTimeZone("UTC").getID(), registration.getTimeZoneId());
  }
}
