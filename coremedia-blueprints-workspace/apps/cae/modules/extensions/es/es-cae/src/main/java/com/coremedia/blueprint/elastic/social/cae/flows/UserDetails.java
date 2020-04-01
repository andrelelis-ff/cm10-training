package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.blueprint.elastic.social.cae.controller.BlobRef;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.social.api.users.CommunityUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import static com.coremedia.blueprint.elastic.social.cae.flows.ValidationUtil.MINIMUM_PASSWORD_LENGTH;
import static com.coremedia.blueprint.elastic.social.cae.flows.ValidationUtil.MINIMUM_USERNAME_LENGTH;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @cm.template.api
 */
public class UserDetails implements Serializable {
  private static final long serialVersionUID = 42L;

  private String id;
  private @PersonalData String username;
  private Date registrationDate;
  private Date lastLoginDate;
  private @PersonalData String emailAddress;
  private @PersonalData BlobRef profileImage;
  private long numberOfLogins;
  private long numberOfComments;
  private long numberOfRatings;
  private long numberOfLikes;
  private long numberOfReviews;
  private @PersonalData LocalizedLocale localizedLocale;
  private @PersonalData String timeZoneId;
  private boolean viewOwnProfile = false;
  private boolean preModerationChanged = false;
  private @PersonalData String givenname;
  private @PersonalData String surname;
  private @PersonalData String password;
  private @PersonalData String newPassword;
  private @PersonalData String newPasswordRepeat;
  private boolean receiveCommentReplyEmails;
  private PasswordPolicy passwordPolicy;
  private boolean deleteProfileImage = false;
  private boolean preview = false;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public @PersonalData String getUsername() {
    return username;
  }

  public void setUsername(@PersonalData String username) {
    this.username = username;
  }

  /**
   * @cm.template.api
   */
  public Date getRegistrationDate() {
    return registrationDate == null ? null : new Date(registrationDate.getTime());
  }

  public void setRegistrationDate(Date registrationDate) {
    this.registrationDate = registrationDate == null ? null : new Date(registrationDate.getTime());
  }

  /**
   * @cm.template.api
   */
  public Date getLastLoginDate() {
    return lastLoginDate == null ? null : new Date(lastLoginDate.getTime());
  }

  public void setLastLoginDate(Date lastLoginDate) {
    this.lastLoginDate = lastLoginDate == null ? null : new Date(lastLoginDate.getTime());
  }

  public @PersonalData String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(@PersonalData String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * @cm.template.api
   */
  public long getNumberOfLogins() {
    return numberOfLogins;
  }

  public void setNumberOfLogins(long numberOfLogins) {
    this.numberOfLogins = numberOfLogins;
  }

  /**
   * @cm.template.api
   */
  public long getNumberOfComments() {
    return numberOfComments;
  }

  public void setNumberOfComments(long numberOfComments) {
    this.numberOfComments = numberOfComments;
  }

  /**
   * @cm.template.api
   */
  public long getNumberOfRatings() {
    return numberOfRatings;
  }

  public void setNumberOfRatings(long numberOfRatings) {
    this.numberOfRatings = numberOfRatings;
  }

  /**
   * @cm.template.api
   */
  public long getNumberOfLikes() {
    return numberOfLikes;
  }

  public void setNumberOfLikes(long numberOfLikes) {
    this.numberOfLikes = numberOfLikes;
  }

  /**
   * Returns the number of approved reviews from the current user.
   * @return The number of approved reviews from the current user.
   * @cm.template.api
   */
  public long getNumberOfReviews() {
    return numberOfReviews;
  }

  public void setNumberOfReviews(long numberOfReviews) {
    this.numberOfReviews = numberOfReviews;
  }

  /**
   * @cm.template.api
   */
  public @PersonalData BlobRef getProfileImage() {
    return profileImage;
  }

  public void setProfileImage(@PersonalData BlobRef profileImage) {
    this.profileImage = profileImage;
  }

  public @PersonalData LocalizedLocale getLocalizedLocale() {
    return localizedLocale;
  }

  public void setLocalizedLocale(@PersonalData LocalizedLocale localizedLocale) {
    this.localizedLocale = localizedLocale;
  }

  /**
   * @cm.template.api
   */
  public boolean isViewOwnProfile() {
    return viewOwnProfile;
  }

  public void setViewOwnProfile(boolean viewOwnProfile) {
    this.viewOwnProfile = viewOwnProfile;
  }

  public @PersonalData String getGivenname() {
    return givenname;
  }

  public void setGivenname(@PersonalData String givenname) {
    this.givenname = givenname;
  }

  public @PersonalData String getSurname() {
    return surname;
  }

  public void setSurname(@PersonalData String surname) {
    this.surname = surname;
  }

  public @PersonalData String getPassword() {
    return password;
  }

  public void setPassword(@PersonalData String password) {
    this.password = password;
  }

  public @PersonalData String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(@PersonalData String newPassword) {
    this.newPassword = newPassword;
  }

  public @PersonalData String getNewPasswordRepeat() {
    return newPasswordRepeat;
  }

  public void setNewPasswordRepeat(@PersonalData String newPasswordRepeat) {
    this.newPasswordRepeat = newPasswordRepeat;
  }

  public PasswordPolicy getPasswordPolicy() {
    return passwordPolicy;
  }

  public void setPasswordPolicy(PasswordPolicy passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
  }

  public boolean isDeleteProfileImage() {
    return deleteProfileImage;
  }

  public void setDeleteProfileImage(boolean deleteProfileImage) {
    this.deleteProfileImage = deleteProfileImage;
  }

  public @PersonalData Locale getLocale() {
    return localizedLocale != null ? localizedLocale.getLocale() : null;
  }

  /**
   * @cm.template.api
   */
  public boolean isPreModerationChanged() {
    return preModerationChanged;
  }

  public void setPreModerationChanged(boolean preModerationChanged) {
    this.preModerationChanged = preModerationChanged;
  }

  /**
   * @cm.template.api
   */
  public boolean isPreview() {
    return preview;
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  public void validate(RequestContext context) {

    if (isBlank(username)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_USERNAME_ERROR, "username");
    } else if (!ValidationUtil.validateUsernameLength(username)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_USERNAME_TOO_SHORT_ERROR, "username", MINIMUM_USERNAME_LENGTH);
    }

    validateName(context);

    if (isBlank(emailAddress)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_EMAIL_ADDRESS_ERROR, "emailAddress");
    } else if (!ValidationUtil.validateEmailAddressSyntax(emailAddress)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_EMAIL_ADDRESS_SYNTAX_ERROR, "emailAddress");
    }

    if (localizedLocale == null || localizedLocale.getDisplayLanguage() == null || localizedLocale.getLocale() == null) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_LOCALE_ERROR, "localizedLocale");
    }

    if (isBlank(timeZoneId)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_TIME_ZONE_ERROR, "timeZoneId");
    }

    if (isValidationOfPasswordRequired()) {
      validatePassword(context);
    }
  }

  protected void validateName(RequestContext context) {
    if (isBlank(givenname)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_GIVENNAME_ERROR, "givenname");
    }

    if (isBlank(surname)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_SURNAME_ERROR, "surname");
    }

    if (contains(username, '#')) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_USERNAME_SYNTAX_ERROR, "username");
    }
  }

  protected boolean isValidationOfPasswordRequired() {
    return isNotBlank(password) || isNotBlank(newPassword) || isNotBlank(newPasswordRepeat);
  }

  protected void validatePassword(RequestContext context) {
    if (isBlank(password)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_PASSWORD_ERROR, "password");
    }
    if (isBlank(newPassword)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_NEW_PASSWORD_ERROR, "newPassword");
    }
    if (isBlank(newPasswordRepeat)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_NEW_PASSWORD_REPEAT_ERROR, "newPasswordRepeat");
    }
    if (isNotBlank(newPassword) && !StringUtils.equals(newPassword, newPasswordRepeat)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_PASSWORDS_DO_NOT_MATCH_ERROR, "newPasswordRepeat");
    }
    if (isNotBlank(password) && StringUtils.equals(password, newPassword)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_NEW_PASSWORD_MATCHES_OLD_ERROR, "newPassword");
    }
    if (isNotBlank(newPassword) && passwordPolicy != null && !passwordPolicy.verify(newPassword)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.USER_DETAILS_PASSWORD_TOO_WEAK_ERROR, "newPassword", MINIMUM_PASSWORD_LENGTH);
    }
  }

  public boolean hasChangesWhichNeedModeration(CommunityUser user, CommonsMultipartFile file) {
    boolean result;
    result = !StringUtils.equals(username, user.getName());
    result |= !StringUtils.equals(emailAddress, user.getEmail());
    result |= !StringUtils.equals(givenname, user.getProperty("givenName", String.class));
    result |= !StringUtils.equals(surname, user.getProperty("surName", String.class));
    result |= (file != null && file.getSize() > 0) || isDeleteProfileImage();
    @SuppressWarnings("PersonalData") // while the profile image is @PersonalData, its presence is not
    boolean hasProfileImage = profileImage != null;
    result |= file == null && !isDeleteProfileImage() && hasProfileImage;
    return result;
  }

  @SuppressWarnings("PersonalData") // Safe comparison of @PersonalData: user's locale and time zone.
  public boolean hasChangesWhichDoNotNeedModeration(CommunityUser user) {
    boolean result;
    result = !user.getLocale().equals(getLocale());
    result |= user.isReceiveCommentReplyEmails() != isReceiveCommentReplyEmails();
    result |= user.getTimeZone() != null && !user.getTimeZone().getID().equals(getTimeZoneId());
    return result;
  }

  public boolean isReceiveCommentReplyEmails() {
    return receiveCommentReplyEmails;
  }

  public void setReceiveCommentReplyEmails(boolean receiveCommentReplyEmails) {
    this.receiveCommentReplyEmails = receiveCommentReplyEmails;
  }

  public @PersonalData String getTimeZoneId() {
    return timeZoneId;
  }

  public void setTimeZoneId(@PersonalData String timeZoneId) {
    this.timeZoneId = timeZoneId;
  }
}
