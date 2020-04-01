package com.coremedia.blueprint.cae.web.i18n;

import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMHasContexts;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A handler interceptor that makes a {@link Page page's} resource bundle and locale available to the template engine so
 * that <code>&lt;fmt:message&gt;</code>, <code>&lt;spring:message&gt;</code> make use of the bundle.
 */
public class ResourceBundleInterceptor extends HandlerInterceptorAdapter {
  private PageResourceBundleFactory resourceBundleFactory;


  // --- configure --------------------------------------------------

  @Required
  public void setResourceBundleFactory(PageResourceBundleFactory resourceBundleFactory) {
    this.resourceBundleFactory = resourceBundleFactory;
  }


  // --- HandlerInterceptorAdapter ----------------------------------

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    User developer = UserVariantHelper.getUser(request);
    Page page = getPage(modelAndView, request);
    if (page != null) {
      registerResourceBundleForPage(page, developer, request, response);
    } else {
      if (modelAndView != null) {
        Navigation navigation = NavigationLinkSupport.getNavigation(modelAndView.getModelMap());
        if (navigation == null) {
          Object self = HandlerHelper.getRootModel(modelAndView);
          if (self instanceof CMHasContexts) {
            List<CMContext> contexts = ((CMHasContexts) self).getContexts();
            navigation = contexts != null && !contexts.isEmpty() ? contexts.get(0) : null;
          }
        }
        if (navigation != null) {
          registerResourceBundle(resourceBundleFactory.resourceBundle(navigation, developer), navigation.getLocale(), request, response);
        }
      }
    }
  }


  // --- features ---------------------------------------------------

  /**
   * Register the resource bundle configured in the {@link Page Page's}
   * settings for the given request / response.
   * <p>
   * Considers the developer's work in progress resource bundles.
   */
  public void registerResourceBundleForPage(Page page, @Nullable User developer, HttpServletRequest request, HttpServletResponse response) {
    Locale locale = page.getLocale();
    ResourceBundle bundle = resourceBundleFactory.resourceBundle(page, developer);
    registerResourceBundle(bundle, locale, request, response);
  }


  // --- internal ---------------------------------------------------

  private static void registerResourceBundle(ResourceBundle bundle, Locale locale, HttpServletRequest request, HttpServletResponse response) {
    // --- 1.) registering locale to be used by <spring:message> etc.
    // This has been copied from org.springframework.web.servlet.i18n.LocaleChangeInterceptor
    // Note that this works only for some LocaleResolver implementations.
    LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
    if (localeResolver == null) {
      // Is this request not sent by a dispatcher servlet?
      throw new IllegalStateException("No LocaleResolver found");
    }
    localeResolver.setLocale(request, response, locale);

    // --- 2.) registering bundle to be used by <spring:message> etc.
    RequestMessageSource.setMessageSource(new FixedResourceBundleMessageSource(bundle), request);

    // --- 3.) registering bundle/locale to be used by <fmt:message>
    LocalizationContext localizationContext = new LocalizationContext(bundle, locale);
    Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, localizationContext);
  }

  /**
   * Returns the page that is either the self object of the modelAndView
   * or the page from the request attribute.
   *
   * @param modelAndView the {@link org.springframework.web.servlet.ModelAndView}
   * @param request      the {@link javax.servlet.http.HttpServletRequest}
   * @return the page object or <code>null</code> if no page could be found.
   */
  private static Page getPage(ModelAndView modelAndView, HttpServletRequest request) {
    // try to get Page via "self" from ModelAndView
    Object self = modelAndView != null ? HandlerHelper.getRootModel(modelAndView) : null;
    // if self is in in the model or not a page (e.g. direct request to a CMLinkable) still try to get the page
    // from a well-known request attribute
    if (self instanceof Page) {
      return (Page) self;
    } else {
      Page page = null;
      if (modelAndView != null) {
        page = RequestAttributeConstants.getPage(modelAndView);
      }
      if (page==null) {
        // note: the page should be set via RequestAttributeConstants#setPage(ModelAndView) and NOT via
        // RequestAttributeConstants#setPage(HttpServletRequest)
        page = RequestAttributeConstants.getPage(request);
      }
      return page;
    }
  }
}
