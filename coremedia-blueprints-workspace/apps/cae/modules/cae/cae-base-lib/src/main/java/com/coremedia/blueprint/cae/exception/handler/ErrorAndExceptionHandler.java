package com.coremedia.blueprint.cae.exception.handler;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Strategy interface for fine grained exception handling by an
 * {@link com.coremedia.blueprint.cae.exception.resolver.ErrorAndExceptionMappingResolver}.
 */
public interface ErrorAndExceptionHandler {

  ModelAndView handleException(String viewName, Exception ex, HttpServletRequest request, HttpServletResponse response);

}
