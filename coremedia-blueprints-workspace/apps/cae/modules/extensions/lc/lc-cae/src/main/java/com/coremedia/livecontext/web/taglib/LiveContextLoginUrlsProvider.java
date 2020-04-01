package com.coremedia.livecontext.web.taglib;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides URLs to Commerce Login Form and logout handlers.
 */
public interface LiveContextLoginUrlsProvider {

  /**
   * Returns the URL for the login form of the Commerce system.
   *
   * @return login form URL
   * @param request the current request
   */
  @NonNull
  String buildLoginFormUrl(@NonNull HttpServletRequest request);

  /**
   * Returns the URL to log out of the Commerce system.
   *
   * @return logout URL
   * @param request the current request
   */
  @NonNull
  String buildLogoutUrl(@NonNull HttpServletRequest request);

  /**
   *  Transforms the given URL of the {@link com.coremedia.livecontext.handler.LoginStatusHandler}
   *  for the use with the Commerce system.
   *
   *  <p>For example, the implementation could add required query parameters.
   *
   * @param url login status handler URL
   * @param request the current request
   * @return transformed URL
   */
  @NonNull
  String transformLoginStatusUrl(@NonNull String url, @NonNull HttpServletRequest request);

}
