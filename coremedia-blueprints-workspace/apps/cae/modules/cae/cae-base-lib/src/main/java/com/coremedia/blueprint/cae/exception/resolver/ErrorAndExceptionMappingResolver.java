package com.coremedia.blueprint.cae.exception.resolver;

import com.coremedia.blueprint.cae.exception.ExceptionRenderDynamicViewDecorator;
import com.coremedia.blueprint.cae.exception.handler.ErrorAndExceptionHandler;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} that selects a
 * {@link ErrorAndExceptionHandler} and maps an exception to it.
 *
 * @see DispatcherServlet#processHandlerException(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
 */
public class ErrorAndExceptionMappingResolver extends SimpleMappingExceptionResolver {

  private static final Logger LOG = getLogger(lookup().lookupClass());

  private final List<ErrorAndExceptionHandler> errorAndExceptionHandler;

  public ErrorAndExceptionMappingResolver(List<ErrorAndExceptionHandler> errorAndExceptionHandler) {
    this.errorAndExceptionHandler = errorAndExceptionHandler;
  }

  @Override
  public ModelAndView resolveException(@NonNull HttpServletRequest request,
                                       @NonNull HttpServletResponse response,
                                       Object handler,
                                       @NonNull Exception ex) {

    String matchingViewName = determineViewName(ex, request);

    return getModelAndView(matchingViewName, ex, request, response);

  }

  @Override
  protected String determineViewName(@NonNull Exception ex, HttpServletRequest request) {
    if (ex instanceof ExceptionRenderDynamicViewDecorator) {
      ExceptionRenderDynamicViewDecorator exDec = (ExceptionRenderDynamicViewDecorator) ex;
      if (exDec.getView() != null) {
        return exDec.getView();
      }
    }

    return super.determineViewName(ex, request);
  }

  protected ModelAndView getModelAndView(String viewName, Exception ex, HttpServletRequest request, HttpServletResponse response) {
    LOG.debug("{} was thrown, trying to find a corresponding handler...", ex.getClass().getName());

    for (ErrorAndExceptionHandler exhandler : errorAndExceptionHandler) {
      ModelAndView modelAndView = exhandler.handleException(viewName, ex, request, response);
      if (modelAndView != null) {
        LOG.debug("Found handler for {}: {}", ex.getClass().getName(), exhandler);
        return modelAndView;
      }
    }

    LOG.debug("Could not find a handler for {}.", ex.getClass().getName());
    return null;
  }

}
