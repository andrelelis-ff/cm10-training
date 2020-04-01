package com.coremedia.blueprint.cae.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 * Default blueprint implementation of a {@link SecureHashCodeGeneratorStrategy}
 */
public class DefaultSecureHashCodeGeneratorStrategy implements SecureHashCodeGeneratorStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultSecureHashCodeGeneratorStrategy.class);

  private static final String HASHCODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int HASH_LENGTH = 2;
  private static final String SEPARATOR = ":";

  private Charset checksumCharset = Charset.defaultCharset();

  /**
   * Sets the character encoding used by the checksum algorithm to convert parameters to bytes.
   * <p>
   * Defaults to the VM default file encoding. When using VM default file encodings, generating/validating checksums
   * in a mixed environment of platforms with different default file encodings will fail.
   *
   * To avoid such problems, it is recommended to set a fixed encoding to be used on all platforms, e.g. "UTF-8".
   * </p>
   *
   * @param encoding character set name, as returned by {@link java.nio.charset.Charset#availableCharsets()}
   *
   * @throws java.nio.charset.IllegalCharsetNameException if the given {@code encoding} is illegal
   * @throws IllegalArgumentException if the given {@code encoding} is null
   * @throws java.nio.charset.UnsupportedCharsetException if no support for the named charset is available in this instance of the Java virtual machine
   *
   * @since cm7-23
   */
  public void setEncoding(String encoding) {
    checksumCharset = Charset.forName(encoding);
  }

  @Override
  public boolean matches(Map<String, Object> parameters, String secureHashCode) {

    String convertedMap = convertParameterMap(parameters);

    String expectedSecHash = generateSecureHashCode(convertedMap);

    return secureHashCode.equals(expectedSecHash);
  }

  @Override
  public String generateSecureHashCode(Map<String, Object> parameters) {

    String convertedMap = convertParameterMap(parameters);

    return generateSecureHashCode(convertedMap);
  }

  //====================================================================================================================

  /**
   * @return parameters converted to a string separated by {@link #SEPARATOR}
   */
  private String convertParameterMap(Map<String, Object> parameters) {
    StringBuilder sb = new StringBuilder();
    for (String parameter : sortedValues(parameters)) {
      sb.append(parameter);
      sb.append(SEPARATOR);
    }
    return sb.toString();
  }

  /**
   * @param parameters a Map
   * @return A sorted collection of parameters' values' toString() results
   */
  private Collection<String> sortedValues(Map<String, Object> parameters) {
    TreeSet<String> sorted = new TreeSet<>();
    for (Object parameter : parameters.values()) {
      sorted.add(parameter.toString());
    }
    return sorted;
  }

  /**
   * Method to provide a hash value based on the URL to prevent malicious
   * access to CAE objects by URL manipulation
   *
   * @param token the String to generate the secure hashcode from
   * @return the generated hashcode
   */
  private String generateSecureHashCode(String token) {
    String retval = "";
    if (token != null && token.length() > 0) {

      // Take in a URL find its hash using Adler32
      byte[] bytes = token.getBytes(checksumCharset);
      Checksum checksumEngine = new Adler32();
      checksumEngine.update(bytes, 0, bytes.length);
      long checksum = checksumEngine.getValue();

      // Using hashLength, calculate how many possibilities can we have.
      int modValue = HASHCODE_CHARSET.length();
      for (int i = 1; i < HASH_LENGTH; i++) {
        modValue = modValue * HASHCODE_CHARSET.length();
      }
      // MOD our Checksum by the number of possibilities
      long modCheck = (checksum % modValue);

      // Encode the result, but PAD it out first
      retval = encode((int) modCheck);
      while (retval.length() < HASH_LENGTH) {
        retval = HASHCODE_CHARSET.charAt(0) + retval;
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Hashcode for token '" + token + "' is '" + retval + "'");
    }
    return retval;
  }

  /**
   * @return parameter encoded with {@link #HASHCODE_CHARSET}
   */
  private String encode(int decimalNumber) {
    return encode(decimalNumber, HASHCODE_CHARSET);
  }

  /**
   * @return parameter encoded with given charset
   */
  private String encode(int decimalNumber, String charSet) {
    int tempDecimalNumber = decimalNumber;
    String tempVal = tempDecimalNumber == 0 ? "0" : "";
    int mod;
    int base = charSet.length();
    while (tempDecimalNumber != 0) {
      mod = tempDecimalNumber % base;
      tempVal = charSet.substring(mod, mod + 1) + tempVal;
      tempDecimalNumber = tempDecimalNumber / base;
    }
    return tempVal;
  }
}