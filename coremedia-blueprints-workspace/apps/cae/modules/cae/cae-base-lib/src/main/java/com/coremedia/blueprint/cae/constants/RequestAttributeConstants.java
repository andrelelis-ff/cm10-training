package com.coremedia.blueprint.cae.constants;

import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.objectserver.beans.ContentBean;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

public final class RequestAttributeConstants {

  public static final String ATTR_NAME_PAGE_SITE = "cmpage_site";
  public static final String ATTR_NAME_PAGE_MODEL = "cmpage_model";

  private static final String ATTR_NAME_PAGE = ContextHelper.ATTR_NAME_PAGE;

  /**
   * Hide Utility Class Constructor
   */
  private RequestAttributeConstants() {
  }

  public static void setPage(@NonNull ModelAndView modelAndView, Page page) {
    modelAndView.addObject(ATTR_NAME_PAGE, page);
  }

  public static void setPageModel(ContentBean bean) {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (requestAttributes == null) {
      throw new IllegalStateException("Servlet request attributes are not available, cannot set page model.");
    }

    requestAttributes.setAttribute(ATTR_NAME_PAGE_MODEL, bean, RequestAttributes.SCOPE_REQUEST);
  }

  public static Page getPage(@NonNull HttpServletRequest request) {
    return (Page) request.getAttribute(ATTR_NAME_PAGE);
  }

  public static Page getPage(@NonNull ModelAndView modelAndView) {
    return (Page) modelAndView.getModel().get(ATTR_NAME_PAGE);
  }
}
