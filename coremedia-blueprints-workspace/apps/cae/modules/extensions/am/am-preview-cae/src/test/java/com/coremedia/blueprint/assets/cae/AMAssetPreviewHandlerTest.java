package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.util.Optional;

import static com.coremedia.blueprint.assets.cae.AMAssetPreviewHandler.ASSET_FRAGMENT_PREFIX;
import static com.coremedia.blueprint.assets.cae.AMAssetPreviewHandler.STUDIO_PREFERRED_SITE_PARAMETER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AMAssetPreviewHandlerTest {

  @Mock
  private AMAsset asset;

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site;

  @Mock
  private Content siteRootDocument;

  @Mock
  private Content downloadPortalRootDocument;

  @Mock
  private SettingsService settingsService;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private BeanFactory beanFactory;

  @Mock
  private PageImpl pageImpl;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private CMChannel cmChannel;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private CMChannel downloadPortalChannel;

  @Mock
  private Navigation navigation;

  private AMAssetPreviewHandler handler = new AMAssetPreviewHandler();

  private UriTemplate uriTemplate = new UriTemplate("/");

  private MockHttpServletRequest request = new MockHttpServletRequest();

  private String preferredSiteId = "preferredSiteId";

  private String viewName = "testView";

  @Before
  public void setup() {
    handler.setSettingsService(settingsService);
    handler.setContentBeanFactory(contentBeanFactory);
    handler.setSitesService(sitesService);
    handler.setBeanFactory(beanFactory);

    when(sitesService.findSite(preferredSiteId)).thenReturn(Optional.of(site));
    when(beanFactory.getBean("cmPage", PageImpl.class)).thenReturn(pageImpl);
    when(pageImpl.getNavigation()).thenReturn(navigation);
  }

  @Test
  public void buildAssetLink() {
    int contentId = 110;
    request.setParameter(STUDIO_PREFERRED_SITE_PARAMETER, preferredSiteId);
    when(asset.getContentId()).thenReturn(contentId);

    UriComponents uriComponents = handler.buildAssetLink(asset, uriTemplate, viewName, request);

    assertTrue(uriComponents.getQueryParams().containsKey(STUDIO_PREFERRED_SITE_PARAMETER));
    assertEquals(preferredSiteId, uriComponents.getQueryParams().get(STUDIO_PREFERRED_SITE_PARAMETER).get(0));
    assertTrue(uriComponents.getFragment().contains(ASSET_FRAGMENT_PREFIX));
    assertTrue(uriComponents.getFragment().contains(String.valueOf(contentId)));
  }

  @Test
  public void buildAssetLink_emptyViewName() {
    UriComponents uriComponents = handler.buildAssetLink(asset, uriTemplate, null, request);

    assertFalse(uriComponents.getQueryParams().containsKey(UriConstants.RequestParameters.VIEW_PARAMETER));
  }

  @Test
  public void handleAssetRequest_preferredSite() {
    when(site.getSiteRootDocument()).thenReturn(siteRootDocument);
    when(cmChannel.getContent().getType().getName()).thenReturn("type");
    when(contentBeanFactory.createBeanFor(siteRootDocument, CMChannel.class)).thenReturn(cmChannel);
    ModelAndView modelAndView = handler.handleAssetRequest(asset, viewName, preferredSiteId, request);

    assertTrue(modelAndView.getModel().containsKey(HandlerHelper.MODEL_ROOT));
    assertTrue(modelAndView.getModel().containsKey(ContextHelper.ATTR_NAME_PAGE));
    assertTrue(modelAndView.getModel().containsKey(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(asset, modelAndView.getModel().get(HandlerHelper.MODEL_ROOT));
    assertEquals(pageImpl, modelAndView.getModel().get(ContextHelper.ATTR_NAME_PAGE));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(viewName, modelAndView.getViewName());
    verify(pageImpl).setContent(cmChannel);
  }

  @Test
  public void handleAssetRequest_downloadPortalContent() {
    when(site.getSiteRootDocument()).thenReturn(siteRootDocument);
    when(downloadPortalChannel.getContent().getType().getName()).thenReturn("type");
    when(settingsService.nestedSetting(anyList(), eq(Content.class), eq(siteRootDocument))).thenReturn(downloadPortalRootDocument);
    when(contentBeanFactory.createBeanFor(downloadPortalRootDocument, CMChannel.class)).thenReturn(downloadPortalChannel);
    ModelAndView modelAndView = handler.handleAssetRequest(asset, viewName, preferredSiteId, request);

    assertTrue(modelAndView.getModel().containsKey(HandlerHelper.MODEL_ROOT));
    assertTrue(modelAndView.getModel().containsKey(ContextHelper.ATTR_NAME_PAGE));
    assertTrue(modelAndView.getModel().containsKey(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(asset, modelAndView.getModel().get(HandlerHelper.MODEL_ROOT));
    assertEquals(pageImpl, modelAndView.getModel().get(ContextHelper.ATTR_NAME_PAGE));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(viewName, modelAndView.getViewName());
    verify(pageImpl).setContent(downloadPortalChannel);
  }

  @Test
  public void handleAssetRequest_noContent() {
    String viewName = "testView";
    String preferredSiteId = "preferredSiteId";
    ModelAndView modelAndView = handler.handleAssetRequest(asset, viewName, preferredSiteId, request);

    assertTrue(modelAndView.getModel().containsKey(HandlerHelper.MODEL_ROOT));
    assertFalse(modelAndView.getModel().containsKey(ContextHelper.ATTR_NAME_PAGE));
    assertFalse(modelAndView.getModel().containsKey(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(asset, modelAndView.getModel().get(HandlerHelper.MODEL_ROOT));
    assertEquals(viewName, modelAndView.getViewName());
  }
}
