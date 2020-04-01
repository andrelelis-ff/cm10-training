package com.coremedia.livecontext.services;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;

/**
 * Synchronizes sessions of systems.
 */
@DefaultAnnotation(NonNull.class)
public interface SessionSynchronizer {

  /**
   * Synchronizes sessions from different systems.
   * In detail this means that an established session on one system should be also established in other systems.
   * If the sessions is not established in one system the user will get logged out in all systems.
   *
   * @param request  the request sent from the client containing informations like cookies to identify session state
   * @param response the response which can be modified for the client.
   */
  void synchronizeUserSession(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException;
}
