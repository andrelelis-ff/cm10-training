package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBeanIdConverter;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.cae.web.links.NavigationLinkSupport.ATTR_NAME_CMNAVIGATION;
import static com.coremedia.blueprint.common.services.context.ContextHelper.ATTR_NAME_PAGE;
import static com.coremedia.objectserver.web.HandlerHelper.MODEL_ROOT;
import static com.coremedia.objectserver.web.HandlerHelper.VIEWNAME_DEFAULT;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public abstract class PageHandlerBaseTest<T extends PageHandlerBase> {
  protected abstract T createTestling();

  protected void defaultSetup() {
    testling = createTestling();
    testling.setNavigationSegmentsUriHelper(navigationSegmentsUriHelper);
    testling.setContextHelper(contextHelper);
    testling.setContentBeanIdConverter(contentBeanIdConverter);
    testling.setPermittedLinkParameterNames(PERMITTED_LINK_PARAMETER_NAMES);
    testling.setSitesService(sitesService);

    defaultContexts = ImmutableList.of(defaultNavigation);

    when(defaultActionBean.getSegment()).thenReturn(DEFAULT_ACTION);
    when(navigationSegmentsUriHelper.parsePath(asList(DEFAULT_CONTEXT))).thenReturn(defaultNavigation);
    when(navigationSegmentsUriHelper.getPathList(defaultNavigation)).thenReturn(DEFAULT_PATH_LIST);
    when(defaultActionBean.getContentId()).thenReturn(DEFAULT_CONTENT_ID);
    when(defaultActionBean.getTitle()).thenReturn(DEFAULT_TITLE);
    when(defaultActionBean.getKeywords()).thenReturn(DEFAULT_KEYWORDS);
    when(defaultActionBean.getValidFrom()).thenReturn(DEFAULT_VALID_FROM);
    when(defaultActionBean.getValidTo()).thenReturn(DEFAULT_VALID_TO);
    when(defaultActionBean.getContent()).thenReturn(defaultActionContent);
    when(defaultActionBean.getId()).thenReturn(DEFAULT_FLOW_ID);
    doReturn(defaultContexts).when(defaultActionBean).getContexts();
    when(defaultActionContent.getType()).thenReturn(defaultActionContentType);
    when(defaultActionContentType.getName()).thenReturn(DEFAULT_ACTION_CONTENT_TYPE);
    when(contextHelper.contextFor(defaultActionBean)).thenReturn(defaultNavigation);
    when(uriTemplate.toString()).thenReturn(DEFAULT_URI_TEMPLATE);
    when(contentBeanIdConverter.convert(defaultActionBean)).thenReturn(Integer.toString(DEFAULT_CONTENT_ID));
    when(defaultNavigation.getContent()).thenReturn(defaultNavigationContent);
    when(defaultNavigationContent.getType()).thenReturn(defaultNavigationContentType);
    when(defaultNavigationContentType.getName()).thenReturn(DEFAULT_NAVIGATION_CONTENT_TYPE_NAME);
  }

  protected void assertDefaultPage(ModelAndView result) {
    Map<String, Object> model = result.getModel();
    Page page = (Page)model.get(MODEL_ROOT);
    assertEquals(VIEWNAME_DEFAULT, result.getViewName());
    assertEquals(page, model.get(ATTR_NAME_PAGE));
    assertEquals(defaultNavigation, model.get(ATTR_NAME_CMNAVIGATION));
    assertEquals(defaultNavigation, page.getNavigation());
    assertEquals(defaultActionBean, page.getContent());
    assertEquals(Integer.toString(DEFAULT_CONTENT_ID), page.getContentId());
    assertEquals(DEFAULT_TITLE, page.getTitle());
    assertEquals(DEFAULT_KEYWORDS, page.getKeywords());
    assertEquals(DEFAULT_VALID_FROM, page.getValidFrom());
    assertEquals(DEFAULT_VALID_TO, page.getValidTo());
    assertEquals(DEFAULT_ACTION_CONTENT_TYPE, page.getContentType());
  }

  protected void assertNavigationPage(ModelAndView result) {
    Map<String, Object> model = result.getModel();
    Page page = (Page)model.get(MODEL_ROOT);
    assertEquals(VIEWNAME_DEFAULT, result.getViewName());
    assertEquals(page, model.get(ATTR_NAME_PAGE));
    assertEquals(defaultNavigation, model.get(ATTR_NAME_CMNAVIGATION));
    assertEquals(defaultNavigation, page.getNavigation());
    assertEquals(defaultNavigation, page.getContent());
    assertEquals("0", page.getContentId());
    assertEquals("No Page Title", page.getTitle());
    assertEquals(null, page.getKeywords());
    assertEquals(null, page.getValidFrom());
    assertEquals(null, page.getValidTo());
    assertEquals(DEFAULT_NAVIGATION_CONTENT_TYPE_NAME, page.getContentType());
  }

  protected void assertNotFound(String message, ModelAndView modelAndView) {
    assertHttpError(message, HttpServletResponse.SC_NOT_FOUND, modelAndView);
  }

  protected void assertHttpError(String message, int expectedErrorCode, ModelAndView modelAndView) {
    assertEquals(String.format("Unexpected root model (%s):", message), HttpError.class, HandlerHelper.getRootModel(modelAndView).getClass());
    assertEquals(String.format("Unexpected error code (%s):", message), expectedErrorCode, ((HttpError) HandlerHelper.getRootModel(modelAndView)).getErrorCode());
  }

  protected T testling;

  @Mock
  protected Content defaultActionContent;

  @Mock
  protected CMAction defaultActionBean;

  @Mock
  protected NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  @Mock
  protected CMChannel defaultNavigation;

  @Mock
  protected Content defaultNavigationContent;

  @Mock
  protected ContentType defaultNavigationContentType;

  @Mock
  protected ContentType defaultActionContentType;

  @Mock
  protected HttpServletRequest request;

  @Mock
  protected HttpServletResponse response;

  @Mock
  protected UriTemplate uriTemplate;

  @Mock
  protected ContextHelper contextHelper;

  @Mock
  protected ContentBeanIdConverter contentBeanIdConverter;

  @Mock
  protected CMContext defaultContext;

  @Mock
  protected SitesService sitesService;

  protected List<? extends CMContext> defaultContexts;

  protected static final String DEFAULT_ACTION = "Vogon Constructor flagship";
  protected static final String DEFAULT_CONTEXT = "The Hitchhiker's Guide to the Galaxy";
  protected static final int DEFAULT_CONTENT_ID = 42;
  protected static final String DEFAULT_CONTENT_ID_STR = Integer.toString(DEFAULT_CONTENT_ID);
  protected static final String DEFAULT_TITLE = "Don't panic!";
  protected static final String DEFAULT_KEYWORDS = "Point of View Gun";
  protected static final Calendar DEFAULT_VALID_FROM = Calendar.getInstance();
  protected static final Calendar DEFAULT_VALID_TO = Calendar.getInstance();
  protected static final String DEFAULT_ACTION_CONTENT_TYPE = "Point of View Gun";
  protected static final String DEFAULT_URI_TEMPLATE = "{navigationPath}/{contentId}/{action}";
  protected static final List<String> DEFAULT_PATH_LIST = Arrays.asList(DEFAULT_CONTEXT);
  protected static final String PARAM_DOORS = "Doors";
  protected static final String PARAM_MATTER_TRANSFERENCE_BEAMS = "Matter-transference-beams";
  protected static final String PARAM_BUSINESS_END = "Business-End";
  protected static final List<String> PERMITTED_LINK_PARAMETER_NAMES = Arrays.asList(PARAM_DOORS, PARAM_MATTER_TRANSFERENCE_BEAMS);
  protected static final String DEFAULT_FLOW_ID = "Infinite Improbability Drive";
  protected static final String DEFAULT_FLOW_VIEW_ID = "Total Perspective Vortex";
  protected static final String DEFAULT_NAVIGATION_CONTENT_TYPE_NAME = "Billion Year Bunker";
}
