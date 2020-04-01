package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.blobs.BlobException;
import com.coremedia.elastic.core.api.blobs.BlobService;
import com.coremedia.elastic.core.api.staging.StagingService;
import com.coremedia.elastic.core.api.users.DuplicateEmailException;
import com.coremedia.elastic.core.api.users.DuplicateNameException;
import com.coremedia.elastic.social.api.ModerationType;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.mail.MailTemplateService;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.coremedia.elastic.core.test.Injection.inject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsHelperTest {
  private String id = "1234";
  private String userName = "username";
  private String givenName = "givenName";
  private String surName = "surName";
  private String emailAddress = "email@address.de";
  private Locale locale = Locale.ENGLISH;
  private String password = "123456";
  private String newPassword = "654321";
  private String contentType = "image/jpeg";
  private Map<String, Object> params = new HashMap<>();
  private UserDetails details = new UserDetails();

  @InjectMocks
  private UserDetailsHelper userDetailsHelper;

  @Mock
  private CommunityUser communityUser;

  @Mock
  private RequestContext requestContext;

  @Mock
  private ExternalContext externalContext;

  @Mock
  private CommentService commentService;

  @Mock
  private RatingService ratingService;

  @Mock
  private ReviewService reviewService;

  @Mock
  private LikeService likeService;

  @Mock
  private CommunityUserService communityUserService;

  @Mock
  private MailTemplateService mailTemplateService;

  @Mock
  private DefaultMessageContext messageContext;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private Blob blob;

  @Mock
  private BlobService blobService;

  @Mock
  private InputStream inputStream;

  @Mock
  private CommonsMultipartFile file;

  @Mock
  private CommunityUser loggedInUser;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private Page page;

  @Mock
  private SharedAttributeMap sessionMap;

  @Mock
  private PasswordPolicy passwordPolicy;

  @Mock
  private StagingService stagingService;

  @Mock
  private FlowUrlHelper flowUrlHelper;

  @Mock
  private ElasticSocialConfiguration elasticSocialConfiguration;

  @Mock
  private ElasticSocialPlugin elasticSocialPlugin;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws IOException {
    Map<String, Object> properties = getProperties();
    when(communityUser.getEmail()).thenReturn(emailAddress);
    when(communityUser.getProperties()).thenReturn(properties);
    when(communityUser.getId()).thenReturn(id);
    UserContext.setUser(communityUser);

    when(elasticSocialPlugin.getElasticSocialConfiguration(any())).thenReturn(elasticSocialConfiguration);
    when(elasticSocialConfiguration.getMaxImageFileSize()).thenReturn(512000);

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(requestContext.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getLocale()).thenReturn(Locale.ENGLISH);
    when(externalContext.getNativeRequest()).thenReturn(httpServletRequest);
    when(requestContext.getMessageContext()).thenReturn(messageContext);

    when(commentService.getNumberOfApprovedComments(communityUser)).thenReturn(10L);
    when(ratingService.getNumberOfRatingsFromUser(communityUser)).thenReturn(10L);
    when(likeService.getNumberOfLikesFromUser(communityUser)).thenReturn(10L);
    when(reviewService.getNumberOfApprovedReviews(communityUser)).thenReturn(10L);
    when(communityUserService.getUserByName(userName)).thenReturn(communityUser);
    when(communityUserService.createFrom(communityUser)).thenReturn(communityUser);
    when(communityUserService.getPasswordHashAlgorithm()).thenReturn("algorithm");

    when(communityUser.getLocale()).thenReturn(Locale.ENGLISH);
    when(communityUser.isReceiveCommentReplyEmails()).thenReturn(true);
    when(communityUser.getProperties()).thenReturn(getProperties());
    when(communityUser.getTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));

    when(blobService.put(inputStream, contentType, "filename")).thenReturn(blob);

    when(file.getInputStream()).thenReturn(inputStream);
    when(file.getContentType()).thenReturn(contentType);

    when(this.httpServletRequest.getAttribute("cmpage")).thenReturn(page);

    when(requestContext.getExternalContext().getNativeRequest()).thenReturn(this.httpServletRequest);

    ModerationType moderationType = ModerationType.NONE;
    when(elasticSocialConfiguration.getUserModerationType()).thenReturn(moderationType);

    params.put("name", communityUser.getName());
    details = getUserDetails();

    userDetailsHelper = new UserDetailsHelper();
    inject(userDetailsHelper, commentService);
    inject(userDetailsHelper, ratingService);
    inject(userDetailsHelper, likeService);
    inject(userDetailsHelper, communityUserService);
    inject(userDetailsHelper, contentRepository);
    inject(userDetailsHelper, mailTemplateService);
    inject(userDetailsHelper, stagingService);
    inject(userDetailsHelper, blobService);
    inject(userDetailsHelper, mailTemplateService);
    inject(userDetailsHelper, flowUrlHelper);
    inject(userDetailsHelper, elasticSocialPlugin);
    inject(userDetailsHelper, reviewService);
  }

  @Test
  public void testUserDetailsForLoggedInUser() {
    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy);
    assertNotNull(details);

    verify(communityUserService, never()).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));    
    verify(communityUser).isReceiveCommentReplyEmails();
    verify(communityUser).getGivenName();
    verify(communityUser).getSurName();
    verify(communityUser).getImage();
    verify(communityUser, atLeast(1)).getLocale();
    verify(communityUser).getLastLoginDate();
    verify(communityUser).getRegistrationDate();
    verify(communityUser).getEmail();
    verify(communityUser, atLeast(1)).getName();
    verify(communityUser).getId();
  }

  @Test
  public void testUserDetailsForPreview() {
    UserContext.clear();
    userDetailsHelper.setPreview(true);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));    verify(communityUser).isReceiveCommentReplyEmails();
    verify(communityUser).getGivenName();
    verify(communityUser).getSurName();
    verify(communityUser).getImage();
    verify(communityUser, atLeast(1)).getLocale();
    verify(communityUser).getLastLoginDate();
    verify(communityUser).getRegistrationDate();
    verify(communityUser).getEmail();
    verify(communityUser, atLeast(1)).getName();
    verify(communityUser).getId();
    verify(communityUser).hasChangesForPreModeration();
    verify(stagingService).applyChanges(communityUser);
  }

  @Test
  public void testUserDetailsForUserFromContextNotLoggedIn() {
    when(communityUser.isActivated()).thenReturn(true);
    UserContext.clear();

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));    verify(communityUser).isReceiveCommentReplyEmails();
    verify(communityUser, never()).getGivenName();
    verify(communityUser, never()).getSurName();
    verify(communityUser).getImage();
    verify(communityUser, atLeast(1)).getLocale();
    verify(communityUser).getLastLoginDate();
    verify(communityUser).getRegistrationDate();
    verify(communityUser, never()).getEmail();
    verify(communityUser, atLeast(1)).getName();
    verify(communityUser).getId();
  }

  @Test
  public void getIgnoredUserOnProduction() {
    userDetailsHelper.setPreview(true);
    UserContext.clear();

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));  }

  @Test
  public void getBlockedUserOnProduction() {
    userDetailsHelper.setPreview(true);
    UserContext.clear();

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));  }

  @Test
  public void getUnactivatedUserOnProduction() {
    userDetailsHelper.setPreview(true);
    UserContext.clear();

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));  }

  @Test
  public void testUserDetailsForUserFromContextLoggedIn() {
    when(communityUser.isActivated()).thenReturn(true);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));  }

  @Test
  public void testUserDetailsForUserFromContextOtherLoggedIn() {
    when(communityUser.isActivated()).thenReturn(true);
    UserContext.setUser(loggedInUser);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));  }


  @Test
  public void testUserDetailsForNonExistentUserFromContextNotLoggedIn() {
    UserContext.clear();
    when(communityUserService.getUserByName(userName)).thenReturn(null);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(never());
  }

  @Test
  public void testUserDetailsForEmptyUserNameParam() {
    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, "");
    assertNull(details);

    verify(communityUserService, never()).getUserByName(userName);
    verifyStatsServicesBeingCalled(never());

  }


  @Test
  public void testUserDetailsForInUserFromContextNotActivated() {
    when(communityUser.isActivated()).thenReturn(false);
    UserContext.setUser(communityUser);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(never());
  }

  @Test
  public void testUserDetailsForIgnoredUserLoggedIn() {
    when(communityUser.isActivated()).thenReturn(false);
    when(communityUser.isIgnored()).thenReturn(true);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNotNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));
  }

  @Test
  public void testUserDetailsForIgnoredUserOtherLoggedIn() {
    when(communityUser.isActivated()).thenReturn(false);
    when(communityUser.isIgnored()).thenReturn(true);
    UserContext.setUser(loggedInUser);

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(never());
  }

  @Test
  public void testUserDetailsForIgnoredUserNotLoggedIn() {
    when(communityUser.isActivated()).thenReturn(false);
    when(communityUser.isIgnored()).thenReturn(true);
    UserContext.clear();

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy, userName);
    assertNull(details);

    verify(communityUserService).getUserByName(userName);
    verifyStatsServicesBeingCalled(never());
  }

  @Test
  public void testUserDetailsForLoggedInUserNull() {
    UserContext.clear();

    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy);
    assertNull(details);

    verify(communityUserService, never()).getUserByName(userName);
    verifyStatsServicesBeingCalled(never());

  }

  @Test
  public void testUserDetailsWithPreModerationChanges() {
    when(communityUser.hasChangesForPreModeration()).thenReturn(true);
    UserDetails details = userDetailsHelper.getUserDetails(requestContext, passwordPolicy);
    assertNotNull(details);

    verify(communityUserService, never()).getUserByName(userName);
    verifyStatsServicesBeingCalled(times(1));    verify(communityUser).applyChangesFromPreModeration();
  }

  private void verifyStatsServicesBeingCalled(VerificationMode mode) {
    verify(communityUserService,mode).getNumberOfLogins(communityUser);
    verify(commentService,mode).getNumberOfApprovedComments(communityUser);
    verify(ratingService,mode).getNumberOfRatingsFromUser(communityUser);
    verify(likeService,mode).getNumberOfLikesFromUser(communityUser);
    verify(reviewService,mode).getNumberOfApprovedReviews(communityUser);
  }

  @Test
  public void deleteUser() {
    userDetailsHelper.deleteUser();

    verify(communityUserService).anonymize(communityUser);
  }

  @Test
  public void deleteUserNotLoggedIn() {
    UserContext.clear();

    userDetailsHelper.deleteUser();

    verify(communityUserService, never()).anonymize(communityUser);
  }

  @Test
  public void saveUser() {
    when(messageContext.hasErrorMessages()).thenReturn(false);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertTrue(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(communityUser).setReceiveCommentReplyEmails(details.isReceiveCommentReplyEmails());
    verify(communityUser).setGivenName(details.getGivenname());
    verify(communityUser).setSurName(details.getSurname());
    verify(communityUser).setLocale(details.getLocale());
    verify(communityUser).setEmail(details.getEmailAddress());
    verify(communityUser).setName(details.getUsername());
  }

  @Test
  public void saveUserNoChanges() {
    when(communityUser.getEmail()).thenReturn(details.getEmailAddress());
    when(communityUser.getName()).thenReturn(details.getUsername());
    when(communityUser.getProperty("givenName", String.class)).thenReturn(details.getGivenname());
    when(communityUser.getProperty("surName", String.class)).thenReturn(details.getSurname());
    when(communityUser.getLocale()).thenReturn(details.getLocale());
    when(communityUser.isReceiveCommentReplyEmails()).thenReturn(details.isReceiveCommentReplyEmails());
    when(communityUser.getTimeZone()).thenReturn(null);
    when(messageContext.hasErrorMessages()).thenReturn(false);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertTrue(userSaved);

    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
  }

  @Test
  public void saveUserDuplicateEmail() {
    doThrow(new DuplicateEmailException("horst@coremedia.com", new Throwable())).when(communityUserService).storeChanges(communityUser, ModerationType.NONE);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertFalse(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, params);
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserDuplicateUserName() {
    doThrow(new DuplicateNameException("horst", new Throwable())).when(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    MailTemplateService mailTemplateService = mock(MailTemplateService.class);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertFalse(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, params);
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserNoUserLoggedIn() {
    UserContext.clear();

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertFalse(userSaved);

    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, params);
  }

  @Test
  public void saveUserWithPassword() {
    when(communityUser.validatePassword(password)).thenReturn(true);
    when(communityUser.getLocale()).thenReturn(Locale.GERMAN);
    when(messageContext.hasErrorMessages()).thenReturn(false);

    UserDetails details = getUserDetailsWithPassword();
    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertTrue(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    verify(communityUser).validatePassword(password);
    verify(communityUser).setPassword(eq(newPassword), anyString());
    verify(mailTemplateService).sendMail("profileChanged", Locale.GERMAN, emailAddress, getProperties());
  }

  @Test
  public void saveUserWithPasswordInvalid() {
    when(communityUser.validatePassword(password)).thenReturn(false);
    when(messageContext.hasErrorMessages()).thenReturn(false).thenReturn(true);
    when(passwordPolicy.verify(newPassword)).thenReturn(true);

    UserDetails details = getUserDetailsWithPassword();

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertFalse(userSaved);

    verify(communityUser).validatePassword(password);
    verify(communityUser, never()).setPassword(eq(newPassword), anyString());
    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserWithPasswordException() {
    when(communityUser.validatePassword(password)).thenThrow(new IllegalArgumentException(""));
    when(messageContext.hasErrorMessages()).thenReturn(false).thenReturn(true);
    when(passwordPolicy.verify(newPassword)).thenReturn(true);

    UserDetails details = getUserDetailsWithPassword();
    details.setReceiveCommentReplyEmails(true);
    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertFalse(userSaved);

    verify(communityUser).validatePassword(password);
    verify(communityUser, never()).setPassword(eq(newPassword), anyString());
    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserDeleteImage() {
    when(messageContext.hasErrorMessages()).thenReturn(false);

    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    details.setDeleteProfileImage(true);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertTrue(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    verify(communityUser).setImage(null);
    verify(mailTemplateService).sendMail("profileChanged", locale, emailAddress, getProperties());
  }

  @Test
  public void saveUserWithImage() throws IOException {

    when(messageContext.hasErrorMessages()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("filename");

    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    when(file.getSize()).thenReturn(1000L);

    boolean userSaved = userDetailsHelper.save(details, requestContext, file);
    assertTrue(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.NONE);
    verify(blobService).put(inputStream, contentType, "filename");
    verify(communityUser).setImage(blob);
    verify(mailTemplateService).sendMail("profileChanged", locale, emailAddress, getProperties());
  }

  @Test
  public void saveUserWithImageTooBig() throws IOException {
    when(communityUser.getLocale()).thenReturn(Locale.GERMAN);
    when(messageContext.hasErrorMessages()).thenReturn(false).thenReturn(true);

    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    when(file.getSize()).thenReturn(512001L);

    boolean userSaved = userDetailsHelper.save(details, requestContext, file);
    assertFalse(userSaved);

    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(blobService, never()).put(inputStream, contentType, "filename");
    verify(communityUser, never()).setImage(blob);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserWithImageUnsupportedContentType() throws IOException {
    when(communityUser.getLocale()).thenReturn(Locale.GERMAN);
    when(messageContext.hasErrorMessages()).thenReturn(false).thenReturn(true);

    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    when(file.getSize()).thenReturn(512000L);
    when(file.getContentType()).thenReturn("abcd");

    boolean userSaved = userDetailsHelper.save(details, requestContext, file);
    assertFalse(userSaved);

    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(blobService, never()).put(inputStream, contentType, "filename");
    verify(communityUser, never()).setImage(blob);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }


  @Test
  public void saveUserWithBlobException() {
    when(communityUser.getLocale()).thenReturn(Locale.GERMAN);
    when(blobService.put(inputStream, contentType, "filename")).thenThrow(new BlobException("I/O error"));

    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    when(file.getSize()).thenReturn(1000L);
    when(file.getOriginalFilename()).thenReturn("filename");
    when(messageContext.hasErrorMessages()).thenReturn(false).thenReturn(true);

    boolean userSaved = userDetailsHelper.save(details, requestContext, file);
    assertFalse(userSaved);

    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(blobService).put(inputStream, contentType, "filename");
    verify(communityUser, never()).setImage(blob);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserWithIOException() throws IOException {
    when(communityUser.getLocale()).thenReturn(Locale.GERMAN);
    when(file.getInputStream()).thenThrow(new IOException("I/O error"));

    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    when(file.getSize()).thenReturn(1000L);
    when(messageContext.hasErrorMessages()).thenReturn(false).thenReturn(true);

    boolean userSaved = userDetailsHelper.save(details, requestContext, file);
    assertFalse(userSaved);

    verify(communityUserService, never()).storeChanges(communityUser, ModerationType.NONE);
    verify(blobService, never()).put(inputStream, contentType, "filename");
    verify(communityUser, never()).setImage(blob);
    verify(mailTemplateService, never()).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(messageContext, times(1)).addMessage(any(MessageResolver.class));
  }

  @Test
  public void saveUserPreModeration() {
    ModerationType moderationType = ModerationType.PRE_MODERATION;
    when(elasticSocialConfiguration.getUserModerationType()).thenReturn(moderationType);
    when(messageContext.hasErrorMessages()).thenReturn(false);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertTrue(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.PRE_MODERATION);
    verify(mailTemplateService).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(communityUserService).storeChanges(communityUser, moderationType);
  }

  @Test
  public void saveUserPostModeration() {
    ModerationType moderationType = ModerationType.POST_MODERATION;
    when(elasticSocialConfiguration.getUserModerationType()).thenReturn(moderationType);
    when(messageContext.hasErrorMessages()).thenReturn(false);

    boolean userSaved = userDetailsHelper.save(details, requestContext, null);
    assertTrue(userSaved);

    verify(communityUserService).storeChanges(communityUser, ModerationType.POST_MODERATION);
    verify(mailTemplateService).sendMail("profileChanged", locale, emailAddress, getProperties());
    verify(communityUserService).storeChanges(communityUser, moderationType);
  }

  @Test
  public void getLocales() {
    List<LocalizedLocale> locales = userDetailsHelper.getLocales(requestContext);
    assertNotNull(locales);
    assertTrue(locales.size() > 0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void redirectOnLogout() {
    UserContext.clear();
    when(flowUrlHelper.getRootPageUrl(requestContext)).thenReturn("link");
    userDetailsHelper.redirectOnLogout(requestContext, null);
    verify(externalContext).requestExternalRedirect("serverRelative:link");
  }

  @Test
  public void noRedirectUserLoggedIn() {
    userDetailsHelper.redirectOnLogout(requestContext, null);
    verify(externalContext, never()).requestExternalRedirect("contextRelative:");
  }

  @Test
  public void noRedirectUserLoggedInAuthorNameNotNull() {
    userDetailsHelper.redirectOnLogout(requestContext, userName);
    verify(externalContext, never()).requestExternalRedirect("contextRelative:");
  }

  @Test
  public void noRedirectUserNotLoggedInAuthorNameNotNull() {
    UserContext.clear();
    userDetailsHelper.redirectOnLogout(requestContext, userName);
    verify(externalContext, never()).requestExternalRedirect("contextRelative:");
  }

  @Test
  public void getCommentAuthorName() {
    MockParameterMap map = new MockParameterMap();
    map.put("userName", userName);
    when(externalContext.getRequestParameterMap()).thenReturn(map);

    String authorName = userDetailsHelper.getCommentAuthorName(requestContext);
    assertEquals(userName, authorName);
  }

  @Test
  public void getCommentAuthorNameNull() {
    MockParameterMap map = new MockParameterMap();
    when(externalContext.getRequestParameterMap()).thenReturn(map);

    String authorName = userDetailsHelper.getCommentAuthorName(requestContext);
    assertEquals(null, authorName);
  }

  @Test
  public void timeZones() {
    List<TimeZone> timeZones = UserDetailsHelper.getTimeZones();
    assertNotNull(timeZones);
    assertTrue(timeZones.size() > 0);
  }

  private Map<String, Object> getProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("name", userName);
    properties.put("givenName", givenName);
    properties.put("surName", surName);
    properties.put("test", "123");
    return properties;
  }

  private UserDetails getUserDetails() {
    details.setLocalizedLocale(new LocalizedLocale(locale, locale.getDisplayLanguage()));
    details.setEmailAddress(emailAddress);
    details.setGivenname(givenName);
    details.setSurname(surName);
    details.setUsername(userName);
    details.setReceiveCommentReplyEmails(false);
    details.setTimeZoneId("UTC");
    details.setPasswordPolicy(passwordPolicy);
    return details;
  }

  private UserDetails getUserDetailsWithPassword() {
    details.setPassword(password);
    details.setNewPassword(newPassword);
    details.setNewPasswordRepeat(newPassword);
    return details;
  }
}
