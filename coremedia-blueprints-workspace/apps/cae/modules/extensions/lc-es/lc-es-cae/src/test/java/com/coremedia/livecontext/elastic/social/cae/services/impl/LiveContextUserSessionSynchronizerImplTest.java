package com.coremedia.livecontext.elastic.social.cae.services.impl;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserService;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiveContextUserSessionSynchronizerImplTest {

  private LiveContextUserSessionSynchronizerImpl testling;

  @Mock
  private User commerceUser;

  @Mock
  private CommunityUser communityUser;

  @Mock
  private CommunityUserService communityUserService;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private HttpServletResponse httpServletResponse;

  @Mock
  private UserService userService;

  @Mock
  private UserSessionService userSessionService;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private StoreContext storeContext;

  // The default setup simulates a correctly logged in user. That is a user who is logged into commerce as well
  // as into elastic.
  @Before
  public void defaultSetup() {
    testling = spy(new LiveContextUserSessionSynchronizerImpl());
    testling.setCommunityUserService(communityUserService);

    when(commerceConnection.getUserService()).thenReturn(Optional.of(userService));
    when(commerceConnection.getUserSessionService()).thenReturn(Optional.of(userSessionService));

    when(storeContext.getConnection()).thenReturn(commerceConnection);

    CurrentStoreContext.set(storeContext);
  }

  @After
  public void cleanUp() {
    UserContext.clear();
    CurrentStoreContext.remove();
  }

  @Test
  public void synchronizeUserSessionWithCommerceLogin() throws GeneralSecurityException {
    when(userSessionService.isLoggedIn()).thenReturn(true);

    when(userService.findCurrentUser()).thenReturn(commerceUser);
    when(commerceUser.getLogonId()).thenReturn("shopper");

    when(communityUserService.getUserByName("shopper")).thenReturn(communityUser);

    testling.synchronizeUserSession(httpServletRequest, httpServletResponse);

    verifyIsLoggedIn();
  }

  @Test
  public void synchronizeUserSessionWithCommerceLoginAndUserCreation() throws GeneralSecurityException {
    when(userSessionService.isLoggedIn()).thenReturn(true);
    when(communityUserService.getUserByName("shopper")).thenReturn(null);

    when(commerceUser.getLogonId()).thenReturn("shopper");
    when(commerceUser.getEmail1()).thenReturn("shopper@mail.com");
    when(userService.findCurrentUser()).thenReturn(commerceUser);

    when(communityUserService.createUser("shopper", null, "shopper@mail.com")).thenReturn(communityUser);

    testling.synchronizeUserSession(httpServletRequest, httpServletResponse);

    verify(communityUserService).createUser("shopper", null, "shopper@mail.com");
    assertEquals(UserContext.getUser(), communityUser);

    verifyIsLoggedIn();
  }

  @Test
  public void synchronizeUserSessionNoCommerceLogin() throws GeneralSecurityException {
    when(userSessionService.isLoggedIn()).thenReturn(false);
    testling.synchronizeUserSession(httpServletRequest, httpServletResponse);

    verifyNoUserContext();
  }

  private void verifyIsLoggedIn() throws GeneralSecurityException {
    verify(userSessionService).isLoggedIn();
    assertNotNull(UserContext.getUser());
  }

  private void verifyNoUserContext() {
    assertNull(UserContext.getUser());
  }
}
