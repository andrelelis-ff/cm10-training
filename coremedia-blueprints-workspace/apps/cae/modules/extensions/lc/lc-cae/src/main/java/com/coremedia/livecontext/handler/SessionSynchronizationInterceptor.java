package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.coremedia.livecontext.services.SessionSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class SessionSynchronizationInterceptor extends HandlerInterceptorAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(SessionSynchronizationInterceptor.class);

  private SessionSynchronizer sessionSynchronizer;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (RequestMethod.OPTIONS.toString().equals(request.getMethod())) {
      return true;
    }

    Optional<UserSessionService> userSessionService = CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .flatMap(CommerceConnection::getUserSessionService);

    if (userSessionService.isEmpty()) {
      return true;
    }

    try {
      sessionSynchronizer.synchronizeUserSession(request, response);
    } catch (GeneralSecurityException e) {
      LOG.error("Could not synchronize the user sessions between CAE and commerce.", e);
    }

    return true;
  }

  @Required
  public void setSessionSynchronizer(SessionSynchronizer sessionSynchronizer) {
    this.sessionSynchronizer = sessionSynchronizer;
  }
}
