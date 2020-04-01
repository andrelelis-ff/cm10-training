package com.coremedia.blueprint.elastic.social.cae.action;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.action.webflow.WebflowActionState;
import com.coremedia.blueprint.cae.handlers.WebflowHandlerBase;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.elastic.social.cae.user.PasswordExpiryPolicy;
import com.coremedia.blueprint.elastic.social.cae.user.UserContext;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_NUMBER;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_SEGMENTS;
import static com.coremedia.blueprint.base.links.UriConstants.Patterns.PATTERN_WORD;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_NAVIGATION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ACTION;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;

/**
 * Handles authentication (login/logout) actions. This handler is currently used for rendering the initial form only.
 * The login logic itself is handled by a Webflow
 * <p>
 * See com.coremedia.blueprint.elastic.social.cae.flows.Login.xml
 * See com.coremedia.blueprint.elastic.social.cae.flows.Logout.xml
 */
@Link
@RequestMapping
public class AuthenticationHandler extends WebflowHandlerBase {

  public static final String LOGIN_ACTION_ID = "com.coremedia.blueprint.es.webflow.Login";
  public static final String PROFILE_ACTION_ID = "com.coremedia.blueprint.es.webflow.UserDetails";
  public static final String REGISTRATION_ACTION_ID = "com.coremedia.blueprint.es.webflow.Registration";

  public static final String EXPIRED_PASSWORD_SETTING_ID = "flow.passwordExpired"; // NOSONAR false positive: Credentials should not be hard-coded

  private static final String URI_PREFIX = "auth";

  /**
   * URI pattern suffix for actions on page resources like "/dynamic/auth/fragment/site/4420/login"
   *                                                        /dynamic/auth/--/site/5320/login
   */
  public static final String URI_PATTERN =
          '/' + PREFIX_DYNAMIC +
                  '/' + URI_PREFIX +
                  "/{" + SEGMENTS_FRAGMENT + ": (" + PATTERN_WORD + "|--)}" +
                  "/{" + SEGMENTS_NAVIGATION + ":" + PATTERN_SEGMENTS + "}" +
                  "/{" + SEGMENT_ID + ":" + PATTERN_NUMBER + "}" +
                  "/{" + SEGMENT_ACTION + "}";

  private static final String FRAGMENT_DEFAULT_VALUE = "--";

  private SettingsService settingsService;
  private PasswordExpiryPolicy passwordExpiryPolicy;
  private LinkFormatter linkFormatter;

  // --- configure --------------------------------------------------
  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  public SettingsService getSettingsService() {
    return settingsService;
  }

  /**
   * Creates a bean that represents the authentication state
   */
  @Substitution(LOGIN_ACTION_ID)
  public AuthenticationState createLoginActionStateBean(CMAction action) {
    return new AuthenticationState(action, null, AuthenticationState.class.getName(), null, getSettingsService());
  }

  /**
   * Creates a bean that represents the authentication state
   */
  @Substitution(PROFILE_ACTION_ID)
  public Object createProfileActionStateBean(CMAction action) {
    // it's the same bean than for the login action. In fact the profile CMAction holds the logout button and  only
    // a link pointing to the profile. Thus, "login" and "profile" action might be merged.
    return createLoginActionStateBean(action);
  }

  /**
   * Creates a bean that represents the registration state
   */
  @Substitution(REGISTRATION_ACTION_ID)
  public Object createRegistrationActionStateBean(CMAction action) {
    return createLoginActionStateBean(action);
  }

  // --------------- handler --------------

  @Override
  protected WebflowActionState getWebflowActionState(CMAction action, ModelAndView webFlowOutcome, String flowId, String flowViewId) {
    return new AuthenticationState(action, webFlowOutcome.getModelMap(), flowId, flowViewId, getSettingsService());
  }

  // ---------------- links -------------------

  /**
   * Builds a generic action link for an {@link AuthenticationState} form.
   */
  @Link(type = AuthenticationState.class, uri = URI_PATTERN)
  public UriComponents buildLink(AuthenticationState action, UriTemplate uriPattern, Map<String, Object> linkParameters, String view) {
    String actionName = getVanityName(action.getAction());
    Navigation context = getNavigation(action.getAction());
    UriComponentsBuilder result = UriComponentsBuilder.fromPath(uriPattern.toString());
    result = addLinkParametersAsQueryParameters(result, linkParameters);
    return result.buildAndExpand(ImmutableMap.of(
            SEGMENTS_FRAGMENT, view == null ? FRAGMENT_DEFAULT_VALUE : view,
            SEGMENTS_NAVIGATION, joinPath(getPathSegments(context)),
            SEGMENT_ID, getId(action.getAction()),
            SEGMENT_ACTION, actionName
    ));
  }

  @SuppressWarnings("squid:S3752") // multiple request methods allowed by intention as part of the fix for CMS-13646
  @RequestMapping(value = URI_PATTERN, method = {RequestMethod.GET, RequestMethod.POST})
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ID) CMAction action,
                                    @PathVariable(SEGMENTS_NAVIGATION) List<String> navigationPath,
                                    @PathVariable(SEGMENTS_FRAGMENT) String fragment,
                                    @PathVariable(SEGMENT_ACTION) String actionName,
                                    @RequestParam(value = "targetView", required = false) String view,
                                    @RequestParam(value = "webflow", required = false) String webflow,
                                    @NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response) {
    Navigation navigationContext = getNavigation(navigationPath);
    ModelAndView result;
    if (StringUtils.hasText(fragment) && !fragment.equals(FRAGMENT_DEFAULT_VALUE)) {
      CommunityUser user = UserContext.getUser();

      if (user != null && passwordExpiryPolicy.isExpiredFor(user) && !Boolean.valueOf(String.valueOf(webflow))) {
        return forceNewPassword(action, request, response);
      }

      if (navigationContext == null) {
        return HandlerHelper.notFound();
      }
      AuthenticationState bean = createLoginActionStateBean(action);
      // add navigationContext as navigationContext request param
      result = HandlerHelper.createModelWithView(bean, view);
      response.setContentType("text/html");
    } else {
      result = handleRequestInternal(action, navigationContext, actionName, request, response);
    }
    if (result != null) {
      addPageModel(result, asPage(navigationContext, action, UserVariantHelper.getUser(request)));
    }
    return result;
  }

  private ModelAndView forceNewPassword(@NonNull CMAction loginAction, @NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response) {
    response.setContentType("text/plain");
    try (PrintWriter writer = response.getWriter()) {
      // Set standard HTTP/1.1 no-cache headers.
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
      // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
      response.addHeader("Cache-Control", "post-check=0, pre-check=0");
      // Set standard HTTP/1.0 no-cache header.
      response.setHeader("Pragma", "no-cache");
      writer.print("<script>coremedia.blueprint.basic.redirectTo('" + createForceNewPasswordUrl(loginAction, request, response) + "')</script>");
      writer.flush();
    } catch (Exception ex) {
      LOG.error("A user needs to set a new password but we could not redirect him to the corresponding view.", ex);
      return HandlerHelper.forbidden();
    }

    return null;
  }

  private String createForceNewPasswordUrl(@NonNull CMAction loginAction, @NonNull HttpServletRequest request,
                                           @NonNull HttpServletResponse response) {
    CMAction passwordExpiredAction = settingsService.setting(EXPIRED_PASSWORD_SETTING_ID, CMAction.class, loginAction);
    AuthenticationState passwordExpiredState = createLoginActionStateBean(passwordExpiredAction);

    request.setAttribute(ABSOLUTE_URI_KEY, true);
    return linkFormatter.formatLink(passwordExpiredState, null, request, response, true);
  }

  @Required
  public void setPasswordExpiryPolicy(PasswordExpiryPolicy passwordExpiryPolicy) {
    this.passwordExpiryPolicy = passwordExpiryPolicy;
  }

  @Required
  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }
}
