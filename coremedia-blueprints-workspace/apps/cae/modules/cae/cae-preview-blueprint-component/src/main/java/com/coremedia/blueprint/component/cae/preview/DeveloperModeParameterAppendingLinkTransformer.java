package com.coremedia.blueprint.component.cae.preview;

import com.coremedia.objectserver.web.links.LinkTransformer;
import com.coremedia.objectserver.web.links.ParameterAppendingLinkTransformer;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link LinkTransformer} implementation that adds the request parameters for developer mode to links.
 */
public class DeveloperModeParameterAppendingLinkTransformer implements LinkTransformer {

  private final ParameterAppendingLinkTransformer userVariantAppender;

  public DeveloperModeParameterAppendingLinkTransformer() {
    userVariantAppender = new ParameterAppendingLinkTransformer();
    userVariantAppender.setParameterName("userVariant");
  }

  @PostConstruct
  void initialize() throws Exception {
    userVariantAppender.afterPropertiesSet();
  }

  @Override
  public String transform(String source, Object bean, String view, @NonNull HttpServletRequest request,
                          @NonNull HttpServletResponse response, boolean forRedirect) {
    return userVariantAppender.transform(source, bean, view, request, response, forRedirect);
  }
}
