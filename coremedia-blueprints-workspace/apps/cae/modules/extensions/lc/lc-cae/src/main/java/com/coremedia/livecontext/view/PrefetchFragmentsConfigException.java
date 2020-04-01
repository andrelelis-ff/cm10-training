package com.coremedia.livecontext.view;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public class PrefetchFragmentsConfigException extends RuntimeException {

  private final String propertyPath;
  private final Class<?> expectedType;
  private final Object value;

  public PrefetchFragmentsConfigException(String propertyPath, Class<?> expectedType, Object value) {
    this.propertyPath = propertyPath;
    this.expectedType = expectedType;
    this.value = value;
  }

  @Override
  public String getMessage() {
    return String.format("LiveContext Fragment Prefetch Configuration has errors. " +
            "PropertyPath: %s , expectedType: %s , value: %s", propertyPath, expectedType, value);
  }

  public String getPropertyPath() {
    return propertyPath;
  }

  public Class<?> getExpectedType() {
    return expectedType;
  }

  public Object getValue() {
    return value;
  }
}
