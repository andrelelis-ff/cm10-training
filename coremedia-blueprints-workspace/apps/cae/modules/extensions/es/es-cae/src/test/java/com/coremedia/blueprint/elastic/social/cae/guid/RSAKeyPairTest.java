package com.coremedia.blueprint.elastic.social.cae.guid;

import com.coremedia.elastic.core.api.settings.Settings;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *  Add a test that checks that private keys defined in the settings can be used.
 *  There are tests for two variants, the old and the new format.
 *  Since IBM JDK cannot handle the old format, that test is excluded for the Java vendor.
 */
public class RSAKeyPairTest {

  @Test
  public void testCreateFromExtended() throws Exception {

    Settings settings = mock(Settings.class);

    // check that a new set of keys is generated
    RSAKeyPair rsaKeyPair = RSAKeyPair.createFrom(settings);
    assertNotNull(rsaKeyPair);

    // use the newly generated key as setting
    RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyPair.getPrivateKey();
    when(settings.getString("signCookie.publicKey")).thenReturn(
            Base64.encodeBase64String(rsaKeyPair.getPublicKey().getEncoded()));
    when(settings.getString("signCookie.privateKey")).thenReturn(
            Base64.encodeBase64String(privateKey.getEncoded()) + "#" + privateKey.getPrivateExponent().toString() + "#" + privateKey.getModulus().toString());

    RSAKeyPair newRsaKeyPair = RSAKeyPair.createFrom(settings);
    RSAPrivateKey newRsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivateKey();

    compare(rsaKeyPair.getPublicKey().getEncoded(), newRsaKeyPair.getPublicKey().getEncoded());
    compare(privateKey.getEncoded(), newRsaKeyPair.getPrivateKey().getEncoded());
    assertEquals(privateKey.getPrivateExponent(), newRsaPrivateKey.getPrivateExponent());
    assertEquals(privateKey.getModulus(), newRsaPrivateKey.getModulus());
  }

  @Test
  public void testCreateFromLegacy() throws Exception {
    Properties prop = System.getProperties();
    assumeTrue(!prop.getProperty("java.vendor").contains("IBM"));
    Settings settings = mock(Settings.class);

    // check that a new set of keys is generated
    RSAKeyPair rsaKeyPair = RSAKeyPair.createFrom(settings);
    assertNotNull(rsaKeyPair);

    // use the newly generated key as setting
    RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyPair.getPrivateKey();
    when(settings.getString("signCookie.publicKey")).thenReturn(Base64.encodeBase64String(rsaKeyPair.getPublicKey().getEncoded()));
    when(settings.getString("signCookie.privateKey")).thenReturn(Base64.encodeBase64String(privateKey.getEncoded()));

    RSAKeyPair newRsaKeyPair = RSAKeyPair.createFrom(settings);
    RSAPrivateKey newRsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivateKey();

    compare(rsaKeyPair.getPublicKey().getEncoded(), newRsaKeyPair.getPublicKey().getEncoded());
    compare(privateKey.getEncoded(), newRsaKeyPair.getPrivateKey().getEncoded());
    assertEquals(privateKey.getPrivateExponent(), newRsaPrivateKey.getPrivateExponent());
    assertEquals(privateKey.getModulus(), newRsaPrivateKey.getModulus());
  }

  private void compare(byte[] b1, byte[] b2) {
    assertEquals(b1.length, b2.length);
    for(int i=0;i < b1.length; i++) {
      assertEquals(b1[i], b2[i]);
    }
  }
}