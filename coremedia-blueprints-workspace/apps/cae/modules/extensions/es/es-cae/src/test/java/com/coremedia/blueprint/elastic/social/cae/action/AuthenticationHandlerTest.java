package com.coremedia.blueprint.elastic.social.cae.action;

import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.coremedia.blueprint.cae.web.links.NavigationLinkSupport.ATTR_NAME_CMNAVIGATION;
import static com.coremedia.objectserver.view.ViewUtils.DEFAULT_VIEW;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = AuthenticationHandlerTestConfiguration.class)
public class AuthenticationHandlerTest {

  private AuthenticationState authenticationState;
  private CMAction action;

  @Inject
  private MockMvc mockMvc;
  @Inject
  private LinkFormatter linkFormatter;
  @Inject
  private ContentTestHelper contentTestHelper;

  @Before
  public void setUp() {
    authenticationState = mock(AuthenticationState.class);
  }

  @After
  public void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testShowPageForUnknownActionWhereTheActionNameEqualsTheVanityName() throws Exception {
    setAction(22);
    assertModelWithPageBean(handleRequest("/dynamic/auth/--/root/22/22"), contentTestHelper.getContentBean(4), action);
  }

  @Test
  public void testNotFoundForUnknownActionWithSegmentMismatch() throws Exception {
    setAction(22);
    ModelAndView modelAndView = handleRequest("/dynamic/auth/--/root/22/unknown");

    assertThat(modelAndView)
            .extracting(HandlerHelper::getRootModel)
            .isInstanceOfSatisfying(HttpError.class,
                    e -> assertThat(e)
                            .returns(HttpServletResponse.SC_NOT_FOUND, from(HttpError::getErrorCode)));
  }

  @Test
  public void testGenerateActionLink() {
    setAction(24);
    assertThat(formatLink(authenticationState, ImmutableMap.of("action", "login")))
            .isEqualTo("/dynamic/auth/--/root/24/login");
  }

  @Test
  public void testGenerateGenericActionLink() {
    setAction(24);
    assertThat(formatLink(authenticationState, emptyMap()))
            .isEqualTo("/dynamic/auth/--/root/24/login");
  }

  private ModelAndView handleRequest(String path) throws Exception {
    MockHttpServletRequestBuilder req = MockMvcRequestBuilders
            .get(path)
            .requestAttr(ATTR_NAME_CMNAVIGATION, contentTestHelper.getContentBean(4))
            .characterEncoding("UTF-8");
    return mockMvc.perform(req).andReturn().getModelAndView();
  }

  /*
   * common assertions after controller parses link into model for controllers that generate pages
   */
  private void assertModelWithPageBean(ModelAndView modelAndView, CMNavigation navigation, CMLinkable content) {
    assertThat(modelAndView)
            .isNotNull()
            .extracting(ModelAndView::getViewName)
            .isEqualTo(DEFAULT_VIEW);

    assertThat(modelAndView)
            .extracting(HandlerHelper::getRootModel)
            .isInstanceOfSatisfying(Page.class,
                    p -> assertThat(p)
                            .returns(content, from(Page::getContent))
                            .returns(navigation, from(Page::getNavigation)));
  }

  private String formatLink(Object bean, Map<String, Object> parameters) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
    request.setCharacterEncoding("UTF-8");
    request.setAttribute(ViewUtils.PARAMETERS, parameters);
    return linkFormatter.formatLink(bean, null, request, new MockHttpServletResponse(), false);
  }

  private void setAction(int id) {
    action = contentTestHelper.getContentBean(id);
    when(authenticationState.getAction()).thenReturn(action);
  }

}
