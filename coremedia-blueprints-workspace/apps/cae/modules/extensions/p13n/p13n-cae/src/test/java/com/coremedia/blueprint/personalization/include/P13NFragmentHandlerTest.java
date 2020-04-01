package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.base.links.ContentLinkBuilder;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch;
import com.coremedia.blueprint.personalization.contentbeans.CMSelectionRules;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class P13NFragmentHandlerTest {

  P13NFragmentHandler testling;

  @Mock
  private BeanFactory beanFactory;

  @Mock
  ContentBeanFactory contentBeanFactory;

  @Mock
  ContentRepository contentRepository;

  @Mock
  private ContextHelper contextHelper;

  @Mock
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  @Mock
  private SitesService sitesService;

  @Mock
  private ContentLinkBuilder contentLinkBuilder;

  @Before
  public void setUp() throws Exception {
    testling = new P13NFragmentHandler();
    testling.setBeanFactory(beanFactory);
    testling.setContentBeanFactory(contentBeanFactory);
    testling.setContentRepository(contentRepository);
    testling.setSitesService(sitesService);
    testling.setNavigationSegmentsUriHelper(navigationSegmentsUriHelper);
    testling.setContextHelper(contextHelper);
    testling.setContentLinkBuilder(contentLinkBuilder);

    when(beanFactory.getBean("cmPage", PageImpl.class)).thenReturn(new PageImpl(false, sitesService, Cache.currentCache(), null, null, null));
  }

  @Test
  public void testHandleFragmentRequest() {
    Content p13nContent = mock(Content.class);
    Content cmNavigationContent = mock(Content.class);
    ContentType cmNavigationContentType = mock(ContentType.class);
    CMSelectionRules cmSelectionRules = mock(CMSelectionRules.class);
    CMNavigation cmNavigation = mock(CMNavigation.class);
    when(contentRepository.getContent(anyString())).thenReturn(p13nContent);
    when(contentBeanFactory.createBeanFor(p13nContent, ContentBean.class)).thenReturn(cmSelectionRules);
    configureContext("helios", cmNavigation);

    when(cmNavigation.getContent()).thenReturn(cmNavigationContent);
    when(cmNavigationContent.getType()).thenReturn(cmNavigationContentType);
    when(cmNavigationContentType.getName()).thenReturn(CMSelectionRules.NAME);

    ModelAndView modelAndView = testling.handleFragmentRequest("helios", 4711, "myView", new MockHttpServletRequest());
    assertEquals("myView", modelAndView.getViewName());
    assertTrue(modelAndView.getModel().get("self") instanceof CMSelectionRules);
    assertTrue(modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION) instanceof CMNavigation);
    assertTrue(modelAndView.getModel().get(ContextHelper.ATTR_NAME_PAGE) instanceof Page);
  }

  @Test
  public void testHandleFragmentRequestWrongContent() {
    Content wrongContent = mock(Content.class);
    when(contentRepository.getContent(anyString())).thenReturn(wrongContent);
    CMArticle cmArticle = mock(CMArticle.class);
    when(contentBeanFactory.createBeanFor(wrongContent, ContentBean.class)).thenReturn(cmArticle);

    ModelAndView modelAndView = testling.handleFragmentRequest("helios", 4711, "myView", new MockHttpServletRequest());
    assertEquals(HandlerHelper.notFound().getModel(), modelAndView.getModel());
  }

  @Test
  public void testBuildFragmentLinkForCMSelectionRules() {
    UriTemplate uriTemplate = mock(UriTemplate.class);
    when(uriTemplate.toString()).thenReturn(P13NFragmentHandler.DYNAMIC_URI_PATTERN);

    Content p13nContent = mock(Content.class);
    CMSelectionRules cmSelectionRules = mock(CMSelectionRules.class);
    when(cmSelectionRules.getContentId()).thenReturn(4711);
    when(cmSelectionRules.getContent()).thenReturn(p13nContent);

    configureSegmentPath("helios");

    Map<String, Object> linkParameters = new HashMap<>();
    linkParameters.put("targetView", "myTargetView");

    UriComponents uriComponents = testling.buildFragmentLink(cmSelectionRules, uriTemplate, linkParameters);

    assertEquals("Expected link does not match built link.",
            '/' + PREFIX_DYNAMIC + '/' + SEGMENTS_FRAGMENT + "/p13n/helios/4711", uriComponents.getPath());
  }

  @Test
  public void testBuildFragmentLink() {
    UriTemplate uriTemplate = mock(UriTemplate.class);
    when(uriTemplate.toString()).thenReturn(P13NFragmentHandler.DYNAMIC_URI_PATTERN);

    Content p13nContent = mock(Content.class);
    CMP13NSearch p13nSearch = mock(CMP13NSearch.class);
    when(p13nSearch.getContentId()).thenReturn(4711);
    when(p13nSearch.getContent()).thenReturn(p13nContent);

    configureSegmentPath("helios");

    Map<String, Object> linkParameters = new HashMap<>();
    linkParameters.put("targetView", "myTargetView");

    UriComponents uriComponents = testling.buildFragmentLink(p13nSearch, uriTemplate, linkParameters);

    assertEquals("Expected link does not match built link.",
            '/' + PREFIX_DYNAMIC + '/' + SEGMENTS_FRAGMENT + "/p13n/helios/4711", uriComponents.getPath());

  }

  private void configureContext(String context, Navigation navigation) {
    when(navigationSegmentsUriHelper.parsePath(eq(Collections.singletonList(context)))).thenReturn(navigation);
  }

  private void configureSegmentPath(String context) {
    CMNavigation cmNavigation = mock(CMNavigation.class);
    when(contextHelper.currentSiteContext()).thenReturn(cmNavigation);
    List<String> pathList = new ArrayList<>();
    pathList.add(context);
    when(navigationSegmentsUriHelper.getPathList(cmNavigation)).thenReturn(pathList);
  }
}
