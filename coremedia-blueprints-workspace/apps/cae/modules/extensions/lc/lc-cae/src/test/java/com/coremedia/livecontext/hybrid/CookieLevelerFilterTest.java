package com.coremedia.livecontext.hybrid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CookieLevelerFilterTest {

  private CookieLevelerFilter testling;

  @Mock
  private FilterChain filterChain;

  @Before
  public void beforeEachTest() {
    testling = new CookieLevelerFilter();
  }

  @Test
  public void doFilterNoHttpServletRequest() throws Exception {
    ServletRequest request = mock(ServletRequest.class);
    ServletResponse response = mock(HttpServletResponse.class);
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterNoHttpServletResponse() throws Exception {
    ServletRequest request = mock(HttpServletRequest.class);
    ServletResponse response = mock(ServletResponse.class);
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterCookieDomainsNull() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    testling.setCookieDomain(null);
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterCookieDomainsEmpty() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    testling.setCookieDomain(new String[] {});
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterHasMatchingCookieDomainOfOneAvailable() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getServerName()).thenReturn("app.subdomain.tld1");

    testling.setCookieDomain(new String[] {".subdomain.tld1"});
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(same(request), isA(CookieLevelerFilter.HttpServletResponseCookieAware.class));
  }

  @Test
  public void doFilterHasMatchingCookieDomainOfTwoAvailableMatchingFirst() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getServerName()).thenReturn("app.subdomain.tld1");

    testling.setCookieDomain(new String[] {".subdomain.tld1","subdomain.tld2"});
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(same(request), isA(CookieLevelerFilter.HttpServletResponseCookieAware.class));
  }

  @Test
  public void doFilterHasMatchingCookieDomainOfTwoAvailableMatchingSecond() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getServerName()).thenReturn("app.subdomain.tld2");

    testling.setCookieDomain(new String[]{".subdomain.tld1",".subdomain.tld2"});
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(same(request), isA(CookieLevelerFilter.HttpServletResponseCookieAware.class));
  }

  @Test
  public void doFilterNoMatchingCookieDomain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getServerName()).thenReturn("app.subdomain.tld3");

    testling.setCookieDomain(new String[] {".subdomain.tld1",".subdomain.tld2"});
    testling.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }
}
