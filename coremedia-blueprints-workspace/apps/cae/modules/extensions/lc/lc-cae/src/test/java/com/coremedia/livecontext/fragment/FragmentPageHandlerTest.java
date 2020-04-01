package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FragmentPageHandlerTest {

  private final static String STORE_ID = "10001";
  private final static String SITE_ID = "123456789";
  private final static Locale LOCALE = Locale.CANADA;
  private final static String SITE_ATTRIBUTE_NAME = SiteHelper.class.getName() + "site";

  private FragmentPageHandler testling;
  private FragmentParameters fragmentParameters;

  @Mock
  private BeanFactory beanFactory;

  @Mock
  private StoreContext storeContext;

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site;

  @Mock
  private Content rootChannel;

  @Mock
  private ContentType rootChannelType;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private CMChannel channelBean;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpServletRequest request;

  @Mock
  private CatalogAliasTranslationService catalogAliasTranslationService;

  private final Cache cache = new Cache("test");

  @Before
  public void setUp() {
    testling = new FragmentPageHandler();
    testling.setBeanFactory(beanFactory);
    testling.setFragmentHandlers(new ArrayList<>());
    testling.setContentBeanFactory(contentBeanFactory);
    testling.setSitesService(sitesService);
    testling.setCatalogAliasTranslationService(catalogAliasTranslationService);

    when(storeContext.getSiteId()).thenReturn(SITE_ID);

    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    when(storeContext.getConnection()).thenReturn(commerceConnection);

    CurrentStoreContext.set(storeContext);

    when(sitesService.getSite(SITE_ID)).thenReturn(site);

    when(site.getSiteRootDocument()).thenReturn(rootChannel);
    when(beanFactory.getBean("cmPage", PageImpl.class))
            .thenReturn(new PageImpl(false, sitesService, cache, null, null, null));

    FragmentContext context = new FragmentContext();
    context.setFragmentRequest(true);
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;";
    this.fragmentParameters = FragmentParametersFactory.create(url);
    context.setParameters(this.fragmentParameters);
    when(request.getAttribute(FragmentContextProvider.FRAGMENT_CONTEXT_ATTRIBUTE)).thenReturn(context);

    when(channelBean.getContent()).thenReturn(rootChannel);
    when(rootChannel.getType()).thenReturn(rootChannelType);
    when(rootChannelType.getName()).thenReturn("contentTypeName");
    when(contentBeanFactory.createBeanFor(rootChannel, CMChannel.class)).thenReturn(channelBean);
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }

  @Test
  public void testDefault() {
    ModelAndView result = testling.handleFragment(STORE_ID, LOCALE, request, response);
    assertNotNull(result);
    assertEquals(result.getViewName(), "DEFAULT");
  }

  @Test
  public void testDefaultWithView() {
    fragmentParameters.setView("test");
    ModelAndView result = testling.handleFragment(STORE_ID, LOCALE, request, response);
    assertNotNull(result);
    assertEquals("test", result.getViewName());

    // this one is important because the LinkAbsolutizer needs a site
    // to build absolute css links for fragments
    verify(request).setAttribute(SITE_ATTRIBUTE_NAME, site);
  }

  @Test
  public void noSiteInPreview() {
    testling.setPreview(true);

    CurrentStoreContext.remove();

    ModelAndView result = testling.handleFragment("unknown", LOCALE, request, response);
    assertNotNull(result);
    assertTrue(result.getModel().get(HandlerHelper.MODEL_ROOT) instanceof HttpError);

    HttpError error = (HttpError) result.getModel().get(HandlerHelper.MODEL_ROOT);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, error.getErrorCode());
  }

  @Test
  public void noSiteNoPreview() {
    testling.setPreview(false);
    when(sitesService.getSite(SITE_ID)).thenReturn(null);
    assertNull(testling.handleFragment("unknown", LOCALE, request, response));
  }
}
