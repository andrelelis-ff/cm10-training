package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.fragment.pagegrid.PageGridPlacementResolver;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

import static com.coremedia.livecontext.fragment.FragmentHandler.PLACEMENT_NAME_MAV_KEY;
import static com.coremedia.livecontext.fragment.FragmentHandler.UNRESOLVABLE_PLACEMENT_VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FragmentHandlerTest {

  private final static String PLACEMENT = "main";
  private final static String VIEW = "asTeaser";

  private FragmentHandler testling;

  @Mock
  private SitesService sitesService;

  @Mock
  private PageGridPlacementResolver pageGridPlacementResolver;

  @Mock
  private Content rootChannel;

  @Mock
  private ContentType rootChannelType;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private PageGridPlacement placement;

  @Mock
  private ContentBeanBackedPageGridPlacement contentBeanBackedPageGridPlacement;

  @Mock
  private Content placementChannel;

  @Mock
  private ContentType placementChannelType;

  @Mock
  private CMChannel placementChannelBean;

  @Mock
  private CMChannel channelBean;

  @Mock
  private StoreContext storeContext;

  @Mock
  private ValidationService validationService;

  @Mock
  private BeanFactory beanFactory;

  private final Cache cache = new Cache("test");

  @Test
  public void testPlacementFound() {
    when(channelBean.getContext()).thenReturn(channelBean);
    when(pageGridPlacementResolver.resolvePageGridPlacement(channelBean, PLACEMENT)).thenReturn(placement);
    ModelAndView result = testling.createFragmentModelAndViewForPlacementAndView(channelBean, PLACEMENT, VIEW, channelBean, null);
    assertNotNull(result);
  }

  @Test
  public void testInvalidChannel() {
    when(validationService.validate(channelBean)).thenReturn(false);
    ModelAndView result = testling.createFragmentModelAndViewForPlacementAndView(channelBean, PLACEMENT, VIEW, channelBean, null);

    HttpError error = (HttpError) result.getModel().get(HandlerHelper.MODEL_ROOT);
    assertEquals(HttpServletResponse.SC_NOT_FOUND, error.getErrorCode());
  }

  @Test
  public void testNoPlacementFound() {
    when(channelBean.getContext()).thenReturn(channelBean);
    when(pageGridPlacementResolver.resolvePageGridPlacement(channelBean, PLACEMENT)).thenReturn(null);
    ModelAndView result = testling.createFragmentModelAndViewForPlacementAndView(channelBean, PLACEMENT, VIEW, channelBean, null);

    HttpError error = (HttpError) result.getModel().get(HandlerHelper.MODEL_ROOT);
    assertEquals(HttpServletResponse.SC_NOT_FOUND, error.getErrorCode());
    assertEquals(UNRESOLVABLE_PLACEMENT_VIEW_NAME, result.getViewName());
    assertEquals("main", result.getModelMap().get(PLACEMENT_NAME_MAV_KEY));
  }

  @Test
  public void testCreateModelAndViewForPlacementAndView() {
    when(pageGridPlacementResolver.resolvePageGridPlacement(channelBean, PLACEMENT)).
            thenReturn(contentBeanBackedPageGridPlacement);
    when(contentBeanBackedPageGridPlacement.getNavigation()).thenReturn(placementChannelBean);
    ModelAndView modelAndView = testling.createModelAndViewForPlacementAndView(channelBean, PLACEMENT, null, null);
    PageImpl page = (PageImpl) modelAndView.getModel().get("cmpage");
    assertEquals("The specific placement channel is taken for the page", placementChannelBean, page.getContent());
  }

  @Test
  public void testCreateModelAndViewForPlacementAndView_NoPlacementChannelAvailable() {
    when(pageGridPlacementResolver.resolvePageGridPlacement(channelBean, PLACEMENT)).
            thenReturn(contentBeanBackedPageGridPlacement);
    when(contentBeanBackedPageGridPlacement.getNavigation()).thenReturn(null);
    ModelAndView modelAndView = testling.createModelAndViewForPlacementAndView(channelBean, PLACEMENT, null, null);
    PageImpl page = (PageImpl) modelAndView.getModel().get("cmpage");
    assertEquals("The given channel is taken for the page because the placement does not provide a navigation",
            channelBean, page.getContent());
  }

  @Before
  public void setUp() {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    when(storeContext.getConnection()).thenReturn(commerceConnection);

    testling = new ExternalPageFragmentHandler();
    testling.setBeanFactory(beanFactory);
    testling.setPageGridPlacementResolver(pageGridPlacementResolver);
    testling.setContentBeanFactory(contentBeanFactory);
    testling.setSitesService(sitesService);
    testling.setValidationService(validationService);

    CurrentStoreContext.set(storeContext);

    when(beanFactory.getBean("cmPage", PageImpl.class)).thenReturn(new PageImpl(false, sitesService, cache, null, null, null));
    when(validationService.validate(any())).thenReturn(true);

    FragmentContext context = new FragmentContext();
    context.setFragmentRequest(true);
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;";
    FragmentParameters fragmentParameters = FragmentParametersFactory.create(url);
    context.setParameters(fragmentParameters);

    when(channelBean.getContent()).thenReturn(rootChannel);
    when(rootChannel.getType()).thenReturn(rootChannelType);
    when(rootChannelType.getName()).thenReturn("contentTypeName");

    when(placementChannel.getType()).thenReturn(placementChannelType);
    when(placementChannelType.getName()).thenReturn("contentTypeName");
    when(placementChannelBean.getContent()).thenReturn(placementChannel);
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }
}
