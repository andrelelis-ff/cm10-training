package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserContextProvider;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static java.util.function.Predicate.not;

/**
 * Provides access to the current {@link UserContext}. The handling itself is delegated to the {@link UserContextHelper}.
 */
@DefaultAnnotation(NonNull.class)
public class UserContextProviderImpl implements UserContextProvider {

  private static final String STUDIO_PREVIEW_TEST_PARAM = "p13n_test";

  @Nullable
  private UserSessionService userSessionService;

  @Override
  public UserContext createContext(HttpServletRequest request) {
    UserContext.Builder builder = UserContext.builder();

    findUserId(request).ifPresent(builder::withUserId);
    findCookieHeader(request).ifPresent(builder::withCookieHeader);

    return builder.build();
  }

  private Optional<String> findUserId(HttpServletRequest request) {
    if (userSessionService == null) {
      return Optional.empty();
    }

    return CurrentStoreContext.find()
            .map(StoreContext::getStoreId)
            .filter(not(Strings::isNullOrEmpty))
            .map(storeId -> userSessionService.resolveUserId(request, storeId, false));
  }

  private static Optional<String> findCookieHeader(HttpServletRequest request) {
    if (isStudioPreviewRequest(request)) {
      return Optional.empty();
    }

    String header = request.getHeader("Cookie");
    String rewrittenHeader = WcCookieHelper.rewritePreviewCookies(header);
    return Optional.ofNullable(rewrittenHeader);
  }

  private static boolean isStudioPreviewRequest(HttpServletRequest request) {
    return "true".equals(request.getParameter(STUDIO_PREVIEW_TEST_PARAM));
  }

  // ------------ Config -----------------------

  @Autowired(required = false)
  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }
}
