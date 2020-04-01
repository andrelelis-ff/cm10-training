package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MinimumLengthPasswordPolicyTest {
  @Test
  public void testVerify() {
    PasswordPolicy passwordPolicy = new MinimumLengthPasswordPolicy();
    assertTrue(passwordPolicy.verify("testme"));
    assertFalse(passwordPolicy.verify("xxxxx"));
  }
}
