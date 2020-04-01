package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.base.elastic.common.ImageHelper;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.elastic.social.cae.controller.BlobRefImpl;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.blobs.BlobException;
import com.coremedia.elastic.core.api.blobs.BlobService;
import com.coremedia.elastic.core.api.staging.StagingService;
import com.coremedia.elastic.core.api.users.DuplicateEmailException;
import com.coremedia.elastic.core.api.users.DuplicateNameException;
import com.coremedia.elastic.core.api.users.User;
import com.coremedia.elastic.social.api.ModerationType;
import com.coremedia.elastic.social.api.comments.CommentService;
import com.coremedia.elastic.social.api.mail.MailTemplateService;
import com.coremedia.elastic.social.api.ratings.LikeService;
import com.coremedia.elastic.social.api.ratings.RatingService;
import com.coremedia.elastic.social.api.reviews.ReviewService;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.coremedia.blueprint.elastic.social.cae.flows.MessageHelper.addErrorMessage;
import static com.coremedia.blueprint.elastic.social.cae.flows.MessageHelper.addErrorMessageWithSource;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Named
public class UserDetailsHelper {
  private static final String USER_NAME_PARAM = "userName";
  private static List<Locale> LOCALES = asList(Locale.CHINA, Locale.US, Locale.FRANCE, Locale.GERMANY, Locale.ITALY, Locale.JAPAN);
  private static final String TEMPLATE_NAME_PROFILE_CHANGE = "profileChanged";
  private static final String EMAIL_ADDRESS = "emailAddress";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password"; // NOSONAR false positive: Credentials should not be hard-coded
  private static final String PROFILE_IMAGE_ID = "profileImage";
  private static List<TimeZone> timeZones = new ArrayList<>();
  private static final int MAX_LENGTH_TIMEZONE_ID = 3;

  @Value("${cae.is.preview}")
  private boolean preview;

  @Inject
  protected CommentService commentService;

  @Inject
  protected RatingService ratingService;

  @Inject
  protected LikeService likeService;

  @Inject
  protected ReviewService reviewService;

  @Inject
  protected MailTemplateService mailTemplateService;

  @Inject
  protected BlobService blobService;

  @Inject
  protected CommunityUserService communityUserService;

  @Inject
  protected ContentRepository contentRepository;

  @Inject
  protected StagingService stagingService;
  
  @Inject
  protected FlowUrlHelper flowUrlHelper;

  @Inject
  protected ElasticSocialPlugin elasticSocialPlugin;

  public UserDetails getUserDetails(RequestContext context, PasswordPolicy passwordPolicy) {
    return getUserDetails(context, passwordPolicy, null);
  }

  public UserDetails getUserDetails(RequestContext context, PasswordPolicy passwordPolicy, String authorName) {
    User loggedInUser = getLoggedInUser();
    Locale requestLocale = getLocaleFromContext(context);

    if (authorName == null) {
      return getDetails(loggedInUser, true, passwordPolicy, requestLocale, preview);
    }
    if (isBlank(authorName)) {
      return null;
    }
    User user = communityUserService.getUserByName(authorName);
    if (user == null) {
      return null;
    }
    CommunityUser communityUser = communityUserService.createFrom(user);
    if (preview || communityUser.isActivated() || communityUser.isActivatedAndRequiresModeration()) {
      return getDetails(user, user.equals(loggedInUser), passwordPolicy, requestLocale, preview);
    } else if (communityUser.isIgnored() && user.equals(loggedInUser)) {
      return getDetails(user, true, passwordPolicy, requestLocale, preview);
    }
    return null;
  }

  public void redirectOnLogout(RequestContext context, String authorName) {
    if (isBlank(authorName) && UserContext.getUser() == null) {
      String url = flowUrlHelper.getRootPageUrl(context);
      if (isNotBlank(url)) {
        context.getExternalContext().requestExternalRedirect("serverRelative:" + url);
      }
    }
  }

  public String getCommentAuthorName(RequestContext context) {
    final String authorName = context.getExternalContext().getRequestParameterMap().get(USER_NAME_PARAM);
    if (isBlank(authorName)) {
      return null;
    }
    return authorName;
  }

  public Locale getLocaleFromContext(RequestContext context) {
    return context.getExternalContext().getLocale();
  }

  public void deleteUser() {
    CommunityUser user = getLoggedInUser();
    if (user != null) {
      communityUserService.anonymize(user);
    }
  }

  public boolean save(UserDetails userDetails, RequestContext context, CommonsMultipartFile file) {
    userDetails.validate(context);
    final boolean result = !context.getMessageContext().hasErrorMessages() && doSave(userDetails, context, file);
    userDetails.setDeleteProfileImage(false);
    return result;
  }

  public boolean doSave(UserDetails userDetails, RequestContext context, CommonsMultipartFile file) {
    CommunityUser user = getLoggedInUser();
    if (user != null) {
      boolean hasChanges = false;
      if (validatePassword(user, context, userDetails.getPassword(), userDetails.getNewPassword())) {
        hasChanges = true;
        user.setPassword(userDetails.getNewPassword(), communityUserService.getPasswordHashAlgorithm());
      }
      if (userDetails.hasChangesWhichDoNotNeedModeration(user)) {
        hasChanges = true;
        user = setChangesWhichDoNotNeedModeration(userDetails, user);
      }
      if (userDetails.hasChangesWhichNeedModeration(user, file)) {
        hasChanges = true;
        user = setChangesForModeration(userDetails, user, file, context);
      }
      if (context.getMessageContext().hasErrorMessages()) {
        return false;
      }
      if (!hasChanges) {
        return true;
      }
      try {
        user = saveChanges(user, context);
        UserContext.setUser(user);
        mailTemplateService.sendMail(
                TEMPLATE_NAME_PROFILE_CHANGE,
                user.getLocale(),
                user.getEmail(),
                user.getProperties()
        );
        return true;
      } catch (DuplicateEmailException e) {
        addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_EMAIL_ADDRESS_NOT_AVAILABLE_ERROR, EMAIL_ADDRESS);

      } catch (DuplicateNameException e) {
        addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_USERNAME_NOT_AVAILABLE_ERROR, USERNAME);
      }
    }
    return false;
  }

  public List<LocalizedLocale> getLocales(RequestContext context) {
    Locale requestLocale = getLocaleFromContext(context);
    List<LocalizedLocale> localizedLocales = new ArrayList<>();
    for (Locale locale : LOCALES) {
      localizedLocales.add(new LocalizedLocale(locale, locale.getDisplayLanguage(requestLocale)));
    }
    Collections.sort(localizedLocales, new LanguageComparator());
    return localizedLocales;
  }

  public static List<TimeZone> getTimeZones() {
    if (timeZones.isEmpty()) {
      timeZones = generateTimeZonesList();
    }
    Collections.sort(timeZones, new TimeZoneComparator());
    return timeZones;
  }

  private static List<TimeZone> generateTimeZonesList() {
    List<String> timeZoneIds = new ArrayList<>(Arrays.asList(TimeZone.getAvailableIDs()));
    ArrayList<TimeZone> zoneList = new ArrayList<>();
    for (String timeZoneId : timeZoneIds) {
      TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
      if (!removeTimeZone(timeZone)) {
        zoneList.add(timeZone);
      }
    }
    return zoneList;
  }

  private static boolean removeTimeZone(TimeZone zone) {
    String[] removePrefixes = new String[]{
            "Etc/Greenwich", "Etc/UCT", "Etc/UTC", "Etc/Universal", "Etc/Zulu",
            "Zulu", "Etc/GMT-0", "Etc/GMT0", "GMT0", "Greenwich", "Universal", "Etc", "SystemV"
    };
    String[] protectedPrefixes = new String[]{
            "UTC"
    };
    for (String protect : protectedPrefixes) {
      if (StringUtils.startsWithIgnoreCase(zone.getID(), protect)) {
        return false;
      }
    }
    if (!StringUtils.contains(zone.getID(), "/")) {
      return true;
    }
    for (String remove : removePrefixes) {
      if (StringUtils.startsWithIgnoreCase(zone.getID(), remove)) {
        return true;
      }
    }
    return zone.getID().length() == MAX_LENGTH_TIMEZONE_ID;
  }

  protected UserDetails getDetails(User user, boolean viewOwnProfile, PasswordPolicy passwordPolicy, Locale requestLocale, boolean preview) {
    if (user != null) {
      CommunityUser communityUser = communityUserService.createFrom(user);
      final UserDetails details = createUserDetails();
      if (viewOwnProfile || preview) {
        if (communityUser.hasChangesForPreModeration()) {
          details.setPreModerationChanged(true);
          communityUser.applyChangesFromPreModeration();
        }
        if (preview) {
          stagingService.applyChanges(communityUser);
        }
        details.setViewOwnProfile(viewOwnProfile);
        details.setEmailAddress(communityUser.getEmail());
        details.setGivenname(communityUser.getGivenName());
        details.setSurname(communityUser.getSurName());
      }
      details.setUsername(communityUser.getName());
      details.setProfileImage(communityUser.getImage() != null ? new BlobRefImpl(communityUser.getImage().getId()) : null);
      details.setId(communityUser.getId());
      details.setLastLoginDate(communityUser.getLastLoginDate());
      details.setRegistrationDate(communityUser.getRegistrationDate());
      details.setNumberOfLogins(communityUserService.getNumberOfLogins(communityUser));
      details.setNumberOfComments(commentService.getNumberOfApprovedComments(communityUser));
      details.setNumberOfRatings(ratingService.getNumberOfRatingsFromUser(communityUser));
      details.setNumberOfLikes(likeService.getNumberOfLikesFromUser(communityUser));
      details.setNumberOfReviews(reviewService.getNumberOfApprovedReviews(communityUser));

      @PersonalData Locale userLocale = communityUser.getLocale();

      // safe to pass @PersonalData userLocale to LocalizedLocale as it's assigned to @PersonalData variable afterwards
      @SuppressWarnings("PersonalData")
      @PersonalData LocalizedLocale localizedLocale = userLocale != null
                                                      ? new LocalizedLocale(userLocale, userLocale.getDisplayLanguage(requestLocale))
                                                      : null;
      details.setLocalizedLocale(localizedLocale);
      details.setPasswordPolicy(passwordPolicy);
      details.setPreview(preview);
      details.setReceiveCommentReplyEmails(communityUser.isReceiveCommentReplyEmails());
      if (communityUser.getTimeZone() != null) {
        details.setTimeZoneId(communityUser.getTimeZone().getID());
      }
      return details;
    }
    return null;
  }

  /**
   * Can be overwritten with another details helper to provider a more detailed model.
   */
  protected UserDetails createUserDetails() {
    return new UserDetails();
  }

  private boolean isConnectedWithProvider(String providerId, @PersonalData Collection<String> providerIds) {
    if (providerIds != null) {
      for (@PersonalData String id : providerIds) {
        if (id.startsWith(providerId + ":")) {
          return true;
        }
      }
    }
    return false;
  }

  @VisibleForTesting
  public CommunityUser getLoggedInUser() {
    return UserContext.getUser();
  }

  private CommunityUser setChangesForModeration(UserDetails userDetails, CommunityUser user, CommonsMultipartFile file, RequestContext context) {
    @PersonalData Blob profileImage = null;
    if (!userDetails.isDeleteProfileImage()) {
      profileImage = saveProfileImage(context, file);
    }
    user.setEmail(userDetails.getEmailAddress());
    user.setName(userDetails.getUsername());

    user.setGivenName(userDetails.getGivenname());
    user.setSurName(userDetails.getSurname());
    if (userDetails.isDeleteProfileImage()) {
      user.setImage(null);
    } else if (profileImage != null) {
      user.setImage(profileImage);
    } else if (userDetails.getProfileImage() != null) {
      user.setImage(blobService.get(userDetails.getProfileImage().getId()));
    }
    return user;
  }

  public CommunityUser saveChanges(CommunityUser user, RequestContext context) {
    Page page = RequestAttributeConstants.getPage((HttpServletRequest) context.getExternalContext().getNativeRequest());
    ElasticSocialConfiguration elasticSocialConfiguration = elasticSocialPlugin.getElasticSocialConfiguration(page);

    ModerationType moderationType = elasticSocialConfiguration.getUserModerationType();
    communityUserService.storeChanges(user, moderationType);
    return user;
  }

  private CommunityUser setChangesWhichDoNotNeedModeration(UserDetails userDetails, CommunityUser user) {
    user.setLocale(userDetails.getLocale());
    user.setReceiveCommentReplyEmails(userDetails.isReceiveCommentReplyEmails());
    user.setTimeZone(TimeZone.getTimeZone(userDetails.getTimeZoneId()));
    return communityUserService.createFrom(user);
  }

  private @PersonalData Blob saveProfileImage(RequestContext context, CommonsMultipartFile file) {
    if (file != null && file.getSize() > 0) {
      Page page = RequestAttributeConstants.getPage((HttpServletRequest) context.getExternalContext().getNativeRequest());
      ElasticSocialConfiguration elasticSocialConfiguration = elasticSocialPlugin.getElasticSocialConfiguration(page);

      if (file.getSize() > elasticSocialConfiguration.getMaxImageFileSize()) {
        addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_PROFILE_IMAGE_TOO_BIG_ERROR, PROFILE_IMAGE_ID, ImageHelper.getBytesAsKBString(elasticSocialConfiguration.getMaxImageFileSize()));
      } else if (!ImageHelper.isSupportedMimeType(file.getContentType())) {
        addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_PROFILE_IMAGE_UNSUPPORTED_CONTENT_TYPE, PROFILE_IMAGE_ID, ImageHelper.getSupportedMimeTypesString());
      } else {
        try {
          return blobService.put(file.getInputStream(), file.getContentType(), file.getOriginalFilename());
        } catch (BlobException | IOException e) {
          addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_PROFILE_IMAGE_ERROR, PROFILE_IMAGE_ID);
        }
      }
    }
    return null;
  }

  protected boolean validatePassword(CommunityUser user, RequestContext context,
                                     @PersonalData String oldPassword, @PersonalData String newPassword) {
    if (isNotBlank(newPassword)) {
      try {
        if (user.validatePassword(oldPassword)) {
          return true;
        }
      } catch (IllegalArgumentException e) {
        //password invalid
      }
      addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_OLD_PASSWORD_INVALID_ERROR, PASSWORD);
    }
    return false;
  }

  private static class LanguageComparator implements Comparator<LocalizedLocale>, Serializable {
    private static final long serialVersionUID = 42L;

    @Override
    public int compare(LocalizedLocale locale1, LocalizedLocale locale2) {
      return locale1.getDisplayLanguage().compareTo(locale2.getDisplayLanguage());
    }
  }

  private static class TimeZoneComparator implements Comparator<TimeZone>, Serializable {
    private static final long serialVersionUID = 42L;

    @Override
    public int compare(TimeZone timeZone1, TimeZone timeZone2) {
      return timeZone1.getID().compareTo(timeZone2.getID());
    }
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
  }
}
