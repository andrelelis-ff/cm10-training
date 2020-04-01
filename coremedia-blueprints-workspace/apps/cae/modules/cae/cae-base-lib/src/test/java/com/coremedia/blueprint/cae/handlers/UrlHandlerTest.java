package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.id.IdProvider;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.urlservice.UrlServiceRequestParams;
import com.coremedia.objectserver.urlservice.UrlServiceResponse;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

import static com.coremedia.cap.multisite.SiteHelper.SITE_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UrlHandlerTest {

  public static final String URL = UrlHandler.URI_PATTERN + "?id=coremedia:///cap/content/1234";

  private UrlHandler urlHandler;

  @Mock
  private MockHttpServletRequest request;

  @Mock
  private MockHttpServletResponse response;

  @Mock
  private LinkFormatter linkFormatter;

  @Mock
  private IdProvider idProvider;

  @Mock
  private SitesService sitesService;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private Content content;

  @Mock
  private Content context;

  @Mock
  private Content siteRootDoc;

  @Mock
  private CMChannel siteRootDocChannel;

  @Mock
  private ContentBean contentBean;

  @Mock
  private Site site;

  @Mock
  private CMChannel channel;

  private static final String id = "coremedia:///cap/content/1234";
  private static final String url = "http://coremedia.com/test/";
  private static final String siteId = "abcdefg";
  private static final String siteRootDocId = "coremedia:///cap/content/10";
  private static final String contextId = "coremedia:///cap/content/12";

  private List<UrlServiceRequestParams> idOnly = Lists.newArrayList(UrlServiceRequestParams.create(id, null, null));
  private List<UrlServiceRequestParams> idWithSite = Lists.newArrayList(UrlServiceRequestParams.create(id, siteId, null));
  private List<UrlServiceRequestParams> idWithContext = Lists.newArrayList(UrlServiceRequestParams.create(id, null, contextId));
  private List<UrlServiceRequestParams> idWithSiteAndContext = Lists.newArrayList(UrlServiceRequestParams.create(id, siteId, contextId));

  @Before
  public void setup() {
    urlHandler = new UrlHandler(linkFormatter);
    urlHandler.setSitesService(sitesService);
    urlHandler.setIdProvider(idProvider);
    urlHandler.setContentBeanFactory(contentBeanFactory);
  }

  @Test
  public void testHandleRequest() {
    when(idProvider.parseId(id)).thenReturn(content);
    when(contentBeanFactory.createBeanFor(content, ContentBean.class)).thenReturn(contentBean);
    when(linkFormatter.formatLink(contentBean, null, request, response, false)).thenReturn(url);
    List<UrlServiceResponse> formatResponse = urlHandler.handleId(idOnly, request, response);

    assertTrue(formatResponse.stream().findFirst().isPresent());
    assertEquals(url, formatResponse.get(0).getUrl());
  }

  @Test
  public void testHandleRequest_withSite() {
    when(idProvider.parseId(id)).thenReturn(content);
    when(contentBeanFactory.createBeanFor(content, ContentBean.class)).thenReturn(contentBean);
    when(linkFormatter.formatLink(contentBean, null, request, response, false)).thenReturn(url);
    when(sitesService.findSite(siteId)).thenReturn(Optional.of(site));
    when(site.getSiteRootDocument()).thenReturn(siteRootDoc);
    when(siteRootDoc.getId()).thenReturn(siteRootDocId);
    when(idProvider.parseId(siteRootDocId)).thenReturn(siteRootDoc);
    when(contentBeanFactory.createBeanFor(siteRootDoc, ContentBean.class)).thenReturn(siteRootDocChannel);
    List<UrlServiceResponse> formatResponse = urlHandler.handleId(idWithSite, request, response);

    assertTrue(formatResponse.stream().findFirst().isPresent());
    assertEquals(url, formatResponse.get(0).getUrl());

    verify(request).setAttribute(SITE_KEY, site);
    verify(request).setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, siteRootDocChannel);
  }

  @Test
  public void testHandleRequest_withContext() {
    when(idProvider.parseId(id)).thenReturn(content);
    when(contentBeanFactory.createBeanFor(content, ContentBean.class)).thenReturn(contentBean);
    when(idProvider.parseId(contextId)).thenReturn(context);
    when(contentBeanFactory.createBeanFor(context, ContentBean.class)).thenReturn(channel);
    when(linkFormatter.formatLink(contentBean, null, request, response, false)).thenReturn(url);
    List<UrlServiceResponse> formatResponse = urlHandler.handleId(idWithContext, request, response);

    assertTrue(formatResponse.stream().findFirst().isPresent());
    assertEquals(url, formatResponse.get(0).getUrl());

    verify(request, never()).setAttribute(SITE_KEY, site);
    verify(request).setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, channel);
  }

  @Test
  public void testHandleRequest_withContextAndSite() {
    when(idProvider.parseId(id)).thenReturn(content);
    when(contentBeanFactory.createBeanFor(content, ContentBean.class)).thenReturn(contentBean);
    when(idProvider.parseId(contextId)).thenReturn(context);
    when(contentBeanFactory.createBeanFor(context, ContentBean.class)).thenReturn(channel);
    when(linkFormatter.formatLink(contentBean, null, request, response, false)).thenReturn(url);
    when(sitesService.findSite(siteId)).thenReturn(Optional.of(site));
    when(site.getSiteRootDocument()).thenReturn(siteRootDoc);
    when(siteRootDoc.getId()).thenReturn(siteRootDocId);
    when(idProvider.parseId(siteRootDocId)).thenReturn(siteRootDoc);
    when(contentBeanFactory.createBeanFor(siteRootDoc, ContentBean.class)).thenReturn(siteRootDocChannel);
    List<UrlServiceResponse> formatResponse = urlHandler.handleId(idWithSiteAndContext, request, response);

    assertTrue(formatResponse.stream().findFirst().isPresent());
    assertEquals(url, formatResponse.get(0).getUrl());

    verify(request).setAttribute(SITE_KEY, site);
    verify(request).setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, channel);
    verify(request, never()).setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, siteRootDocChannel);
  }

  @Test
  public void testHandleRequest_notFound() {
    when(idProvider.parseId(id)).thenReturn(null);
    List<UrlServiceResponse> formatResponse = urlHandler.handleId(idWithContext, request, response);

    assertTrue(formatResponse.stream().findFirst().isPresent());
    assertEquals(UrlHandler.OBJECT_NOT_FOUND, formatResponse.get(0).getError());
    assertNull(formatResponse.get(0).getUrl());
  }

  @Test
  public void testHandleRequest_siteNotFound() {
    when(idProvider.parseId(id)).thenReturn(null);
    when(sitesService.findSite(siteId)).thenReturn(Optional.empty());
    List<UrlServiceResponse> formatResponse = urlHandler.handleId(idWithSite, request, response);

    assertTrue(formatResponse.stream().findFirst().isPresent());
    assertEquals(UrlHandler.SITE_NOT_FOUND, formatResponse.get(0).getError());
    assertNull(formatResponse.get(0).getUrl());
  }
}
