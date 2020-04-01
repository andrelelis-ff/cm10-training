package com.coremedia.blueprint.cae.web;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds the current navigation context to the {@link ModelAndView}. Some rendering layer code will expect
 * the navigation as a request attribute. Requires the current page to be already exposed as request
 * attribute RequestAttributeConstants#ATTR_NAME_PAGE.
 */
public class ExposeCurrentNavigationInterceptor extends HandlerInterceptorAdapter {

  private ContentBeanFactory contentBeanFactory;

  @Autowired
  void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                         ModelAndView modelAndView) {
    if (modelAndView == null
            || modelAndView.wasCleared()
            || NavigationLinkSupport.getNavigation(modelAndView.getModel()) != null) {
      return;
    }

    Page page = RequestAttributeConstants.getPage(request);
    if (page != null) {
      NavigationLinkSupport.setNavigation(modelAndView, page.getNavigation());
    } else {
      // this is an error situation - but still, error views may be defined in a theme (which depends on a navigation)
      SiteHelper.findSite(request).ifPresent(siteFromRequest -> {
        Content siteRootDocument = siteFromRequest.getSiteRootDocument();
        Navigation bean = contentBeanFactory.createBeanFor(siteRootDocument, Navigation.class);
        NavigationLinkSupport.setNavigation(modelAndView, bean);
      });
    }
  }
}
