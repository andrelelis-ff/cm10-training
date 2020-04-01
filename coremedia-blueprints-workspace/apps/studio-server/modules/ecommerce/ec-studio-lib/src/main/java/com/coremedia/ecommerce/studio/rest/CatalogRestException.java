package com.coremedia.ecommerce.studio.rest;

import com.coremedia.rest.cap.exception.ParameterizedException;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception to transport a given error code and message to the REST client.
 */
public class CatalogRestException extends ParameterizedException {

  static final String DEFAULT_ERROR_CODE_PREFIX = "LIVECONTEXT_ERROR_";

  private static final Map<String, String> errorNames = new HashMap<>();
  private static final long serialVersionUID = -6956338805631939385L;

  public CatalogRestException(HttpStatus status, String errorCode, String message) {
    super(status, errorCode, getErrorName(errorCode), message);
  }

  /**
   * Returns a human-readable error name of this exception.
   *
   * @param errorCode code to translate
   * @return a human-readable error name of this exception
   */
  public static synchronized String getErrorName(String errorCode) {
    return errorNames.computeIfAbsent(errorCode, k -> fetchErrorName(errorCode));
  }

  private static String fetchErrorName(@NonNull String errorCode) {
    for (Field f : CatalogRestErrorCodes.class.getDeclaredFields()) {
      try {
        if (f.getType() == String.class && Modifier.isStatic(f.getModifiers()) && errorCode.equals(f.get(null))) {
          return f.getName();
        }
      } catch (IllegalAccessException e) {
        // cannot happen as long as CatalogRestErrorCodes fields are public
        throw new IllegalStateException("Cannot access field '" + f + "'. Fields of class '"
                + CatalogRestErrorCodes.class + "' must be public", e);
      }
    }
    return DEFAULT_ERROR_CODE_PREFIX + errorCode;
  }
}
