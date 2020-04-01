package com.coremedia.blueprint.cae.exception.handler;

import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HttpError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles a {@link org.springframework.core.convert.ConversionFailedException} wrapped in a
 * {@link org.springframework.beans.TypeMismatchException}. This exception is thrown when the
 * {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter} finds a match for a
 * request but calling the handler fails because {@link org.springframework.web.bind.annotation.PathVariable path variable}
 * values or {@link org.springframework.web.bind.annotation.RequestParam request parameters} cannot be converted
 * to the expected types.
 */
public class ConversionFailedExceptionHandler extends AbstractErrorAndExceptionHandler<ConversionFailedException, HttpError> {
  private static final Logger LOG = LoggerFactory.getLogger(ConversionFailedException.class);

  @Override
  public ModelAndView handleException(String viewName, Exception ex, HttpServletRequest request, HttpServletResponse response) {
    // ignoring viewName argument
    return super.handleException(ViewUtils.DEFAULT_VIEW, ex, request, response);
  }

  @Override
  public ConversionFailedException resolveException(Exception exception) {
    if (exception instanceof TypeMismatchException && exception.getCause() instanceof ConversionFailedException) {
      return (ConversionFailedException) exception.getCause();
    }

    // cannot handle this exception
    return null;
  }

  @Override
  public HttpError resolveSelf(ConversionFailedException exception) {
    return new HttpError(getStatusCode());
  }

  @Override
  public void handleExceptionInternal(ConversionFailedException exception, ModelAndView modelAndView, String viewName, HttpServletRequest request) {
    LOG.debug("Failed to convert value '{}' to {} for {} (responding with 404, NOT_FOUND)", exception.getValue(), exception.getTargetType(), request.getRequestURI(), exception);
  }

  @Override
  public int getStatusCode() {
    return HttpServletResponse.SC_NOT_FOUND;
  }
}
