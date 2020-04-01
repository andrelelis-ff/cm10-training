package com.coremedia.blueprint.ecommerce.cae;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebCommerceContextInterceptorTest {

  @Mock
  private SiteResolver siteResolver;

  @Mock
  private Site site;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  private MockHttpServletRequest request;
  private HttpServletResponse response;
  private Object handler;

  private WebCommerceContextInterceptor testling;

  @Before
  public void setup() {
    testling = new WebCommerceContextInterceptor();

    CommerceConnection connection = mock(CommerceConnection.class);

    StoreContextImpl storeContext = StoreContextBuilderImpl.from(connection, "any-site-id").build();

    when(connection.getStoreContext()).thenReturn(storeContext);

    CurrentStoreContext.set(storeContext);

    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(connection));

    testling.setSiteResolver(siteResolver);
    testling.setInitUserContext(false);
    testling.setCommerceConnectionInitializer(commerceConnectionInitializer);
    testling.setPreview(false);

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    handler = new Object();
  }

  @After
  public void tearDown() {
    CurrentStoreContext.remove();
  }

  @Test
  public void testPreHandle() {
    String path = "/helios";
    request.setPathInfo(path);

    when(siteResolver.findSiteByPath(path)).thenReturn(site);

    testling.preHandle(request, response, handler);

    verify(commerceConnectionInitializer).findConnectionForSite(any(Site.class));
    assertThat(SiteHelper.findSite(request)).isPresent();
  }

  @Test
  public void testNoopPreHandle() {
    String path = "/nosite";
    request.setPathInfo(path);

    StoreContext storeContextBefore = CurrentStoreContext.get();

    testling.preHandle(request, response, handler);

    verify(commerceConnectionInitializer, never()).findConnectionForSite(any(Site.class));
    StoreContext storeContextAfter = CurrentStoreContext.get();
    assertThat(storeContextAfter).isSameAs(storeContextBefore);
    assertThat(SiteHelper.findSite(request)).isNotPresent();
  }
}
