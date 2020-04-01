package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.common.personaldata.PersonalData;

import javax.inject.Named;
import java.io.Serializable;

/**
 * A simple password strength checker.
 */
@Named("passwordPolicy")
public class MinimumLengthPasswordPolicy implements PasswordPolicy, Serializable {
  private static final long serialVersionUID = 42L;

  /**
   * Check the given password's strength.
   * Password must be longer than 3 characters.
   *
   * @param password the password to be checked
   * @return true, if password meets the requirements, false otherwise
   */
  @Override
  public boolean verify(@PersonalData String password) {
    return password.length() >= ValidationUtil.MINIMUM_PASSWORD_LENGTH;
  }
}
