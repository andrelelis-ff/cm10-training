package com.coremedia.livecontext.web.taglib;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.web.FreemarkerEnvironment;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.handler.LoginStatusHandler;
import com.coremedia.objectserver.util.RequestServices;
import com.coremedia.objectserver.web.links.LinkFormatter;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides URLs and parameter values for requests to the {@link LoginStatusHandler}.
 */
public class LiveContextLoginFreemarkerFacade {

  /**
   * Builds the url for the status handler to retrieve the actual state (logged in/logged out) of the user.
   *
   * @return an url to the cae handler.
   */
  public String getStatusUrl() {
    String link = buildLink(LoginStatusHandler.LinkType.STATUS);
    HttpServletRequest request = FreemarkerEnvironment.getCurrentRequest();
    return getLiveContextLoginUrlsProvider().transformLoginStatusUrl(link, request);
  }

  /**
   * Builds the absolute url to the login formular of a commerce system.
   *
   * @return absolute url to a formular of a commerce system.
   */
  public String getLoginFormUrl() {
    LiveContextLoginUrlsProvider provider = getLiveContextLoginUrlsProvider();
    HttpServletRequest request = FreemarkerEnvironment.getCurrentRequest();
    return provider.buildLoginFormUrl(request);
  }

  /**
   * Builds a logout url of a commerce system to logout the current user.
   *
   * @return absolute url to logout the current user.
   */
  public String getLogoutUrl() {
    HttpServletRequest request = FreemarkerEnvironment.getCurrentRequest();
    return getLiveContextLoginUrlsProvider().buildLogoutUrl(request);
  }

  @NonNull
  private static LiveContextLoginUrlsProvider getLiveContextLoginUrlsProvider() {
    CommerceConnection connection = CurrentStoreContext.get().getConnection();

    return connection.getServiceForVendor(LiveContextLoginUrlsProvider.class)
            .orElseThrow(() ->
                    new IllegalStateException("No LiveContextLoginUrlsProvider configured for " + connection + ".")
            );
  }

  private static String buildLink(LoginStatusHandler.LinkType bean) {
    HttpServletRequest request = FreemarkerEnvironment.getCurrentRequest();
    LinkFormatter linkFormatter = (LinkFormatter) request.getAttribute(RequestServices.LINK_FORMATTER);
    return linkFormatter.formatLink(bean, null, request, FreemarkerEnvironment.getCurrentResponse(), false);
  }
}
