package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.action.search.SearchActionState;
import com.coremedia.blueprint.cae.action.search.SearchFormBean;
import com.coremedia.blueprint.cae.action.search.SearchService;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.user.User;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

import static com.coremedia.livecontext.fragment.CMSearchFragmentHandler.SEARCH_ACTION_SETTING;
import static com.coremedia.livecontext.fragment.CMSearchFragmentHandler.SEARCH_CHANNEL_SETTING;
import static com.coremedia.objectserver.web.HandlerHelper.VIEWNAME_DEFAULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CMSearchFragmentHandlerTest extends FragmentHandlerTestBase<CMSearchFragmentHandler> {

  @Mock
  private SettingsService settingsService;

  @Mock
  private SearchService searchService;

  @Mock
  private CMAction action;

  private PageImpl page;

  @Override
  protected CMSearchFragmentHandler createTestling() {
    return new CMSearchFragmentHandler(){
      @NonNull
      @Override
      protected Page asPage(Navigation context, Linkable content, User developer) {
        page = new PageImpl(context, content, false, getSitesService(), cache, null, null, null);
        page.setDeveloper(developer);
        return page;
      }
    };
  }

  @Before
  public void defaultSetup() {
    super.defaultSetup();
    getTestling().setSettingsService(settingsService);
    getTestling().setSearchService(searchService);

    when(settingsService.setting(SEARCH_CHANNEL_SETTING, CMChannel.class, getRootChannelBean())).thenReturn(getRootChannelBean());
    when(settingsService.setting(SEARCH_ACTION_SETTING, CMAction.class, getRootChannelBean())).thenReturn(action);

    SearchResultBean resultBean = new SearchResultBean();
    when(searchService.search(same(page), any(SearchFormBean.class), anyCollection())).thenReturn(resultBean);
  }

  @After
  public void defaultTeardown() {
    super.defaultTeardown();
  }

  @Test
  public void testCreateModelAndView() {
    FragmentParameters params = getFragmentParameters();
    params.setView("testView");
    ModelAndView modelAndView = getTestling().createModelAndView(params, request);
    assertNotNull("ModelAndView is not supposed to be null.", modelAndView);
    assertNotNull("Model is not supposed to be null.", modelAndView.getModel());
    assertEquals("Model is no page model.", page, modelAndView.getModel().get("cmpage"));
    assertEquals("View parameter got lost or is different.", params.getView(), modelAndView.getViewName());
    assertEquals("Model with a bean of a wrong type. ", SearchActionState.class, modelAndView.getModel().get("self").getClass());
  }


  @Test
  public void testCreateModelAndDefaultView() {
    FragmentParameters params = getFragmentParameters();
    ModelAndView modelAndView = getTestling().createModelAndView(params, request);
    assertNotNull("ModelAndView is not supposed to be null.", modelAndView);
    assertNotNull("Model is not supposed to be null.", modelAndView.getModel());
    assertEquals("View parameter got lost or is different.", VIEWNAME_DEFAULT, modelAndView.getViewName());
  }

  @Test
  public void testTruePredicate() {
    FragmentParameters  params = getFragmentParameters();
    params.setExternalReference("cm-search");
    assertTrue(getTestling().include(params));
  }

  @Test
  public void testFalsePredicate() {
    FragmentParameters  params = getFragmentParameters();
    params.setExternalReference("cm-search-wrong");
    assertFalse(getTestling().include(params));
  }

  @Test
  public void testNoSite() {
    when(request.getAttribute(SITE_ATTRIBUTE_NAME)).thenReturn(null);

    FragmentParameters  params = getFragmentParameters();
    ModelAndView modelAndView = getTestling().createModelAndView(params, request);
    assertErrorPage(modelAndView, HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void testNoRootDocument() {

    when(contentBeanFactory.createBeanFor(rootChannel, CMChannel.class)).thenReturn(null);

    FragmentParameters  params = getFragmentParameters();
    ModelAndView modelAndView = getTestling().createModelAndView(params, request);
    assertErrorPage(modelAndView, HttpServletResponse.SC_NOT_FOUND);

  }
  private FragmentParameters getFragmentParameters() {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/" + STORE_ID + "/" + LOCALE_STRING + "/params;";
    FragmentParameters  params = FragmentParametersFactory.create(url);
    params.setPlacement("main");
    params.setExternalReference("cm-search");
    return params;
  }
}
