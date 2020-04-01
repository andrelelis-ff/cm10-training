package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationUtilTest {
  @Test
  public void validateUsernameLength() throws Exception {
    assertTrue(ValidationUtil.validateUsernameLength("test12"));
    assertFalse(ValidationUtil.validateUsernameLength("xx"));
  }

  @Test
  public void validateEmailAddressSyntax() throws Exception {
    assertTrue(ValidationUtil.validateEmailAddressSyntax("horst@coremedia.com"));
    assertTrue(ValidationUtil.validateEmailAddressSyntax("horst+foo@coremedia.com"));
    assertTrue(ValidationUtil.validateEmailAddressSyntax("Horst-Foo@coremedia.com"));
    assertTrue(ValidationUtil.validateEmailAddressSyntax("horst@coremedia.cloud"));
    assertFalse(ValidationUtil.validateEmailAddressSyntax("horst@coremedia"));
  }

}
