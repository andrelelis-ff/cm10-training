package com.coremedia.blueprint.cae.exception.handler;

import com.coremedia.objectserver.web.HandlerHelper;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base implementation of {@link ErrorAndExceptionHandler}.
 * <p>
 * Provides the general flow and leaves some details to be implemented in
 * specific subclasses.
 *
 * @param <T> The concrete exception type
 * @param <S> The type of the model's "self" object
 */
public abstract class AbstractErrorAndExceptionHandler<T extends Exception, S> implements ErrorAndExceptionHandler {

  @Override
  public ModelAndView handleException(String viewName, Exception ex, HttpServletRequest request, HttpServletResponse response) {
    // get typed exception instance
    T exception = resolveException(ex);

    if (exception == null) {
      // not responsible
      return null;
    }

    // set self based on typed object wrapped in exception
    ModelAndView modelAndView = HandlerHelper.createModelWithView(resolveSelf(exception), viewName);

    // custom handling hook
    handleExceptionInternal(exception, modelAndView, viewName, request);

    // response, based on custom response code
    createResponse(response);

    return modelAndView;
  }

  private void createResponse(HttpServletResponse response) {
    final int statusCode = getStatusCode();
    if (statusCode > 0) {
      response.setStatus(statusCode);
    }
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Edge-Control", "no-store");
  }

  public abstract S resolveSelf(T exception);

  public abstract T resolveException(Exception exception);

  public abstract void handleExceptionInternal(T exception, ModelAndView modelAndView, String viewName, HttpServletRequest request);

  public abstract int getStatusCode();
}
