package com.coremedia.livecontext.elastic.social.cae.services.impl;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.elastic.core.api.models.UniqueConstraintViolationException;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserService;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.coremedia.livecontext.services.SessionSynchronizer;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static com.coremedia.common.logging.BaseMarker.PERSONAL_DATA;

/**
 * Implements an UserService to integrate with a commerce system.
 */
public class LiveContextUserSessionSynchronizerImpl implements SessionSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(LiveContextUserSessionSynchronizerImpl.class);

  private CommunityUserService communityUserService;

  @Override
  public void synchronizeUserSession(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response)
          throws GeneralSecurityException {
    CommerceConnection commerceConnection = CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .orElse(null);

    if (commerceConnection != null && isAuthenticatedOnCommerce(commerceConnection)) {
      synchronizeUserContext(commerceConnection);
    }
  }

  private static boolean isAuthenticatedOnCommerce(@NonNull CommerceConnection commerceConnection)
          throws CredentialExpiredException {
    Optional<UserSessionService> userSessionService = commerceConnection.getUserSessionService();
    return userSessionService.isPresent() && userSessionService.get().isLoggedIn();
  }

  private void synchronizeUserContext(@NonNull CommerceConnection commerceConnection) {
    User shopUser = findCurrentUser(commerceConnection).orElse(null);

    if (shopUser == null) {
      String currentUserId = findCurrentUserId().orElse(null);
      LOG.warn(PERSONAL_DATA, "Could not find current user '{}' in shop system", currentUserId);
      return;
    }

    CommunityUser communityUser = getOrCreateCommunityUserWithRetry(shopUser);
    com.coremedia.blueprint.elastic.social.cae.user.UserContext.setUser(communityUser);
  }

  @NonNull
  private static Optional<User> findCurrentUser(@NonNull CommerceConnection commerceConnection) {
    return commerceConnection.getUserService()
            .map(UserService::findCurrentUser);
  }

  @NonNull
  private static Optional<String> findCurrentUserId() {
    return CurrentUserContext.find()
            .map(UserContext::getUserId);
  }

  @NonNull
  private CommunityUser getOrCreateCommunityUserWithRetry(@NonNull User shopUser) {
    try {
      return getOrCreateCommunityUser(shopUser);
    } catch (UniqueConstraintViolationException ex) {
      return getOrCreateCommunityUser(shopUser);
    }
  }

  @NonNull
  private synchronized CommunityUser getOrCreateCommunityUser(@NonNull User shopUser) {
    CommunityUser communityUser = communityUserService.getUserByName(shopUser.getLogonId());

    if (communityUser == null) {
      communityUser = registerCommunityUser(shopUser.getLogonId(), shopUser.getEmail1());
    }

    return communityUser;
  }

  @NonNull
  private CommunityUser registerCommunityUser(String name, String emailAddress) {
    CommunityUser communityUser = communityUserService.createUser(name, null, emailAddress);
    communityUser.setProperty("state", CommunityUser.State.ACTIVATED);
    communityUser.save();
    return communityUser;
  }

  @Required
  public void setCommunityUserService(CommunityUserService communityUserService) {
    this.communityUserService = communityUserService; //NOSONAR - the setter is called before the service is used.
  }
}
