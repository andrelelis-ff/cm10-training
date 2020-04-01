package com.coremedia.blueprint.common.util;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Attributes abstraction from the servlet API.
 * <p>
 * If the methods are invoked from outside a request scope, they return null.
 * <p>
 * <b>WARNING</b>
 * <p>
 * Calculations which (transitively) invoke this class MUST NOT be cached!
 * Usage of the current request makes your feature unpredictable, unstable
 * and hard to test.
 */
@DefaultAnnotation(NonNull.class)
public class ContextAttributes {

  private ContextAttributes() {
  }

  /**
   * Get the request bound to the current thread.
   */
  public static Optional<HttpServletRequest> findRequest() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest);
  }

  /**
   * Get a request attribute of the request bound to the current thread.
   */
  public static <T> Optional<T> findRequestAttribute(String name, Class<T> expectedType) {
    return findRequest()
            .map(request -> request.getAttribute(name))
            .flatMap(value -> typed(value, expectedType));
  }

  /**
   * Get a session attribute of the request bound to the current thread.
   */
  public static <T> Optional<T> findSessionAttribute(String name, Class<T> expectedType) {
    return findRequest()
            .map(request -> request.getSession(false))
            .map(session -> session.getAttribute(name))
            .flatMap(value -> typed(value, expectedType));
  }

  /**
   * Get a request parameter of the request bound to the current thread.
   */
  public static Optional<String> findRequestParameter(String name) {
    return findRequest()
            .map(request -> request.getParameter(name));
  }

  // --- internal ---------------------------------------------------

  @VisibleForTesting
  static <T> Optional<T> typed(Object value, Class<T> expectedType) {
    Class<?> actualType = value.getClass();
    if (expectedType.isAssignableFrom(actualType)) {
      T castedValue = expectedType.cast(value);
      return Optional.ofNullable(castedValue);
    }

    return Optional.empty();
  }
}
