package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.elastic.social.cae.controller.BlobRefImpl;
import com.coremedia.elastic.social.api.users.CommunityUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.webflow.execution.RequestContext;

import java.util.Date;
import java.util.Locale;
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

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsTest {
  private UserDetails userDetails;

  @Mock
  private RequestContext requestContext;

  @Mock
  private DefaultMessageContext messageContext;

  @Mock
  private PasswordPolicy passwordPolicy;

  @Before
  public void setup() {
    when(passwordPolicy.verify("4321")).thenReturn(true);
    userDetails = new UserDetails();
    userDetails.setUsername("frauke");
    userDetails.setGivenname("Frauke");
    userDetails.setSurname("Pantekoek");
    userDetails.setEmailAddress("frauke@coremedia.com");
    userDetails.setLocalizedLocale(new LocalizedLocale(Locale.ENGLISH, Locale.ENGLISH.getDisplayLanguage()));
    userDetails.setPassword("1234");
    userDetails.setNewPassword("4321");
    userDetails.setNewPasswordRepeat("4321");
    userDetails.setPasswordPolicy(passwordPolicy);
    userDetails.setTimeZoneId("UTC");

    when(requestContext.getMessageContext()).thenReturn(messageContext);
  }


  @Test
  public void testId() {
    String id = "1234";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getId());
    userDetails.setId(id);
    assertEquals(id, userDetails.getId());
  }

  @Test
  public void testEmailAddress() {
    String email = "email";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getEmailAddress());
    userDetails.setEmailAddress(email);
    assertEquals(email, userDetails.getEmailAddress());
  }

  @Test
  public void testLastLoginDate() {
    Date date = new Date();
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getLastLoginDate());
    userDetails.setLastLoginDate(date);
    assertEquals(date, userDetails.getLastLoginDate());
  }

  @Test
  public void testUsername() {
    String username = "username";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getUsername());
    userDetails.setUsername(username);
    assertEquals(username, userDetails.getUsername());
  }

  @Test
  public void testRegistrationDate() {
    Date date = new Date();
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getRegistrationDate());
    userDetails.setRegistrationDate(date);
    assertEquals(date, userDetails.getRegistrationDate());
  }

  @Test
  public void testNumberOfLogins() {
    long numberOfLogins = 5;
    UserDetails userDetails = new UserDetails();
    assertEquals(0, userDetails.getNumberOfLogins());
    userDetails.setNumberOfLogins(numberOfLogins);
    assertEquals(numberOfLogins, userDetails.getNumberOfLogins());
  }

  @Test
  public void testNumberOfComments() {
    long numberOfComments = 5;
    UserDetails userDetails = new UserDetails();
    assertEquals(0, userDetails.getNumberOfComments());
    userDetails.setNumberOfComments(numberOfComments);
    assertEquals(numberOfComments, userDetails.getNumberOfComments());
  }

  @Test
  public void testLocale() {
    LocalizedLocale localizedLocale = new LocalizedLocale(Locale.GERMAN, "Deutsch");
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getLocalizedLocale());
    userDetails.setLocalizedLocale(localizedLocale);
    assertEquals(localizedLocale, userDetails.getLocalizedLocale());
  }

  @Test
  public void testProfileImageId() {
    String profileImageId = "1234";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getProfileImage());
    userDetails.setProfileImage(new BlobRefImpl(profileImageId));
    assertEquals(profileImageId, userDetails.getProfileImage().getId());
  }

  @Test
  public void testGivenName() {
    String givenName = "givenname";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getGivenname());
    userDetails.setGivenname(givenName);
    assertEquals(givenName, userDetails.getGivenname());
  }

  @Test
  public void testSurName() {
    String surName = "surname";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getSurname());
    userDetails.setSurname(surName);
    assertEquals(surName, userDetails.getSurname());
  }

  @Test
  public void testPassword() {
    String password = "password";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getPassword());
    userDetails.setPassword(password);
    assertEquals(password, userDetails.getPassword());
  }

  @Test
  public void testNewPassword() {
    String newPassword = "newPassword";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getNewPassword());
    userDetails.setNewPassword(newPassword);
    assertEquals(newPassword, userDetails.getNewPassword());
  }

  @Test
  public void testNewPasswordRepeat() {
    String newPasswordRepeat = "newPasswordRepeat";
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getNewPasswordRepeat());
    userDetails.setNewPasswordRepeat(newPasswordRepeat);
    assertEquals(newPasswordRepeat, userDetails.getNewPasswordRepeat());
  }

  @Test
  public void testPasswordPolicy() {
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getPasswordPolicy());
    userDetails.setPasswordPolicy(passwordPolicy);
    assertSame(passwordPolicy, userDetails.getPasswordPolicy());
  }

  @Test
  public void testViewOwnProfile() {
    boolean viewOwnProfile = true;
    UserDetails userDetails = new UserDetails();
    assertFalse(userDetails.isViewOwnProfile());
    userDetails.setViewOwnProfile(viewOwnProfile);
    assertEquals(viewOwnProfile, userDetails.isViewOwnProfile());
  }

  @Test
  public void testDeleteProfileImage() {
    boolean deleteProfileImage = true;
    UserDetails userDetails = new UserDetails();
    assertFalse(userDetails.isDeleteProfileImage());
    userDetails.setDeleteProfileImage(deleteProfileImage);
    assertEquals(deleteProfileImage, userDetails.isDeleteProfileImage());
  }


  @Test
  public void testValidateSuccess() {
    UserDetails userDetails = new UserDetails();
    userDetails.setUsername("frauke");
    userDetails.setGivenname("Frauke");
    userDetails.setSurname("Pantekoek");
    userDetails.setEmailAddress("frauke@coremedia.com");
    userDetails.setTimeZoneId("UTC");
    userDetails.setLocalizedLocale(new LocalizedLocale(Locale.ENGLISH, Locale.ENGLISH.getLanguage()));

    userDetails.validate(requestContext);

    verify(messageContext, never()).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testValidateFailure() {
    UserDetails userDetails = new UserDetails();
    userDetails.setUsername("");
    userDetails.setGivenname("");
    userDetails.setSurname("");
    userDetails.setEmailAddress("");
    userDetails.setLocalizedLocale(null);

    userDetails.validate(requestContext);

    verify(messageContext, times(6)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testPasswordValidationSuccess() {
    userDetails.validate(requestContext);

    verify(messageContext, never()).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testPasswordUsernameTooShortAndEmailInvalid() {
    userDetails.setUsername("fp");
    userDetails.setEmailAddress("frauke@coremedia");

    userDetails.validate(requestContext);

    verify(messageContext, times(2)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testPasswordValidationFailureNewPasswordEmpty() {
    userDetails.setNewPassword("");
    userDetails.setNewPasswordRepeat("");

    userDetails.validate(requestContext);

    verify(messageContext, times(2)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testPasswordValidationFailureOldPasswordEmpty() {
    userDetails.setPassword("");

    userDetails.validate(requestContext);

    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void testPasswordValidationErrorPasswordsErroneous() {
    when(passwordPolicy.verify("1234")).thenReturn(false);
    userDetails.setNewPassword("1234");
    userDetails.setPasswordPolicy(passwordPolicy);

    userDetails.validate(requestContext);

    verify(messageContext, times(3)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void getLocale() {
    Locale locale = Locale.GERMAN;
    LocalizedLocale localizedLocale = new LocalizedLocale(locale, locale.getDisplayLanguage());
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getLocale());
    userDetails.setLocalizedLocale(localizedLocale);
    assertEquals(locale, userDetails.getLocale());
  }

  @Test
  public void isPreModerationChanged() {
    boolean preModerationChanged = true;
    UserDetails userDetails = new UserDetails();
    assertFalse(userDetails.isPreModerationChanged());
    userDetails.setPreModerationChanged(preModerationChanged);
    assertEquals(preModerationChanged, userDetails.isPreModerationChanged());
  }

  @Test
  public void isPreview() {
    boolean preview = true;
    UserDetails userDetails = new UserDetails();
    assertFalse(userDetails.isPreview());
    userDetails.setPreview(preview);
    assertEquals(preview, userDetails.isPreview());
  }

  @Test
  public void hasChangesWhichDoNotNeedModeration() {
    UserDetails userDetails = new UserDetails();
    CommunityUser communityUser = mock(CommunityUser.class);
    when(communityUser.getLocale()).thenReturn(Locale.ENGLISH);

    Locale locale = Locale.GERMAN;
    LocalizedLocale localizedLocale = new LocalizedLocale(locale, locale.getDisplayLanguage());
    userDetails.setLocalizedLocale(localizedLocale);

    assertTrue(userDetails.hasChangesWhichDoNotNeedModeration(communityUser));
  }

  @Test
  public void hasChangesWhichNeedModeration() {
    CommunityUser user = mock(CommunityUser.class);
    UserDetails userDetails = new UserDetails();
    assertFalse(userDetails.hasChangesWhichNeedModeration(user, null));
    userDetails.setUsername("userName");
    when(user.getName()).thenReturn("newName");
    assertEquals(true, userDetails.hasChangesWhichNeedModeration(user, null));
  }

  @Test
  public void receiveCommentRejectEmails() {
    UserDetails userDetails = new UserDetails();
    assertFalse(userDetails.isReceiveCommentReplyEmails());
    userDetails.setReceiveCommentReplyEmails(true);
    assertTrue(userDetails.isReceiveCommentReplyEmails());
  }

  @Test
  public void testTimeZoneId() {
    UserDetails userDetails = new UserDetails();
    assertNull(userDetails.getTimeZoneId());
    userDetails.setTimeZoneId(TimeZone.getTimeZone("UTC").getID());
    assertEquals(TimeZone.getTimeZone("UTC").getID(), userDetails.getTimeZoneId());
  }
}
