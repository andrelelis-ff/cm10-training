package com.coremedia.blueprint.cae.action.subscription;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 * Validator for {@link SubscriptionForm}
 */
public class SubscriptionFormValidator implements Validator {

  private static final Pattern EMAILADDRESS_PATTERN =
          Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}\\b");

  @Override
  public boolean supports(Class<?> clazz) {
    return SubscriptionForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    SubscriptionForm form = (SubscriptionForm) target;
    ValidationUtils.rejectIfEmptyOrWhitespace(
                            errors,
                            "email",
                            "error-email-missing",
                            "Email is missing");

    if (!errors.hasErrors() && !EMAILADDRESS_PATTERN.matcher(form.getEmail()).matches()) {
      errors.rejectValue(
              "email",
              "error-email-format",
              "Not a valid email address");
    }
  }
}
