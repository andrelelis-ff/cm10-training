package com.coremedia.livecontext.preview;

import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreviewCommerceContextInterceptorTest {

  private PreviewCommerceContextInterceptor interceptor;
  private LiveContextSiteResolver sitesResolver;
  private SitesService sitesService;
  private HttpServletRequest request;
  private Map<String, String[]> params;
  private Site abcSite;

  @Before
  public void setup() {
    interceptor = new PreviewCommerceContextInterceptor();
    sitesResolver = mock(LiveContextSiteResolver.class);
    sitesService = mock(SitesService.class);
    abcSite = mock(Site.class);
    when(sitesService.getSite("abc")).thenReturn(abcSite);
    interceptor.setSitesService(sitesService);
    interceptor.setSiteResolver(sitesResolver);
    request = mock(HttpServletRequest.class);
    params = new HashMap<>();
    when(request.getParameterMap()).thenReturn(params);
  }

  @Test
  public void testWithContentId() {
    String id = "123";
    params.put("id", new String[]{id});

    interceptor.findSite(request, "path");

    verify(sitesResolver).findSiteForContentId(Integer.parseInt(id));
  }

  @Test
  public void testWithSiteId() {
    String siteId = "abc";
    params.put("site", new String[]{siteId});

    Optional<Site> site = interceptor.findSite(request, "path");
    assertThat(site).contains(abcSite);
  }

  @Test
  public void testWithElasticSocialId() {
    String id = "es:comment:539ae297e4b0971a9a345115";
    params.put("id", new String[]{id});

    interceptor.findSite(request, "path");

    verify(sitesResolver, never()).findSiteForContentId(anyInt());
  }
}
