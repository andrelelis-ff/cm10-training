package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.fragment.resolver.ExternalReferenceResolver;
import com.coremedia.livecontext.fragment.resolver.LinkableAndNavigation;
import com.coremedia.objectserver.web.HttpError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static com.coremedia.livecontext.fragment.ExternalRefFragmentHandler.isSubtypeOfCMNavigation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExternalRefFragmentHandlerTest extends FragmentHandlerTestBase<ExternalRefFragmentHandler> {

  @Mock
  private CMChannel channelBean;

  @Test
  public void testWithoutMatchingResolver() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    ModelAndView result = getTestling().createModelAndView(params, mock(HttpServletRequest.class));
    assertErrorPage(result, HttpServletResponse.SC_NOT_FOUND);
    verifyDefault();
  }

  @Test
  public void testResolverReturnsNull() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);

    ModelAndView result = getTestling().createModelAndView(params, request);
    assertErrorPage(result, HttpServletResponse.SC_NOT_FOUND);
    verifyDefault();
  }

  @Test
  public void testResolverReturnsNoLinkable() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(new LinkableAndNavigation(null, null));

    ModelAndView result = getTestling().createModelAndView(params, request);
    assertErrorPage(result, HttpServletResponse.SC_NOT_FOUND);
    verifyDefault();
  }

  @Test
  public void testResolverReturnsNotALinkable() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(linkableAndNavigation.getNavigation()).thenReturn(null);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(linkableAndNavigation);

    ModelAndView result = getTestling().createModelAndView(params, request);
    assertErrorPage(result, HttpServletResponse.SC_NOT_FOUND);
    verifyDefault();
  }

  @Test
  public void testSiteCheckFails() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(linkableAndNavigation);
    when(contentBeanFactory.createBeanFor(linkable, Navigation.class)).thenReturn(getRootChannelBean());
    when(getSitesService().getContentSiteAspect(linkable)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.isPartOf(site)).thenReturn(false);

    ModelAndView modelAndView = getTestling().createModelAndView(params, request);
    assertNotNull(modelAndView);
    HttpError httpError = (HttpError) modelAndView.getModel().get("self");
    assertEquals(400, httpError.getErrorCode());
  }

  @Test
  public void testResolverReturnsLinkable() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(linkableAndNavigation);
    when(contentBeanFactory.createBeanFor(linkable, Navigation.class)).thenReturn(getRootChannelBean());
    when(getSitesService().getContentSiteAspect(linkable)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.isPartOf(site)).thenReturn(true);
    when(contentBeanFactory.createBeanFor(linkable, CMChannel.class)).thenReturn(getRootChannelBean());

    ModelAndView result = getTestling().createModelAndView(params, request);
    assertNotNull(result);
    verifyDefault();
  }

  @Test
  public void testResolverReturnsInvalidLinkable() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(linkableAndNavigation);
    when(contentBeanFactory.createBeanFor(linkable, Navigation.class)).thenReturn(getRootChannelBean());
    when(getSitesService().getContentSiteAspect(linkable)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.isPartOf(site)).thenReturn(true);
    when(contentBeanFactory.createBeanFor(linkable, CMChannel.class)).thenReturn(getRootChannelBean());
    when(validationService.validate(linkable)).thenReturn(false);
    when(getSitesService().isContentInSite(site, navigationDoc)).thenReturn(true);
    when(getSitesService().isContentInSite(site, linkable)).thenReturn(true);

    ModelAndView result = getTestling().createModelAndView(params, request);
    assertErrorPage(result, HttpServletResponse.SC_NOT_FOUND);
    verifyDefault();
  }

  @Test
  public void testIgnorePlacementIfTheLinkableIsDifferentThanTheNavigation() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(linkableAndNavigation);
    when(getSitesService().isContentInSite(site, navigationDoc)).thenReturn(true);
    when(getSitesService().isContentInSite(site, linkable)).thenReturn(true);

    ExternalRefFragmentHandler testlingSpied = Mockito.spy(getTestling());
    testlingSpied.createModelAndView(params, request);
    Mockito.verify(testlingSpied, times(1)).createModelAndViewForLinkable(any(), any(), anyString(), any());
  }

  @Test
  public void testIgnorePlacementIfTheLinkableIsDifferentThanTheNavigationDefaultViewGiven() {
    FragmentParameters params = getFragmentParametersWithExternalRef(EXTERNAL_REF);
    params.setView("default");
    when(contentCapIdExternalReferenceResolver.include(params)).thenReturn(true);
    when(contentCapIdExternalReferenceResolver.resolveExternalRef(params, site)).thenReturn(linkableAndNavigation);
    when(getSitesService().isContentInSite(site, navigationDoc)).thenReturn(true);
    when(getSitesService().isContentInSite(site, linkable)).thenReturn(true);

    ExternalRefFragmentHandler testlingSpied = Mockito.spy(getTestling());
    testlingSpied.createModelAndView(params, request);
    Mockito.verify(testlingSpied, times(1)).createModelAndViewForLinkable(any(), any(), anyString(), any());
  }

  @Test
  public void testDefaultModelAndView() {
    when(channelBean.getContext()).thenReturn(channelBean);
    when(pageGridPlacementResolver.resolvePageGridPlacement(channelBean, PLACEMENT)).thenReturn(null);

    when(contentBeanFactory.createBeanFor(linkable, Linkable.class)).thenReturn(linkableBean);
    when(contentBeanFactory.createBeanFor(rootChannel, Navigation.class)).thenReturn(channelBean);

    ModelAndView result = getTestling().createModelAndViewForLinkable(rootChannel, linkable, VIEW, null);
    assertNotNull(result);
  }

  @Test
  public void testIsSubtypeOfCMNavigation() {
    Content navigationContent = mock(Content.class, RETURNS_DEEP_STUBS);
    when(navigationContent.getType().isSubtypeOf("CMNavigation")).thenReturn(true);
    assertTrue(isSubtypeOfCMNavigation(navigationContent));

    Content nonNavigationContent = mock(Content.class, RETURNS_DEEP_STUBS);
    when(nonNavigationContent.getType().isSubtypeOf("CMNavigation")).thenReturn(false);
    assertFalse(isSubtypeOfCMNavigation(nonNavigationContent));
  }

  @Override
  protected ExternalRefFragmentHandler createTestling() {
    return new ExternalRefFragmentHandler();
  }

  @Before
  public void defaultSetup() {
    super.defaultSetup();

    List<ExternalReferenceResolver> resolvers = new ArrayList<>();
    resolvers.add(contentCapIdExternalReferenceResolver);
    resolvers.add(contentNumericIdWithChannelIdExternalReferenceResolver);
    resolvers.add(contentNumericIdExternalReferenceResolver);
    resolvers.add(contentPathExternalReferenceResolver);

    when(contentBeanFactory.createBeanFor(linkable, Linkable.class)).thenReturn(linkableBean);
    when(linkableBean.getTitle()).thenReturn(TITLE);
    when(linkableBean.getContentId()).thenReturn(CMCONTEXT_ID);
    when(linkableBean.getContent()).thenReturn(linkable);
    when(linkable.getType()).thenReturn(cmContextContentType);

    getTestling().setExternalReferenceResolvers(resolvers);
//    getTestling().setSitesService(getSitesService());
  }

  @After
  public void tearDown() throws Exception {
    defaultTeardown();
  }

  private FragmentParameters getFragmentParametersWithExternalRef(String ref) {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/" + STORE_ID + "/" + LOCALE_STRING + "/params;";
    FragmentParameters  params = FragmentParametersFactory.create(url);
    params.setView(VIEW);
    params.setPlacement(PLACEMENT);
    params.setExternalReference(ref);
    return params;
  }

}
