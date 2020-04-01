package com.coremedia.blueprint.cae.exception.handler;

import com.coremedia.objectserver.web.HttpError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple exception handler implementation for plain spring configuration.
 */
public class SimpleExceptionHandler<T extends Exception> extends AbstractErrorAndExceptionHandler<T, HttpError> {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleExceptionHandler.class);
  private int statusCode;
  private Class<T> exceptionType;

  @Override
  public HttpError resolveSelf(T exception) {
    return new HttpError(getStatusCode(), exception.getMessage());
  }

  @Override
  public T resolveException(Exception exception) {
    if (exceptionType.isInstance(exception)) {
      return exceptionType.cast(exception);
    } else {
      return null;
    }
  }

  @Override
  public void handleExceptionInternal(T exception, ModelAndView modelAndView, String viewName, HttpServletRequest request) {
    LOG.debug("Caught Exception: {} for {} with view {}.", exception, modelAndView, viewName);
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * The status code to send if this resolver handles the exception
   */
  @Required
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * The exception type to handle
   */
  @Required
  public void setExceptionType(Class<T> exceptionType) {
    this.exceptionType = exceptionType;
  }

}
