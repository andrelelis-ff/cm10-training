package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.social.api.mail.MailException;
import com.coremedia.elastic.social.api.registration.RegistrationService;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import org.springframework.webflow.execution.RequestContext;

import javax.inject.Named;
import javax.inject.Inject;

import static com.coremedia.blueprint.elastic.social.cae.flows.MessageHelper.addErrorMessage;
import static com.coremedia.blueprint.elastic.social.cae.flows.MessageHelper.addInfoMessage;

/**
 * A helper used by the password reset web flow
 */
@Named
public class PasswordResetHelper {
  @Inject
  private RegistrationService registrationService;

  /**
   * Send an e-mail containing a link to the given user's mail address.
   *
   * @param passwordReset   the model used for changing the password
   * @param context the calling flow's {@link org.springframework.webflow.execution.RequestContext}
   * @return true if sending the mail succeeded, false otherwise
   */
  public boolean reset(PasswordReset passwordReset, RequestContext context) {
    if (context.getMessageContext().hasErrorMessages()) {
      return false;
    }
    try {
      boolean result =  registrationService.resetPassword(passwordReset.getEmailAddress());
      addInfoMessage(context, WebflowMessageKeys.PASSWORD_RESET_SUCCESS);
      return result;
    } catch (MailException e) {
      addErrorMessage(context, WebflowMessageKeys.PASSWORD_RESET_MESSAGING_ERROR);
    }
    return false;
  }

  public boolean validate(PasswordReset passwordReset, RequestContext context) {
    passwordReset.validate(context);
    return !context.getMessageContext().hasErrorMessages();
  }
  
  public boolean validateToken(@PersonalData String token) {
    return registrationService.getUserByToken(token) != null;
  }

  /**
   * Confirm a password change.
   *
   * @param token    a token
   * @param passwordReset the model
   * @param context  the calling flow's {@link org.springframework.webflow.execution.RequestContext}
   * @return if successful
   */
  public boolean confirm(@PersonalData String token, PasswordReset passwordReset, RequestContext context) {
    passwordReset.validateResetForm(context);
    if (context.getMessageContext().hasErrorMessages()) {
      return false;
    }
    registrationService.changePassword(token, passwordReset.getPassword());

    return true;
  }

  /**
   * Redirect a logged in user to the home page instead of the password reset page.
   *
   * @param context the executing flow's {@link RequestContext}
   */
  public void redirectLoggedInUserToHomePage(RequestContext context) {
    if (UserContext.getUser() != null) {
      context.getExternalContext().requestExternalRedirect("contextRelative:");
    }
  }
}
