package com.coremedia.blueprint.cae.filter;

import com.coremedia.cap.content.ContentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreviewViewFilterTest {
  private static final boolean LIVE_CAE = true;
  private static final boolean PREVIEW_CAE = false;
  private static final boolean ACCEPT = true;
  private static final boolean REJECT = false;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private PreviewViewFilter testling;


  // --- tests ------------------------------------------------------

  @Test
  public void testViewPattern() {
    testling = new PreviewViewFilter();
    assertTrue(testling.isLiveView("Foo"));

    // case sensitive
    assertTrue(testling.isLiveView("preview"));
    assertFalse(testling.isLiveView("Preview"));

    assertFalse(testling.isLiveView("prefixPreviewPostfix"));
  }

  @Test
  public void testPreviewViewInPreview() throws ServletException, IOException {
    runTest(PREVIEW_CAE, "Preview", ACCEPT);
  }

  @Test
  public void testPreviewViewInLive() throws ServletException, IOException {
    runTest(LIVE_CAE, "Preview", REJECT);
  }

  @Test
  public void testLiveViewInLive() throws ServletException, IOException {
    runTest(LIVE_CAE, "foo", ACCEPT);
  }


  // --- internal ---------------------------------------------------

  private void runTest(boolean live, String view, boolean accept) throws ServletException, IOException {
    setup(live, view);
    testling.doFilterInternal(request, response, filterChain);
    if (accept) {
      checkAccept();
    } else {
      checkReject();
    }
  }

  private void setup(boolean live, String view) {
    when(contentRepository.isLiveServer()).thenReturn(live);
    when(request.getParameter("view")).thenReturn(view);
    testling = new PreviewViewFilter();
    testling.setContentRepository(contentRepository);
    testling.afterPropertiesSet();
  }

  private void checkAccept() throws IOException, ServletException {
    verify(filterChain, times(1)).doFilter(request, response);
  }

  private void checkReject() throws IOException {
    verify(response, times(1)).sendError(HttpServletResponse.SC_FORBIDDEN);
  }
}
