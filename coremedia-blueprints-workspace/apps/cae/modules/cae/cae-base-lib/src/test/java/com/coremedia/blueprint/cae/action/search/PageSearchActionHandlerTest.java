package com.coremedia.blueprint.cae.action.search;

import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.handlers.HandlerTestConfiguration;
import com.coremedia.blueprint.cae.handlers.LinkFormatterTestHelper;
import com.coremedia.blueprint.cae.handlers.RequestTestHelper;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestion;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestions;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cache.Cache;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.cae.action.search.PageSearchActionHandler.ACTION_NAME;
import static com.coremedia.blueprint.cae.action.search.PageSearchActionHandler.PARAMETER_QUERY;
import static com.coremedia.blueprint.cae.action.search.PageSearchActionHandler.PARAMETER_ROOT_NAVIGATION_ID;
import static com.coremedia.blueprint.cae.action.search.PageSearchActionHandlerTest.LocalConfig.PROFILE;
import static com.coremedia.blueprint.cae.handlers.HandlerTestUtil.checkError;
import static com.coremedia.blueprint.cae.handlers.HandlerTestUtil.checkPage;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests {@link PageSearchActionHandler}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = PageSearchActionHandlerTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class PageSearchActionHandlerTest {

  @Configuration
  @ImportResource(
          value = {
                  "classpath:/framework/spring/blueprint-page.xml",
                  "classpath:/framework/spring/blueprint-search.xml",
                  "classpath:/framework/spring/errorhandling.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(HandlerTestConfiguration.class)
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "PageSearchActionHandlerTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/blueprint/cae/action/search/pagesearchactionhandler/content.xml";

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }

    @Bean
    public SearchResultBean searchResultBean() {
      return new SearchResultBean();
    }

  }

  private static final Cache TEST_CACHE = new Cache("test");
  private static final String URI = '/'+ PREFIX_SERVICE+"/search/root/22";
  private static final String WRONG_URI = '/'+ PREFIX_SERVICE+"/search/wrongSegment/22";
  private static final String QUERY = "testQuery";
  private static final int ROOT_NAVIGATION_ID = 4;
  private static final int SEARCH_PAGE_RESULT_ID = 24;
  private static final int ACTION_ID = 22;
  private static final ImmutableMap<String, String> AUTOCOMPLETE_PARAMS = ImmutableMap.of(
          PARAMETER_ROOT_NAVIGATION_ID, String.valueOf(ROOT_NAVIGATION_ID),
          PARAMETER_QUERY, QUERY
  );
  private static final String LONG_QUERY = "myLongEnoughQuery";
  private static final String SHORT_QUERY = "a";

  private SearchActionState searchActionState;
  private CMAction action;
  private Page page;
  @Inject
  private SearchResultBean resultBean;

  @Inject
  private ContentTestHelper contentTestHelper;

  @Inject
  private RequestTestHelper requestTestHelper;

  @Inject
  private LinkFormatterTestHelper linkFormatterTestHelper;

  @Inject
  private SitesService sitesService;

  @Inject
  private PageSearchActionHandler testling;

  @MockBean
  private SearchService searchActionService;

  @Before
  public void setUp() throws Exception {
    configureSearchActionService();

    CMChannel navigation = contentTestHelper.getContentBean(ROOT_NAVIGATION_ID);
    action = contentTestHelper.getContentBean(ACTION_ID);
    page = new PageImpl(navigation, action, true, sitesService, TEST_CACHE, null, null, null);

    //create SearchActionState for linkscheme test
    searchActionState = testling.createActionState(action, null);
  }

  private void configureSearchActionService() {
    Suggestion suggestion = new Suggestion("a", "a", 1L);
    Suggestions result = new Suggestions();
    result.addAll(ImmutableList.of(suggestion));

    List<String> searchDocTypes = Collections.singletonList("CMArticle");
    when(searchActionService.search(any(Page.class), any(SearchFormBean.class), eq(searchDocTypes)))
            .thenReturn(resultBean);
    when(searchActionService.getAutocompleteSuggestions(eq(String.valueOf(ROOT_NAVIGATION_ID)), eq(QUERY), eq(searchDocTypes)))
            .thenReturn(result);
  }

  /**
   * Tests {@link PageSearchActionHandler#handleSearchAction} with a query that is long enough.
   * Expects a successful search.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSearch() throws Exception {

    //simulate the HTML search form, add to request
    Map<String,String> parameters = ImmutableMap.of(PARAMETER_QUERY, LONG_QUERY);
    ModelAndView modelAndView = requestTestHelper.request(URI, parameters);

    checkPage(modelAndView, SEARCH_PAGE_RESULT_ID, SEARCH_PAGE_RESULT_ID);

    // check that an action result has been registered
    SearchActionState actionResult = (SearchActionState) modelAndView.getModel().get("substitution." + PageSearchActionHandler.ACTION_ID);
    assertNotNull("form", actionResult.getForm());
    assertEquals("result", resultBean, actionResult.getResult());
    assertEquals("action", action, actionResult.getAction());
  }

  /**
   * Tests {@link PageSearchActionHandler#handleSearchAction} with a short query.
   * Expects an errormessage in the {@link SearchActionState}
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSearchWithShortQuery() throws Exception {

    //simulate the HTML search form, add to request
    Map<String,String> parameters = ImmutableMap.of(PARAMETER_QUERY, SHORT_QUERY);

    ModelAndView modelAndView = requestTestHelper.request(URI, parameters);

    checkPage(modelAndView, SEARCH_PAGE_RESULT_ID, SEARCH_PAGE_RESULT_ID);

    // check that an action result has been registered
    SearchActionState actionResult = (SearchActionState) modelAndView.getModel().get("substitution." + PageSearchActionHandler.ACTION_ID);
    assertNotNull("form", actionResult.getForm());
    assertNull("result", actionResult.getResult());
    assertTrue("query too short", actionResult.isQueryTooShort());
    assertEquals("action", action, actionResult.getAction());
  }

  /**
   * Tests {@link PageSearchActionHandler#handleSearchSuggestionAction(com.coremedia.blueprint.common.contentbeans.CMAction, String, String, String)}
   */
  @Test
  public void testSuggestion() throws Exception {
    //output is written directly to the response, no MaV is returned.
    MvcResult mvcResult = requestTestHelper.requestMvcResult(URI, AUTOCOMPLETE_PARAMS, null, PageSearchActionHandler.CONTENT_TYPE_JSON);
    MockHttpServletResponse response = mvcResult.getResponse();

    String actual = response.getContentAsString();

    assertThat(actual, org.hamcrest.Matchers.allOf(containsString("label"), containsString("value")));
  }

  /**
   * Test "not found" for a non-existent root segment on both handler methods
   * {@link PageSearchActionHandler#handleSearchAction}
   * {@link PageSearchActionHandler#handleSearchSuggestionAction(com.coremedia.blueprint.common.contentbeans.CMAction, String, String, String)}
   */
  @Test
  public void testNotFoundForUnknownRootSegment() throws Exception {

    ModelAndView modelAndView = requestTestHelper.request(WRONG_URI);
    checkError(modelAndView, SC_NOT_FOUND);

    ModelAndView modelAndView2 = requestTestHelper.request(WRONG_URI, AUTOCOMPLETE_PARAMS, null, PageSearchActionHandler.CONTENT_TYPE_JSON);
    checkError(modelAndView2, SC_NOT_FOUND);
  }

  /**
   * Test generation of specific action URL.
   */
  @Test
  public void testGenerateActionLink() {

    Map<String, Object> parameters = ImmutableMap.of("action", ACTION_NAME, "page", page);

    String url = linkFormatterTestHelper.formatLink(parameters, searchActionState);

    assertEquals("wrong uri", URI, url);
  }
}
