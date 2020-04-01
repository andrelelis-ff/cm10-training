package com.coremedia.blueprint.elastic.social.cae.user;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.elastic.social.springsecurity.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class UserFilter implements Filter {

  @Inject
  private CommunityUserService communityUserService;

  @Inject
  private ElasticSocialPlugin elasticSocialPlugin;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to do.
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;

      CommunityUser communityUser = getLoggedInUser();
      if (communityUser != null) {
        boolean mayLogin = communityUser.isActivated()
                || communityUser.isActivatedAndRequiresModeration()
                || communityUser.isIgnored();

        if (mayLogin && isFeedbackEnabled(request)) {
          UserContext.setUser(communityUser);
        } else {
          SecurityContextHolder.clearContext();
          httpServletRequest.logout();
        }
      }
    }

    try {
      chain.doFilter(request, response);
    } finally {
      UserContext.clear();
    }
  }

  private boolean isFeedbackEnabled(@NonNull ServletRequest request) {
    return SiteHelper.findSite(request)
            .map(elasticSocialPlugin::getElasticSocialConfiguration)
            .map(ElasticSocialConfiguration::isFeedbackEnabled)
            .orElse(true);
  }

  @Override
  public void destroy() {
    // Nothing to do.
  }

  @Nullable
  public CommunityUser getLoggedInUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || authentication.getName().equals("anonymousUser")) {
      return null;
    }

    return getUser(authentication.getPrincipal());
  }

  @Nullable
  public CommunityUser getUser(@PersonalData Object principal) {
    if (principal instanceof String) {
      CommunityUser user = communityUserService.getUserByName((String) principal);
      if (user != null) {
        return user;
      }

      return communityUserService.getUserByEmail((String) principal);
    }

    if (principal instanceof UserPrincipal) {
      UserPrincipal userPrincipal = (UserPrincipal) principal;
      return communityUserService.getUserById(userPrincipal.getUserId());
    }

    return null;
  }
}
