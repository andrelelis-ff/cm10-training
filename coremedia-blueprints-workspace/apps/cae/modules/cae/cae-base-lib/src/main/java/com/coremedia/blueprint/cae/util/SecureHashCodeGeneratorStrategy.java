package com.coremedia.blueprint.cae.util;

import java.util.Map;

/**
 * Generate and match secure hash codes for string tokens
 */
public interface SecureHashCodeGeneratorStrategy {

  /**
   * @param parameters     a map of parameters compared to secureHashCode
   * @param secureHashCode the code
   * @return true if secureHashCode matches parameters
   */
  boolean matches(Map<String, Object> parameters, String secureHashCode);

  /**
   * @param parameters a map of parameters to generate secHash from
   * @return generated secHash
   */
  String generateSecureHashCode(Map<String, Object> parameters);

}
