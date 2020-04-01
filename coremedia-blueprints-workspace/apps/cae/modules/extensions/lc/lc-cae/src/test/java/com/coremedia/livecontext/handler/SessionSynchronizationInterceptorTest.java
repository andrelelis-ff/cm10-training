package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.coremedia.livecontext.services.SessionSynchronizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionSynchronizationInterceptorTest {

  private static final String OPTIONS = "OPTIONS";
  private static final String GET = "GET";

  private SessionSynchronizationInterceptor testling;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private SessionSynchronizer sessionSynchronizer;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private StoreContext storeContext;

  @Mock
  private UserSessionService userSessionService;

  @Test
  public void optionsRequest() throws GeneralSecurityException {
    when(request.getMethod()).thenReturn(OPTIONS);
    testling.preHandle(request, response, null);

    verify(request).getMethod();
    verify(sessionSynchronizer, never()).synchronizeUserSession(request, response);
  }

  @Test
  public void getRequest() throws GeneralSecurityException {
    StoreContext storeContext = StoreContextBuilderImpl.from(commerceConnection, "someSiteId").build();

    when(commerceConnection.getUserSessionService()).thenReturn(Optional.of(userSessionService));

    CurrentStoreContext.set(storeContext);

    testling.preHandle(request, response, null);

    verify(request).getMethod();
    verify(sessionSynchronizer).synchronizeUserSession(request, response);
  }

  @Before
  public void defaultSetup() {
    testling = new SessionSynchronizationInterceptor();
    testling.setSessionSynchronizer(sessionSynchronizer);

    when(request.getMethod()).thenReturn(GET);
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }
}
