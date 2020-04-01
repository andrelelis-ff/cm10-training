package com.coremedia.blueprint.cae.exception.handler;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.exception.InvalidContentException;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.objectserver.view.ViewException;
import com.coremedia.objectserver.web.HandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class InvalidContentExceptionHandler extends AbstractErrorAndExceptionHandler<InvalidContentException, Object> {

  private static final Logger LOG = LoggerFactory.getLogger(InvalidContentExceptionHandler.class);

  @Override
  public void handleExceptionInternal(InvalidContentException exception, ModelAndView modelAndView, String viewName, HttpServletRequest request) {
    Object self = HandlerHelper.getRootModel(modelAndView);
    if (self instanceof Page) {
      Page page = (Page) self;
      RequestAttributeConstants.setPage(modelAndView, page);
      NavigationLinkSupport.setNavigation(modelAndView, page.getNavigation());
    }
    modelAndView.addObject("viewException", new ViewException(null, exception, self, null, null));
    if (!viewName.isEmpty()) {
      HandlerHelper.setViewName(modelAndView, viewName);
    }
    LOG.info("The following content is not valid: {}", exception.getMessage());
  }

  @Override
  public int getStatusCode() {
    return HttpServletResponse.SC_NOT_FOUND;
  }

  @Override
  public Object resolveSelf(InvalidContentException exception) {
    return exception.getInvalidContent();
  }

  @Override
  public InvalidContentException resolveException(Exception exception) {
    return exception instanceof InvalidContentException ? (InvalidContentException) exception : null;
  }
}
