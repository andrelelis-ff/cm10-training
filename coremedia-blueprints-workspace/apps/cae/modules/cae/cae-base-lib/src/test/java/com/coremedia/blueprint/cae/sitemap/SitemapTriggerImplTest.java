package com.coremedia.blueprint.cae.sitemap;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_INTERNAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

/**
 * Both methods are tested independently because generateSitemaps() has no result and it's hard to test.
 */
public class SitemapTriggerImplTest {

  @InjectMocks
  @Spy
  private SitemapTriggerImpl sitemapTrigger;

  @Mock
  private UrlPathFormattingHelper urlPathFormattingHelper;

  @Mock
  private SitesService sitesService;

  @Mock
  private ServletContext servletContext;

  @Mock
  private SitemapHelper sitemapHelper;

  @Test
  public void testGenerateSitemaps() throws IOException {
    Set<Site> sites = new HashSet<>();
    Site enabledSite = createSite(true);
    Site disabledSite = createSite(false);

    sites.add(enabledSite);
    sites.add(disabledSite);
    when(sitesService.getSites()).thenReturn(sites);

    //trigger
    //just ignore generate sitemap because this logic will be tested in a separated test...
    doReturn("result").when(sitemapTrigger).generateSitemap(any(Site.class));

    sitemapTrigger.generateSitemaps();
    verify(sitemapTrigger, times(1)).generateSitemap(enabledSite);
    verify(sitemapTrigger, times(0)).generateSitemap(disabledSite);
  }

  @Test
  public void testGenerateSitemap() throws IOException {
    //inputs
    Site site = mock(Site.class);
    String siteSegment = "testSegment";

    //expected results
    String expectedResult = "Sitemap has been written to /path/to/sitemap/siteId, Timestamp";

    //mocking
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    mockSegment(site, siteSegment);
    doReturn(httpClient).when(sitemapTrigger).createHttpClient();

    CloseableHttpResponse response = mockResponse(HttpServletResponse.SC_OK, expectedResult);
    HttpGetMatcher httpGetMatcher = new HttpGetMatcher("http://localhost:49080/blueprint/servlet/" + PREFIX_INTERNAL + "/" + siteSegment + "/sitemap-org");
    when(httpClient.execute(argThat(httpGetMatcher))).thenReturn(response);

    //action
    String result = sitemapTrigger.generateSitemap(site);

    assertEquals(expectedResult, result);
  }

  @Test (expected = IllegalStateException.class)
  public void testGenerateSitemapFailed() throws IOException {
    //inputs
    Site site = mock(Site.class);
    String siteSegment = "testSegment";

    //mocking
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    mockSegment(site, siteSegment);
    doReturn(httpClient).when(sitemapTrigger).createHttpClient();

    CloseableHttpResponse response = mockResponse(HttpServletResponse.SC_BAD_REQUEST);
    HttpGetMatcher httpGetMatcher = new HttpGetMatcher("http://localhost:49080/blueprint/servlet/" + PREFIX_INTERNAL + "/" + siteSegment + "/sitemap-org");
    when(httpClient.execute(argThat(httpGetMatcher))).thenReturn(response);

    //action
    sitemapTrigger.generateSitemap(site);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGenerateSitemapNoSiteSegment() throws IOException {
    //inputs
    Site site = mock(Site.class);
    String siteSegment = null;

    //mocking
    mockSegment(site, siteSegment);

    //action
    sitemapTrigger.generateSitemap(site);
  }

  private CloseableHttpResponse mockResponse(int statusCode, String expectedResult) throws IOException {
    CloseableHttpResponse response = mockResponse(statusCode);
    when(response.getEntity().getContent()).thenReturn(new ByteArrayInputStream(expectedResult.getBytes()));
    return response;
  }

  private CloseableHttpResponse mockResponse(int statusCode) throws IOException {
    CloseableHttpResponse response = mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
    when(response.getStatusLine().getStatusCode()).thenReturn(statusCode);
    return response;
  }

  private void mockSegment(Site site, String siteSegment) {
    Content siteRootDocument = mock(Content.class);
    when(site.getSiteRootDocument()).thenReturn(siteRootDocument);
    when(urlPathFormattingHelper.getVanityName(siteRootDocument)).thenReturn(siteSegment);
  }

  private Site createSite(boolean isEnabledForGeneration) {
    Site site = mock(Site.class);
    when(sitemapHelper.isSitemapEnabled(site)).thenReturn(isEnabledForGeneration);
    return site;
  }

  @Before
  public void setUp() {
    initMocks(this);
    configureServletContext();
  }

  private void configureServletContext() {
    sitemapTrigger.setServletContext(servletContext);
    when(servletContext.getContextPath()).thenReturn("/blueprint");
  }

  private class HttpGetMatcher extends BaseMatcher<HttpGet> {

    private String expectedUrl;

    public HttpGetMatcher(String expectedUrl) {
      this.expectedUrl = expectedUrl;
    }

    @Override
    public boolean matches(Object item) {
      if (!(item instanceof HttpGet)) {
        return false;
      }

      HttpGet actual = (HttpGet) item;
      if(actual.getURI().toString().equals(expectedUrl)) {
        return true;
      }

      return false;
    }

    @Override
    public void describeTo(Description description) {
      //not implemented because its not a verify matcher, so this description is not needed
    }
  }
}
