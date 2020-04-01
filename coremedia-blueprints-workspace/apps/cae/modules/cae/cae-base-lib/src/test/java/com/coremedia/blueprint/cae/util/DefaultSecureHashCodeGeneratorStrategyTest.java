package com.coremedia.blueprint.cae.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test {@link com.coremedia.blueprint.cae.util.DefaultSecureHashCodeGeneratorStrategy}
 */
public class DefaultSecureHashCodeGeneratorStrategyTest {

  DefaultSecureHashCodeGeneratorStrategy defaultSecureHashCodeGeneratorStrategy;
  Map<String, Object> parameters;
  private static final String SECURE_HASHCODE = "qq";

  @Before
  public void setUp() {

    defaultSecureHashCodeGeneratorStrategy = new DefaultSecureHashCodeGeneratorStrategy();

    parameters = new HashMap<>();
    parameters.put("param1", "value1");
    parameters.put("param2", "value2");

  }

  /**
   * Tests {@link DefaultSecureHashCodeGeneratorStrategy#matches(java.util.Map, String)}
   */
  @Test
  public void testMatches() throws Exception {

    assertTrue("hashcode and parameters don't match", defaultSecureHashCodeGeneratorStrategy.matches(parameters, SECURE_HASHCODE));

  }

  /**
   * Tests {@link DefaultSecureHashCodeGeneratorStrategy#generateSecureHashCode(java.util.Map)}
   */
  @Test
  public void testGenerateSecureHashCode() throws Exception {

    String secureHashCode = defaultSecureHashCodeGeneratorStrategy.generateSecureHashCode(parameters);

    assertEquals("generated hashCode doesn't match", SECURE_HASHCODE, secureHashCode);

  }

  @Test
  public void testEncodings() {
    parameters = ImmutableMap.<String, Object>of("param1", "Kompabitilit\u00e4t");

    defaultSecureHashCodeGeneratorStrategy.setEncoding("UTF-8");
    assertEquals("generated checksum", "Yf", defaultSecureHashCodeGeneratorStrategy.generateSecureHashCode(parameters));

    defaultSecureHashCodeGeneratorStrategy.setEncoding("ISO-8859-1");
    assertEquals("generated checksum", "aE", defaultSecureHashCodeGeneratorStrategy.generateSecureHashCode(parameters));
  }

  @Test
  public void testThrowExceptionWhenSettingBadEncoding() {
    try {
      defaultSecureHashCodeGeneratorStrategy.setEncoding("UNKNOWN_ENCODING");
      fail("should have thrown UnsupportedCharsetException");
    } catch (UnsupportedCharsetException e) {
      // expected
    }
  }
}
