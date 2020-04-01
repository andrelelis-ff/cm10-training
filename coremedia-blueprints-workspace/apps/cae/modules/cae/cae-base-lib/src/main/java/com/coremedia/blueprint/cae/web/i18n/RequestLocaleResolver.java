package com.coremedia.blueprint.cae.web.i18n;

import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * {@link org.springframework.web.servlet.LocaleResolver} implementation that uses a fallback to the specified default locale
 * or the request's accept-header locale, but allows overriding per request with setLocale.
 *
 * <p>This is particularly useful for stateless applications without user sessions and without Cookies.
 *
 * <p>Custom controllers can thus override the user's locale for the current request by calling
 * {@link #setLocale(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Locale)},
 * for example when rendering a page with a specific locale.
 *
 * @see #setDefaultLocale
 * @see #setLocale
 */
public class RequestLocaleResolver implements LocaleResolver {

  /**
   * The name of the request attribute that holds the locale.
   * <p>Only used for overriding the default if the locale has been
   * changed in the course of the current request! Use
   * {@link org.springframework.web.servlet.support.RequestContext#getLocale}
   * to retrieve the current locale in controllers or views.
   * @see org.springframework.web.servlet.support.RequestContext#getLocale
   */
  public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = RequestLocaleResolver.class.getName() + ".LOCALE";

  private Locale defaultLocale;


  /**
   * Set a fixed Locale that this resolver will return if not overridden. If not set, the Accept Header is
   * used. If that is not set, the server's default locale will be used.
   */
  public void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  /**
   * Return the fixed Locale that this resolver will return if not overridden in this request,
   * if any.
   */
  protected Locale getDefaultLocale() {
    return this.defaultLocale;
  }


  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    // Check request for overridden locale.
    Locale locale = (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
    if (locale != null) {
      return locale;
    }

    return determineDefaultLocale(request);
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    // Set request attribute.
    request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
  }

  /**
   * Determine the default locale for the given request,
   * Called if not overridden for the current request.
   * <p>The default implementation returns the specified default locale,
   * if any, else falls back to the request's accept-header locale.
   * @param request the request to resolve the locale for
   * @return the default locale (never <code>null</code>)
   * @see #setDefaultLocale
   * @see javax.servlet.http.HttpServletRequest#getLocale()
   */
  protected Locale determineDefaultLocale(HttpServletRequest request) {
    Locale locale = getDefaultLocale();
    return locale==null ? request.getLocale() : locale;
  }

}
