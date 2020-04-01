package com.coremedia.blueprint.cae.web;

import com.coremedia.blueprint.cae.exception.InvalidContentException;
import com.coremedia.blueprint.common.contentbeans.CMHasContexts;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.objectserver.web.HandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class ContentValidityInterceptor extends HandlerInterceptorAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(ContentValidityInterceptor.class);

  private ValidationService<Object> validationService;

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    if (modelAndView != null) {
      Object self = HandlerHelper.getRootModel(modelAndView);
      if (self instanceof Page) {
        Page page = (Page) self;

        boolean contentValidity = validationService.validate(page.getContent());
        boolean pageValidity = contentValidity && validationService.validate(page.getNavigation());

        if (!pageValidity) {
          final String msg = "Trying to render invalid page, returning " + SC_NOT_FOUND + ".  Page=" + page;
          LOG.debug(msg);
          throw new InvalidContentException(msg, page);
        }
      }
      else if (self instanceof CMHasContexts) {
        if (!validationService.validate(self)) {
          final String msg = "Trying to render invalid content, returning " + SC_NOT_FOUND + ".  CMHasContexts=" + self;
          LOG.debug(msg);
          throw new InvalidContentException(msg, self);
        }
      }
    }
  }

  @Required
  public void setValidationService(ValidationService<Object> validationService) {
    this.validationService = validationService;
  }
}
