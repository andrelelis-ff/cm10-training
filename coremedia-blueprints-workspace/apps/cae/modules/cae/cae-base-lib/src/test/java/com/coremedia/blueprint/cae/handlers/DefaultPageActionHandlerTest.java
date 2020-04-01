package com.coremedia.blueprint.cae.handlers;


import com.coremedia.blueprint.base.links.ContentLinkBuilder;
import com.coremedia.blueprint.cae.action.webflow.WebflowActionState;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.web.i18n.ResourceBundleInterceptor;
import com.coremedia.cache.Cache;
import com.coremedia.cae.webflow.FlowRunner;
import com.coremedia.cae.webflow.ModelHelper;
import com.coremedia.objectserver.view.substitution.SubstitutionRegistry;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultPageActionHandlerTest extends PageHandlerBaseTest<DefaultPageActionHandler> {
  @Test
  public void handleRequestInternalActionDocDoesNotMatchRequestedAction() {
    when(defaultActionBean.getSegment()).thenReturn("not-the-action");

    ModelAndView result = testling.handleRequestInternal(defaultActionBean, navigationSegmentsUriHelper.parsePath(asList(DEFAULT_CONTEXT)), DEFAULT_ACTION, request, response);

    assertNotFound("Must not be found.", result);
  }

  @Test
  public void handleRequestInternalNoNavigationFound() {
    when(navigationSegmentsUriHelper.parsePath(Arrays.asList(DEFAULT_CONTEXT))).thenReturn(null);

    ModelAndView result = testling.handleRequestInternal(defaultActionBean, navigationSegmentsUriHelper.parsePath(asList(DEFAULT_CONTEXT)), DEFAULT_ACTION, request, response);

    assertNotFound("Must not be found.", result);
  }

  @Test
  public void handleRequestInternal() {
    when(contentLinkBuilder.getVanityName(defaultActionContent)).thenReturn(DEFAULT_ACTION);
    ModelAndView result = testling.handleRequestInternal(defaultActionBean, navigationSegmentsUriHelper.parsePath(asList(DEFAULT_CONTEXT)), DEFAULT_ACTION, request, response);

    assertDefaultPage(result);
  }

  @Test
  public void handleRequestInternalAsWebflowOutcomeIsNull() {
    when(contentLinkBuilder.getVanityName(defaultActionContent)).thenReturn(DEFAULT_ACTION);
    when(defaultActionBean.isWebFlow()).thenReturn(true);
    when(flowRunner.run(eq(DEFAULT_FLOW_ID), any(ModelAndView.class), eq(request), eq(response))).thenReturn(null);

    assertNull(testling.handleRequestInternal(defaultActionBean, navigationSegmentsUriHelper.parsePath(asList(DEFAULT_CONTEXT)), DEFAULT_ACTION, request, response));
    assertEquals(flowRunner, testling.getFlowRunner());
  }

  @Test
  public void handleRequestInternalAsWebFlow() {
    when(contentLinkBuilder.getVanityName(defaultActionContent)).thenReturn(DEFAULT_ACTION);
    when(defaultActionBean.isWebFlow()).thenReturn(true);

    testling.handleRequestInternal(defaultActionBean, navigationSegmentsUriHelper.parsePath(asList(DEFAULT_CONTEXT)), DEFAULT_ACTION, request, response);

    Object substitution = SubstitutionRegistry.getSubstitution(defaultActionBean.getId(), defaultFlowOutCome);
    assertTrue(substitution instanceof WebflowActionState);
    WebflowActionState state = (WebflowActionState)substitution;
    assertEquals(defaultActionBean, state.getAction());
    assertEquals(DEFAULT_FLOW_VIEW_ID, state.getFlowViewId());
    assertEquals(DEFAULT_FLOW_ID, state.getCustomType().getName());
  }

  @Test(expected = IllegalStateException.class)
  public void buildLinkInternalPathListOfProvidedContextIsEmpty() {
    when(navigationSegmentsUriHelper.getPathList(defaultNavigation)).thenReturn(Collections.<String>emptyList());
    testling.buildLinkInternal(defaultActionBean, uriTemplate, Collections.<String, Object>emptyMap());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void builLinkInternalNoExtraParameters() {
    when(contentLinkBuilder.getVanityName(defaultActionContent)).thenReturn(DEFAULT_ACTION);
    UriComponents result = testling.buildLinkInternal(defaultActionBean, uriTemplate, Collections.emptyMap());
    assertDefaultUri(result.toUriString(), Collections.<String, Object>emptyMap());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void builLinkInternalTwoExtraParametersThatAreAllPermitted() {
    when(contentLinkBuilder.getVanityName(defaultActionContent)).thenReturn(DEFAULT_ACTION);
    Map<String, Object> parameters = ImmutableMap.<String, Object>of(
            PARAM_MATTER_TRANSFERENCE_BEAMS, "1",
            PARAM_DOORS, "2"
    );
    UriComponents result = testling.buildLinkInternal(defaultActionBean, uriTemplate, parameters);
    assertDefaultUri(result.toUriString(), parameters);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void builLinkInternalTwoExtraParametersWhereTheFirstIsNotPermitted() {
    when(contentLinkBuilder.getVanityName(defaultActionContent)).thenReturn(DEFAULT_ACTION);
    Map<String, Object> providedParameters = ImmutableMap.<String, Object>of(
            PARAM_BUSINESS_END, "1",
            PARAM_DOORS, "2"
    );
    Map<String, Object> expectedParameters = ImmutableMap.<String, Object>of(
            PARAM_DOORS, "2"
    );
    UriComponents result = testling.buildLinkInternal(defaultActionBean, uriTemplate, providedParameters);
    assertDefaultUri(result.toUriString(), expectedParameters);
  }

  private void assertDefaultUri(String uri, Map<String, Object> expectedParameters) {
    assertTrue(uri.startsWith(DEFAULT_CONTEXT + "/" + DEFAULT_CONTENT_ID + "/" + DEFAULT_ACTION));
    for (Map.Entry<String, Object> entry : expectedParameters.entrySet()) {
      assertTrue(uri.contains(entry.getKey() + "=" + entry.getValue()));
    }
  }

  @Before
  public void defaultSetup() {
    super.defaultSetup();

    defaultFlowOutCome = new ModelAndView();
    Map<String, Object> webflowOutcomeModel = ImmutableMap.<String, Object>of(
            ModelHelper.FLOWVIEWID_NAME, DEFAULT_FLOW_VIEW_ID
    );
    defaultFlowOutCome.addAllObjects(webflowOutcomeModel);
    when(flowRunner.run(eq(DEFAULT_FLOW_ID), any(ModelAndView.class), eq(request), eq(response))).thenReturn(defaultFlowOutCome);
    when(beanFactory.getBean("cmPage", PageImpl.class)).thenReturn(new PageImpl(false, sitesService, Cache.currentCache(), null, null, null));

    testling.setBeanFactory(beanFactory);
    testling.setResourceBundleInterceptor(resourceBundleInterceptor);
    testling.setFlowRunner(flowRunner);
    testling.setContentLinkBuilder(contentLinkBuilder);
  }

  @Override
  protected  DefaultPageActionHandler createTestling() {
    return new DefaultPageActionHandler();
  }

  @Mock
  private BeanFactory beanFactory;

  @Mock
  private ContentLinkBuilder contentLinkBuilder;

  @Mock
  private ResourceBundleInterceptor resourceBundleInterceptor;

  @Mock
  private FlowRunner flowRunner;

  @Mock
  private ModelAndView defaultFlowOutCome;
}
