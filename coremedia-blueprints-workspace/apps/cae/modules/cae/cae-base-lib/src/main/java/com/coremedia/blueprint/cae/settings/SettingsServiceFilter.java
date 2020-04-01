package com.coremedia.blueprint.cae.settings;

import com.coremedia.blueprint.base.settings.SettingsService;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Puts the SettingsService into the HttpRequest in order to make it
 * available in templates.
 */
public class SettingsServiceFilter implements Filter {
  /**
   * The attribute key of the SettingsService in the HttpRequest.
   * <p>
   * The value is "settingsService".
   */
  public static final String SETTINGS_SERVICE_KEY = "settingsService";

  private SettingsService settingsService;


  // --- configure --------------------------------------------------

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }


  // --- Filter -----------------------------------------------------

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    servletRequest.setAttribute(SETTINGS_SERVICE_KEY, settingsService);
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to do
  }

  @Override
  public void destroy() {
    // Nothing to do
  }
}
