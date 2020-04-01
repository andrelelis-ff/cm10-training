package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FlowUrlHelperTest {

  private static final String REQUEST_SCHEME = "https";
  private static final String REQUEST_URL = REQUEST_SCHEME + "://host.name/a/b/c";
  private static final String ABSOLUTE_URL = "http://host.name2/e/f";
  private static final String SCHEMELESS_URL = "//host.name3/g/h";

  private FlowUrlHelper flowUrlHelper;

  @Mock
  private RequestContext requestContext;


  @Mock
  private ExternalContext externalContext;

  @Mock
  private HttpServletRequest request;


  @Before
  public void setup() {
    flowUrlHelper = new FlowUrlHelper();
    when(requestContext.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getNativeRequest()).thenReturn(request);
    when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));
  }

  @Test
  public void testDontPrependScheme() {
    String result = flowUrlHelper.getNextUrl(ABSOLUTE_URL, requestContext);
    assertEquals(ABSOLUTE_URL, result);
  }

  @Test
  public void testPrependScheme() {
    String result = flowUrlHelper.getNextUrl(SCHEMELESS_URL, requestContext);
    assertEquals(REQUEST_SCHEME + ":" + SCHEMELESS_URL, result);
  }

}
