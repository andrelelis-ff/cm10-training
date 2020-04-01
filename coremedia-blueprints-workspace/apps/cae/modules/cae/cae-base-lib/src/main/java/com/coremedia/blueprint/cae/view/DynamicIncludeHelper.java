package com.coremedia.blueprint.cae.view;

import com.coremedia.objectserver.web.HandlerHelper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;

/**
 * Is used to detect dynamic include loops.
 */
public class DynamicIncludeHelper {
  private static final String ATTR_NAME_DYNAMIC_INCLUDE = "cm_dynamic_include";
  public static final String PLACEMENT_FRAGMENT_ROOT_INDICATOR_VIEW = "_delegate";

  private static final String PATTERN_DYNAMIC = "/" + PREFIX_DYNAMIC + "/";

  /**
   * Checks if the current request is a dynamic include request.
   */
  public static boolean isAlreadyIncludedDynamically() {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (requestAttributes == null) {
      throw new IllegalStateException("Servlet request attributes not available.");
    }
    HttpServletRequest request = requestAttributes.getRequest();
    Object value = request.getAttribute(ATTR_NAME_DYNAMIC_INCLUDE);
    if (value instanceof Boolean) {
      return ((Boolean) value);
    }
    String requestUri = request.getRequestURI();
    boolean isDynamic = requestUri.contains(PATTERN_DYNAMIC);
    request.setAttribute(ATTR_NAME_DYNAMIC_INCLUDE, isDynamic);
    return isDynamic;
  }

  /**
   * Create a wrapper to to bypass the loop/root protection in {@link DynamicIncludeRenderNodeDecoratorProvider}.
   * Needed in case you want to deliver a whole fragment (root) as dynamic include.
   *
   * @param model the model you want to render
   * @param view the view you want to render
   * @return a wrapper which is transparently rendered by the technical template "DynamicInclude._delegate.ftl"
   */
  public static ModelAndView createDynamicIncludeRootDelegateModelAndView(Object model, String view){
    DynamicInclude delegatingModelAndView = new DynamicInclude(model, view);
    return HandlerHelper.createModelWithView(delegatingModelAndView, PLACEMENT_FRAGMENT_ROOT_INDICATOR_VIEW);
  }

}
