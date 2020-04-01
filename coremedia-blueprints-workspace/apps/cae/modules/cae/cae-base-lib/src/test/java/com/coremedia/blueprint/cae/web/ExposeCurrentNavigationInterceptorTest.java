package com.coremedia.blueprint.cae.web;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static com.coremedia.cap.multisite.SiteHelper.SITE_KEY;
import static com.coremedia.blueprint.common.services.context.ContextHelper.ATTR_NAME_PAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExposeCurrentNavigationInterceptorTest {

  @InjectMocks
  private ExposeCurrentNavigationInterceptor testling;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private CMNavigation navigation;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Page page;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Site site;

  private ModelAndView modelAndView;

  @Before
  public void setup() {
    modelAndView = new ModelAndView("test", new HashMap<>());
    when(page.getNavigation()).thenReturn(navigation);
    when(contentBeanFactory.createBeanFor(any(Content.class), eq(Navigation.class))).thenReturn(navigation);
  }

  @Test
  public void withPage() {
    when(request.getAttribute(ATTR_NAME_PAGE)).thenReturn(page);
    testling.postHandle(request, response, null, modelAndView);
    assertThat(NavigationLinkSupport.getNavigation(modelAndView.getModelMap())).isSameAs(navigation);
  }


  @Test
  public void withoutPage() {
    when(request.getAttribute(SITE_KEY)).thenReturn(site);
    testling.postHandle(request, response, null, modelAndView);
    assertThat(NavigationLinkSupport.getNavigation(modelAndView.getModelMap())).isSameAs(navigation);
  }

}
