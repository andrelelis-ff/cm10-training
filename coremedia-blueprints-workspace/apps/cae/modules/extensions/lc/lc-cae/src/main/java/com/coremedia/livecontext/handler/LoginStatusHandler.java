package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

@Link
@Controller
@DefaultAnnotation(NonNull.class)
public class LoginStatusHandler {

  private static final Logger LOG = LoggerFactory.getLogger(LoginStatusHandler.class);

  private static final String STATUS = '/' + PREFIX_DYNAMIC + "/loginstatus";

  private final LiveContextSiteResolver liveContextSiteResolver;
  private final CommerceConnectionInitializer commerceConnectionInitializer;

  @SuppressWarnings("WeakerAccess") // used in Spring XML
  public LoginStatusHandler(LiveContextSiteResolver liveContextSiteResolver,
                            CommerceConnectionInitializer commerceConnectionInitializer) {
    this.liveContextSiteResolver = requireNonNull(liveContextSiteResolver);
    this.commerceConnectionInitializer = requireNonNull(commerceConnectionInitializer);
  }

  @GetMapping(value = STATUS, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> handleStatus(@RequestParam String storeId,
                                                          @RequestParam Locale locale,
                                                          HttpServletRequest request) {
    CommerceConnection connection = findConnection(storeId, locale).orElse(null);
    if (connection == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    try {
      CurrentStoreContext.set(connection.getStoreContext());
      initUserContext(connection, request);

      boolean loggedIn = CurrentUserContext.get().isLoggedIn();
      //either the new implementation login status can be resolved via the generic client
      // or the legacy implementation will be used.
      Map<String, Object> body = singletonMap("loggedIn", loggedIn || isLoggedIn(connection));
      return new ResponseEntity<>(body, HttpStatus.OK);
    } finally {
      CurrentStoreContext.remove();
    }
  }

  private static boolean isLoggedIn(CommerceConnection connection) {
    Optional<UserSessionService> userSessionService = connection.getUserSessionService();
    if (userSessionService.isEmpty()) {
      return false;
    }

    try {
      return userSessionService.get().isLoggedIn();
    } catch (CredentialExpiredException e) {
      return false;
    }
  }

  private static void initUserContext(CommerceConnection commerceConnection, HttpServletRequest request) {
    try {
      UserContext userContext = commerceConnection.getUserContextProvider().createContext(request);
      CurrentUserContext.set(userContext);
    } catch (CommerceException e) {
      LOG.warn("Error creating commerce user context: {}", e.getMessage(), e);
    }
  }

  private Optional<CommerceConnection> findConnection(String storeId, Locale locale) {
    return liveContextSiteResolver.findSiteFor(storeId, locale)
            .flatMap(commerceConnectionInitializer::findConnectionForSite);
  }

  // --- Link building ----------------------------------------------------------------------

  @Link(type = LinkType.class, uri = STATUS)
  public UriComponents buildLink(LinkType linkType, UriTemplate uriTemplate) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromPath(uriTemplate.toString());
    StoreContext storeContext = CurrentStoreContext.get();
    String storeId = storeContext.getStoreId();
    Locale locale = storeContext.getLocale();
    builder.queryParam("storeId", storeId);
    builder.queryParam("locale", locale.toLanguageTag());
    return builder.build();
  }

  public enum LinkType {
    STATUS
  }
}
