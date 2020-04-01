package com.coremedia.blueprint.elastic.social.cae.guid;

import com.coremedia.elastic.core.api.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Public/private key pair generated dependent on JDK (IBM JDK or Oracle JDK)
 */
class RSAKeyPair {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAKeyPair.class);
  private static final String ALGORITHM = "RSA";
  private static final String FORMAT = "PKCS#8";
  private static final int DEFAULT_KEYSIZE = 2048;
  private static final int MINIMUM_KEY_BYTES_LENGTH = 32;

  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  RSAKeyPair(PrivateKey privateKey, PublicKey publicKey) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  PrivateKey getPrivateKey() {
    return privateKey;
  }

  PublicKey getPublicKey() {
    return publicKey;
  }

  @SuppressWarnings("all")
  static RSAKeyPair createFrom(Settings settings) throws NoSuchAlgorithmException {
    String privateKeyToken = settings.getString("signCookie.privateKey");
    String publicKeyString = settings.getString("signCookie.publicKey");
    if (privateKeyToken != null && publicKeyString != null) {
      String[] split = privateKeyToken.split("#");
      String privateKeyString = split[0];
      byte[] publicKeyBytes = Base64.decodeBase64(publicKeyString);
      byte[] privateKeyBytes = Base64.decodeBase64(privateKeyString);
      if (isLongEnough(privateKeyBytes) && isLongEnough(publicKeyBytes)) {
        try {
          X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
          KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
          PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
          // IBM JDK requires private key of type RSAPrivateKey
          if (split.length == 3) {
            return new RSAKeyPair(new RSAPrivateKeyFromBytes(privateKeyBytes, new BigInteger(split[1]), new BigInteger(split[2])), pubKey);
          }
          // oracle JDK can do with private key of type PrivateKey
          return new RSAKeyPair(new PrivateKeyFromBytes(privateKeyBytes), pubKey);
        } catch (Exception e) {
          LOGGER.warn("Failed to initialize RSA key pair from 'signCookie' settings: {}", e.getMessage(), e);
        }
      }
    }

    if (LOGGER.isWarnEnabled()) {
      StringBuilder sb = new StringBuilder();

      if (privateKeyToken == null && publicKeyString == null) {
        sb.append("Missing mandatory properties signCookie.privateKey and signCookie.publicKey. ");
      } else if (privateKeyToken == null) {
        sb.append("Missing mandatory property signCookie.privateKey. ");
      } else if (publicKeyString == null) {
        sb.append("Missing mandatory property signCookie.publicKey. ");
      }
      sb.append("Generating a new key pair to be used instead.");
      LOGGER.warn(sb.toString());
    }

    return generateKeyPair();
  }

  private static boolean isLongEnough(byte[] bytes) {
    return bytes != null && bytes.length >= MINIMUM_KEY_BYTES_LENGTH;
  }

  private static RSAKeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
    kpg.initialize(DEFAULT_KEYSIZE);
    KeyPair kp = kpg.genKeyPair();
    final PublicKey publicKey = kp.getPublic();
    final PrivateKey privateKey = kp.getPrivate();

    if (LOGGER.isWarnEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Please add the following newly generated key values to the settings of all existing CAEs: ")
              .append("signCookie.privateKey=")
              .append(Base64.encodeBase64String(privateKey.getEncoded()));

      // should always be a RSAPrivateKey, just to make it really safe here
      if (privateKey instanceof RSAPrivateKey) {
        final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) kp.getPrivate();
        sb.append('#')
                .append(rsaPrivateKey.getPrivateExponent())
                .append('#')
                .append(rsaPrivateKey.getModulus());
      }

      sb.append(" , signCookie.publicKey=")
              .append(Base64.encodeBase64String(publicKey.getEncoded()));
      LOGGER.warn(sb.toString());
    }

    return new RSAKeyPair(privateKey, publicKey);
  }

  private static class PrivateKeyFromBytes implements PrivateKey {
    private static final long serialVersionUID = 42L;
    private final byte[] privateKeyBytes;

    public PrivateKeyFromBytes(byte[] privateKeyBytes) {
      this.privateKeyBytes = privateKeyBytes.clone();
    }

    @Override
    public String getAlgorithm() {
      return ALGORITHM;
    }

    @Override
    public String getFormat() {
      return FORMAT;
    }

    @Override
    public byte[] getEncoded() {
      return privateKeyBytes;
    }
  }

  private static class RSAPrivateKeyFromBytes extends PrivateKeyFromBytes implements RSAPrivateKey {

    private final BigInteger privateExponent;
    private final BigInteger modulus;

    public RSAPrivateKeyFromBytes(byte[] privateKeyBytes, BigInteger privateExponent, BigInteger modulus) {
      super(privateKeyBytes);
      this.privateExponent = privateExponent;
      this.modulus = modulus;
    }

    @Override
    public BigInteger getPrivateExponent() {
      return privateExponent;
    }

    @Override
    public BigInteger getModulus() {
      return modulus;
    }
  }
}
