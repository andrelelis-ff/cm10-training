package com.coremedia.livecontext.ecommerce.ibm.preview;

import com.coremedia.livecontext.handler.LiveContextPageHandlerBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class WcCookieFilterTest {

  @Mock
  private ServletResponse response;

  @InjectMocks
  private WcCookieFilter cookieFilter;

  @Test
  public void doFilter() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(LiveContextPageHandlerBase.HAS_PREVIEW_TOKEN, true);
    request.setCookies(
            new Cookie("WC_USERACTIVITY_-1002","-1002%2C10302%2Cbla"),
            new Cookie("WC_AnotherActivity", "doesNotMatter")
    );
    MyFilterChain filterChain = new MyFilterChain();

    cookieFilter.doFilter(request, response, filterChain);
    cookieFilter.destroy();
  }

  private class MyFilterChain implements FilterChain {
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
      assertEquals(0, ((HttpServletRequest) servletRequest).getCookies().length);
      assertFalse(((HttpServletRequest) servletRequest).getHeader("Cookie").contains("WC_"));
    }
  }
}
