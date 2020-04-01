package com.coremedia.livecontext.p13n.include;

import com.coremedia.blueprint.base.links.ContentLinkBuilder;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
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
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_PLACEMENT;
import static com.coremedia.livecontext.p13n.include.P13NPlacementHandler.DYNAMIC_PLACEMENT_URI_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class P13NPlacementHandlerTest {

  private P13NPlacementHandler testling;

  @Mock
  private BeanFactory beanFactory;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private ContextHelper contextHelper;

  @Mock
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  @Mock
  private SitesService sitesService;

  @Mock
  private ValidationService validationService;

  @Mock
  private ContentLinkBuilder contentLinkBuilder;

  @Mock
  private ContentBeanBackedPageGridPlacement headerPlacement;

  @Mock
  private ContentBeanBackedPageGridPlacement additionalPlacement;

  @Mock
  private PageGrid pageGrid;

  @Mock
  private PageGrid pdpPageGrid;

  @Mock
  private CMNavigation navigation;

  @Mock
  private CMChannel channel;

  @Before
  public void setUp() throws Exception {
    testling = new P13NPlacementHandler();
    testling.setBeanFactory(beanFactory);
    testling.setContentBeanFactory(contentBeanFactory);
    testling.setSitesService(sitesService);
    testling.setValidationService(validationService);
    testling.setNavigationSegmentsUriHelper(navigationSegmentsUriHelper);
    testling.setContextHelper(contextHelper);
    testling.setContentLinkBuilder(contentLinkBuilder);

    when(beanFactory.getBean("cmPage", PageImpl.class)).thenReturn(new PageImpl(false, sitesService, Cache.currentCache(), null, null, null));

    when(headerPlacement.getNavigation()).thenReturn(navigation);
    when(headerPlacement.getStructPropertyName()).thenReturn("pagegrid");
    when(headerPlacement.getName()).thenReturn("header");
    when(pageGrid.getPlacementForName("header")).thenReturn(headerPlacement);

    when(additionalPlacement.getNavigation()).thenReturn(navigation);
    when(pdpPageGrid.getPlacementForName("additional")).thenReturn(additionalPlacement);

    Content navigationContent = mock(Content.class);
    ContentType navigationContentType = mock(ContentType.class);

    when(navigation.getContent()).thenReturn(navigationContent);
    when(navigationContent.getType()).thenReturn(navigationContentType);
    when(navigationContentType.getName()).thenReturn(CMChannel.NAME);
  }

  @Test
  public void testHandleChannelRequest() {
    when(validationService.validate(channel)).thenReturn(true);
    when(channel.getPageGrid()).thenReturn(pageGrid);

    configureContext("helios", navigation);

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    ModelAndView modelAndView = testling.handleRequest("helios", channel, "pagegrid", "header", "myView", mockRequest);

    assertThat(modelAndView.getViewName()).isEqualTo("myView");
    assertThat(modelAndView.getModel().get("self")).isInstanceOf(ContentBeanBackedPageGridPlacement.class);
    assertThat(modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION)).isInstanceOf(CMNavigation.class);
    assertThat(modelAndView.getModel().get(ContextHelper.ATTR_NAME_PAGE)).isInstanceOf(Page.class);
  }

  @Test
  public void testHandlePdpRequest() {
    CMExternalChannel cmContentBean = mock(CMExternalChannel.class);

    when(validationService.validate(cmContentBean)).thenReturn(true);

    when(cmContentBean.getPdpPagegrid()).thenReturn(pdpPageGrid);

    configureContext("helios", navigation);

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    ModelAndView modelAndView = testling.handleRequest("helios", cmContentBean, "pdpPagegrid", "additional", "myView", mockRequest);

    assertThat(modelAndView.getViewName()).isEqualTo("myView");
    assertThat(modelAndView.getModel().get("self")).isInstanceOf(ContentBeanBackedPageGridPlacement.class);
    assertThat(modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION)).isInstanceOf(CMNavigation.class);
    assertThat(modelAndView.getModel().get(ContextHelper.ATTR_NAME_PAGE)).isInstanceOf(Page.class);
  }

  @Test
  public void testHandleFragmentRequestWrongContent() {
    CMArticle cmArticle = mock(CMArticle.class);

    ModelAndView modelAndView = testling.handleRequest("helios", cmArticle, "pagegrid", "header", "myView", new MockHttpServletRequest());
    assertThat(modelAndView.getModel()).isEqualTo(HandlerHelper.notFound().getModel());
  }

  @Test
  public void testBuildLinkForPlacement() {
    UriTemplate uriTemplate = mock(UriTemplate.class);
    when(uriTemplate.toString()).thenReturn(DYNAMIC_PLACEMENT_URI_PATTERN);

    Content channelContent = mock(Content.class);
    when(headerPlacement.getNavigation().getContentId()).thenReturn(4711);

    configureSegmentPath("helios");

    Map<String, Object> linkParameters = new HashMap<>();
    linkParameters.put("targetView", "myTargetView");

    UriComponents uriComponents = testling.buildLink(headerPlacement, uriTemplate, linkParameters);

    assertThat(uriComponents.getPath()).isEqualTo('/' + PREFIX_DYNAMIC + '/' + SEGMENTS_PLACEMENT + "/p13n/helios/4711/pagegrid/header")
            .withFailMessage("Expected link does not match built link.");
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
