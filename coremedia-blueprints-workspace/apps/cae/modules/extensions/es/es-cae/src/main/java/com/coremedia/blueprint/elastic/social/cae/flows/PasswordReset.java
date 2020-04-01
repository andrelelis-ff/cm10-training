package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.common.personaldata.PersonalData;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A simple form bean for use by the password reset web flow.
 */
public class PasswordReset implements Serializable {
  private static final long serialVersionUID = 42L;
  protected static final String PASSWORD = "password"; // NOSONAR false positive: Credentials should not be hard-coded

  private @PersonalData String emailAddress;
  private @PersonalData String password;
  private @PersonalData String confirmPassword;
  private PasswordPolicy passwordPolicy;

  public @PersonalData String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(@PersonalData String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public @PersonalData String getPassword() {
    return password;
  }

  public void setPassword(@PersonalData String password) {
    this.password = password;
  }

  public @PersonalData String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(@PersonalData String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public PasswordPolicy getPasswordPolicy() {
    return passwordPolicy;
  }

  public void setPasswordPolicy(PasswordPolicy passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
  }

  public void validate(RequestContext context) {
    if (isBlank(emailAddress)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.PASSWORD_RESET_EMAIL_ERROR, "emailAddress");
    }
  }

  public void validateResetForm(RequestContext context) {
    if (isBlank(password)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.CONFIRM_PASSWORD_RESET_PASSWORD_ERROR, PASSWORD);
      return;
    }

    @SuppressWarnings("PersonalData") // safe to pass @PersonalData confirmPassword to #equals
    boolean equal = password.equals(confirmPassword);
    if (!equal) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.CONFIRM_PASSWORD_RESET_PASSWORDS_DO_NOT_MATCH, PASSWORD);
    }

    if (passwordPolicy != null && !passwordPolicy.verify(password)) {
      MessageHelper.addErrorMessageWithSource(context, WebflowMessageKeys.CONFIRM_PASSWORD_RESET_PASSWORD_TOO_WEAK, PASSWORD);
    }
  }
}
