package com.coremedia.blueprint.elastic.social.cae.user;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.cap.multisite.Site;
import com.coremedia.elastic.social.api.users.CommunityUser;
import com.coremedia.elastic.social.api.users.CommunityUserService;
import com.coremedia.elastic.social.springsecurity.UserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.coremedia.elastic.core.test.Injection.inject;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserFilterTest {
  private String userId = "1234";
  private LoggedInUserFilterChain filterChain;
  private Filter filter;

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private HttpServletResponse response;

  @Mock
  private CommunityUser communityUser;

  @Mock
  private ServletResponse servletResponse;

  @Mock
  private FilterChain mockFilterChain;

  @Mock
  private Authentication authentication;

  @Mock
  private UserPrincipal userPrincipal;

  @Mock
  private CommunityUserService communityUserService;

  @Mock
  private Site site;

  @Mock
  private ElasticSocialPlugin elasticSocialPlugin;
  @Mock
  private ElasticSocialConfiguration elasticSocialConfiguration;

  @Before
  public void setup() {
    when(communityUser.getId()).thenReturn(userId);
    when(servletRequest.getAttribute(SiteHelper.SITE_KEY)).thenReturn(site);
    when(elasticSocialPlugin.getElasticSocialConfiguration(site)).thenReturn(
            elasticSocialConfiguration
    );
    when(elasticSocialConfiguration.isFeedbackEnabled()).thenReturn(true);

    filterChain = new LoggedInUserFilterChain();
    filter = new UserFilter();

    inject(filter, communityUserService);
    inject(filter, elasticSocialPlugin);
  }

  @Test
  public void userLoggedInAndActive() throws ServletException, IOException {
    when(communityUser.isActivated()).thenReturn(true);
    when(authentication.getName()).thenReturn("horst");
    when(authentication.getPrincipal()).thenReturn(userPrincipal);
    when(userPrincipal.getUserId()).thenReturn("4711");
    when(communityUserService.getUserById("4711")).thenReturn(communityUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserContext.clear();

    filter.init(null);
    filter.doFilter(servletRequest, response, filterChain);
    filter.destroy();

    assertTrue(filterChain.isCalled());
  }

  @Test
  public void userLoggedInAndIgnored() throws ServletException, IOException {
    when(communityUser.isActivated()).thenReturn(false);
    when(communityUser.isIgnored()).thenReturn(true);
    when(authentication.getName()).thenReturn("horst");
    when(authentication.getPrincipal()).thenReturn("4711");
    when(communityUserService.getUserByEmail("4711")).thenReturn(communityUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserContext.clear();

    filter.init(null);
    filter.doFilter(servletRequest, response, filterChain);
    filter.destroy();

    assertTrue(filterChain.isCalled());
  }

  @Test
  public void userNotLoggedIn() throws ServletException, IOException {
    NotLoggedInUserFilterChain filterChain = new NotLoggedInUserFilterChain();
    UserContext.clear();

    filter.doFilter(servletRequest, response, filterChain);
    filter.destroy();

    assertTrue(filterChain.isCalled());
  }

  @Test
  public void userLoggedInUserNotFound() throws ServletException, IOException {
    UserContext.clear();

    filter.doFilter(servletRequest, response, mockFilterChain);

    verify(mockFilterChain).doFilter(servletRequest, response);
  }

  @Test
  public void userLoggedInAndBlocked() throws ServletException, IOException {
    String contextPath = "context";
    when(servletRequest.getContextPath()).thenReturn(contextPath);
    when(communityUser.isActivated()).thenReturn(false);
    when(communityUser.isIgnored()).thenReturn(false);
    UserContext.clear();
    when(authentication.getName()).thenReturn("horst");
    when(authentication.getPrincipal()).thenReturn("4711");
    when(communityUserService.getUserByName("4711")).thenReturn(communityUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filter.init(null);
    filter.doFilter(servletRequest, response, mockFilterChain);

    verify(mockFilterChain).doFilter(servletRequest, response);
  }

  @Test
  public void noHttpRequest() throws ServletException, IOException {
    NotLoggedInUserFilterChain filterChain = new NotLoggedInUserFilterChain();
    UserContext.clear();

    Filter filter = new UserFilter();

    filter.init(null);
    filter.doFilter(servletRequest, response, filterChain);
    filter.destroy();

    assertTrue(filterChain.isCalled());
  }

  @Test
  public void noHttpResponse() throws ServletException, IOException {
    NotLoggedInUserFilterChain filterChain = new NotLoggedInUserFilterChain();
    UserContext.clear();

    Filter filter = new UserFilter();

    filter.init(null);
    filter.doFilter(servletRequest, servletResponse, filterChain);
    filter.destroy();

    assertTrue(filterChain.isCalled());
  }

  private class LoggedInUserFilterChain implements FilterChain {
    private boolean called = false;

    public boolean isCalled() {
      return called;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
      assertNotNull(UserContext.getUser());
      Assert.assertEquals(userId, UserContext.getUser().getId());
      called = true;
    }
  }

  private class NotLoggedInUserFilterChain implements FilterChain {
    private boolean called = false;

    public boolean isCalled() {
      return called;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
      assertNull(UserContext.getUser());
      called = true;
    }
  }
}
