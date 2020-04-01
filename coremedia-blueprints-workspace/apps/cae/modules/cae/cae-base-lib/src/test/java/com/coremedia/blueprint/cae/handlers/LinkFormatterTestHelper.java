package com.coremedia.blueprint.cae.handlers;

import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.links.LinkFormatter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import java.util.Map;

/**
 * <p>
 * Provides convenience API to format links using the Spring MVC Test Framework.
 * </p>
 * <dl>
 * <dt><strong>Note:</strong></dt>
 * <dd>
 * Requires not only to select {@code SpringJUnit4ClassRunner} as test runner, but also to annotate your test class
 * with {@code org.springframework.test.context.web.WebAppConfiguration}.
 * </dd>
 * </dl>
 */
public class LinkFormatterTestHelper {

  @Inject
  private MockHttpServletRequest request;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private MockHttpServletResponse response;

  @Inject
  private LinkFormatter linkFormatter;

  /**
   * Format a link with the given parameters.
   */
  public String formatLink(Object bean) {
    return formatLink(null, bean);
  }

  /**
   * Format a link with the given parameters.
   */
  public String formatLink(@Nullable Map<String, Object> cmParams, Object bean) {
    return formatLink(cmParams, bean, null);
  }

  /**
   * Format a link with the given parameters.
   */
  public String formatLink(@Nullable Map<String, Object> cmParams, Object bean,
                           @Nullable Map<String, Object> requestAttributes) {
    return formatLink(cmParams, bean, requestAttributes, null);
  }

  /**
   * Format a link with the given parameters.
   */
  public String formatLink(@Nullable Map<String, Object> cmParams, Object bean,
                           @Nullable Map<String, Object> requestAttributes, @Nullable String view) {
    if (cmParams != null) {
      request.setAttribute(ViewUtils.PARAMETERS, cmParams);
    }

    if (requestAttributes != null) {
      for (Map.Entry<String, Object> attribute : requestAttributes.entrySet()) {
        request.setAttribute(attribute.getKey(), attribute.getValue());
      }
    }

    return linkFormatter.formatLink(bean, view, request, response, false);
  }
}
