package com.coremedia.blueprint.cae.handlers;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import java.util.Map;

/**
 * <p>
 * Provides convenience API to trigger requests with Spring MVC Test Framework.
 * </p>
 * <dl>
 * <dt><strong>Note:</strong></dt>
 * <dd>
 * Requires not only to select {@code SpringJUnit4ClassRunner} as test runner, but also to annotate your test class
 * with {@code org.springframework.test.context.web.WebAppConfiguration}.
 * </dd>
 * </dl>
 */
public class RequestTestHelper {
  @Inject
  private MockMvc mockMvc;

  /**
   * Resolve the URL.
   */
  public ModelAndView request(String url) throws Exception {
    return request(url, null);
  }

  /**
   * Resolve the URL.
   */
  public ModelAndView request(String url,
                              @Nullable Map<String, String> params) throws Exception {
    return request(url, params, null);
  }

  /**
   * Resolve the URL.
   */
  public ModelAndView request(String url,
                              @Nullable Map<String, String> params,
                              @Nullable Map<String, Object> requestAttributes)
          throws Exception {
    return request(url, params, requestAttributes, null);
  }

  /**
   * Resolve the URL.
   */
  public ModelAndView request(String url,
                              @Nullable Map<String, String> params,
                              @Nullable Map<String, Object> requestAttributes,
                              @Nullable String contentType)
          throws Exception {

    MvcResult mvcResult = requestMvcResult(url, params, requestAttributes, contentType);
    return mvcResult.getModelAndView();
  }

  public MvcResult requestMvcResult(String url,
                                    @Nullable Map<String, String> params,
                                    @Nullable Map<String, Object> requestAttributes,
                                    @Nullable String contentType) throws Exception {
    MockHttpServletRequestBuilder req = createRequest(url);

    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        req.param(entry.getKey(), entry.getValue());
      }
    }
    if (requestAttributes != null) {
      for (Map.Entry<String, Object> entry : requestAttributes.entrySet()) {
        req.requestAttr(entry.getKey(), entry.getValue());
      }
    }
    if (contentType != null) {
      req.contentType(MediaType.parseMediaType(contentType));
    }
    return mockMvc.perform(req).andReturn();
  }

  public MockHttpServletRequestBuilder createRequest(String shortUrl) {
    return MockMvcRequestBuilders
            .get("/context/servlet" + shortUrl)
            .contextPath("/context")
            .servletPath("/servlet")
            .characterEncoding("UTF-8");
  }

}
