package com.coremedia.blueprint.elastic.social.cae.flows;

import com.coremedia.common.personaldata.PersonalData;

/**
 * Implement this interface to enforce password strength requirements.
 */
public interface PasswordPolicy {
  /**
   * Check the given passwords strength.
   *
   * @param password the password to be checked
   * @return true, if password meets the requirements, false otherwise
   */
  boolean verify(@PersonalData String password);
}
