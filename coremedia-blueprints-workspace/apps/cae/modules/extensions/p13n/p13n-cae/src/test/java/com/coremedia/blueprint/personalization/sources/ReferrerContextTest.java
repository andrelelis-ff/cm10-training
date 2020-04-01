package com.coremedia.blueprint.personalization.sources;

import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.context.collector.CookieSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Tests {@link ReferrerContext}.
 */
public final class ReferrerContextTest {

  private static final String CONTEXT_NAME = "referrer";
  private ContextCollection contextCollection;
  private CookieSource source;

  @Before
  public void setUp() throws Exception {
    contextCollection = new ContextCollectionImpl();
    source = new CookieSource();
    source.setCookieName("cmReferrerCookie");
    source.setContextCoDec(new ReferrerContext.CoDec());
    source.setContextName(CONTEXT_NAME);
  }

  @After
  public void teardown() {
    contextCollection.clear();
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testGoogleReferrerOld() throws Exception {
    String googleReferrer = "http://www.google.de/search?q=google+%22url+referrer%22&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:de:official&client=firefox-a";
    mockReferrerSource(googleReferrer);

    final PropertyProvider referrerContext = contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(googleReferrer, referrerContext.getProperty("url"));
    Assert.assertEquals("google", referrerContext.getProperty("searchengine"));
    Assert.assertEquals("google \"url referrer\"", referrerContext.getProperty("query"));
  }

  @Test
  public void testGoogleReferrerNew() throws Exception {
    // test whether we can decode the "new" style of google referrer URLs
    final String googleReferrer = "http://www.google.com/url?sa=t&source=web&ct=res&cd=7&url=http%3A%2F%2Fwww.example.com%2Fmypage.htm&ei=0SjdSa-1N5O8M_qW8dQN&rct=j&q=flowers&usg=AFQjCNHJXSUh7Vw7oubPaO3tZOzz-F-u_w&sig2=X8uCFh6IoPtnwmvGMULQfw";
    mockReferrerSource(googleReferrer);

    final PropertyProvider referrerContext = contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(googleReferrer, referrerContext.getProperty("url"));
    Assert.assertEquals("google", referrerContext.getProperty("searchengine"));
    Assert.assertEquals("flowers", referrerContext.getProperty("query"));
  }

  @Test
  public void testBingReferrer() throws Exception {
    final String bingReferrer = "http://www.bing.com/search?q=bing+%22referrer+url%22&go=&form=QBLH&filt=all";
    mockReferrerSource(bingReferrer);

    final PropertyProvider referrerContext = contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(bingReferrer, referrerContext.getProperty("url"));
    Assert.assertEquals("bing", referrerContext.getProperty("searchengine"));
    Assert.assertEquals("bing \"referrer url\"", referrerContext.getProperty("query"));
  }

  @Test
  public void testYahooReferrer() throws Exception {
    final String yahooReferrer = "http://de.search.yahoo.com/search?p=url+decode&ei=UTF-8&fr=moz35";
    mockReferrerSource(yahooReferrer);

    final PropertyProvider referrerContext = contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(yahooReferrer, referrerContext.getProperty("url"));
    Assert.assertEquals("yahoo", referrerContext.getProperty("searchengine"));
    Assert.assertEquals("url decode", referrerContext.getProperty("query"));
  }

  @Test
  public void testUnknownReferrer() throws Exception {
    final String referrer = "http://search.coremedia.com/search?q=url+decode&ei=UTF-8&fr=moz35";
    mockReferrerSource(referrer);

    final PropertyProvider referrerContext = contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(referrer, referrerContext.getProperty("url"));
    Assert.assertNull(referrerContext.getProperty("searchengine"));
    Assert.assertNull(referrerContext.getProperty("query"));
  }

  @Test
  public void testEmptyReferrer() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    final MockHttpServletResponse response = new MockHttpServletResponse();
    source.preSession(new MockHttpSession(), contextCollection);
    request.setCookies(response.getCookies());
    source.preHandle(request, response, contextCollection);

    final PropertyProvider referrerContext = contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class);
    ReferrerContext ctx = (ReferrerContext)referrerContext;
    Assert.assertTrue(ctx.isEmpty());
  }
  
  @Test
  public void testLeaveProfileAloneOnSubsequentRequests() throws Exception {
    final String referrer = "http://search.coremedia.com/search?q=url+decode&ei=UTF-8&fr=moz35";
    mockReferrerSource(referrer);

    Assert.assertEquals(referrer, contextCollection.getContext(CONTEXT_NAME, PropertyProvider.class).getProperty("url"));
    contextCollection.clear();

    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("referer", "http://test.coremedia.com");
    source.preHandle(request, null, contextCollection);
  }
  
  
  private void mockReferrerSource(String url) {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    request.addHeader("referer", url);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    source.preHandle(request, response, contextCollection);
    request.setCookies(response.getCookies());
    source.postHandle(request, response, contextCollection);

  }
  
}
