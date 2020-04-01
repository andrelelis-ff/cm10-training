package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.common.personaldata.PersonalData;

import java.util.regex.Pattern;

public final class ValidationUtil {
  //see http://emailregex.com/
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
          "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                  "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                  "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
  , Pattern.CASE_INSENSITIVE);
  public static final int MINIMUM_USERNAME_LENGTH = 3;
  public static final int MINIMUM_PASSWORD_LENGTH = 6;

  private ValidationUtil() {
  }

  public static boolean validateUsernameLength(@PersonalData String username) {
    return username.length() >= MINIMUM_USERNAME_LENGTH;
  }

  @SuppressWarnings("PersonalData") // safe use of personal data
  public static boolean validateEmailAddressSyntax(@PersonalData String emailAddress) {
    return EMAIL_PATTERN.matcher(emailAddress).matches();
  }

}
