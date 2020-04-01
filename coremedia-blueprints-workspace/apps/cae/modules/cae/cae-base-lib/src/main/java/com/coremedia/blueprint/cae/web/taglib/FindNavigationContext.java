package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.navigation.Navigation;

import javax.servlet.ServletRequest;

/**
 * This class defines a static method to find a
 * {@link com.coremedia.blueprint.common.navigation.Navigation navigation context}
 * for a given bean used in JSP Taglib and freemarker templates.
 */
public final class FindNavigationContext {

  // static class
  private FindNavigationContext() {
  }

  /**
   * Find a navigation context for the given {@link com.coremedia.blueprint.common.contentbeans.CMLinkable}
   * using the ContextStrategy set in the given PageContext.
   *
   * @param bean the bean  to find the navigation context for
   * @param request the servlet request
   * @return the navigation context found
   */
  public static Navigation findNavigationContext(Object bean, ServletRequest request) {
    Navigation currentNavigation = getNavigation(request);
    return getContextStrategy(request).findAndSelectContextFor(bean, currentNavigation);
  }

  public static Navigation getNavigation(ServletRequest request) {
    return (Navigation) request.getAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION);
  }

  private static ContextStrategy<Object, Navigation> getContextStrategy(ServletRequest request) {
    // todo broaden broaden ContextStrategy.find|select to Object,Navigation (and un-generify it)!
    return (ContextStrategy<Object, Navigation>) request.getAttribute(ContextStrategy.NAME_CONTEXTSTRATEGY);
  }

}
