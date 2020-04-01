package com.coremedia.livecontext.preview;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.link.PreviewUrlService;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.coremedia.objectserver.web.links.ParameterAppendingLinkTransformer;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * LinkTransformer implementation that adds preview request parameters to links.
 */
public class PreviewParametersAppendingLinkTransformer implements LinkTransformer {

  private boolean preview;

  public boolean isPreview() {
    return preview;
  }

  @Value("${cae.is.preview}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  @Override
  public String transform(String source, Object bean, String view, @NonNull HttpServletRequest request,
                          @NonNull HttpServletResponse response, boolean forRedirect) {
    if (!isPreview()) {
      return source;
    }

    PreviewUrlService previewUrlService = CurrentStoreContext.find()
            .map(StoreContext::getConnection)
            .flatMap(CommerceConnection::getPreviewUrlService)
            .orElse(null);

    if (previewUrlService == null) {
      return source;
    }

    Set<String> parameterNames = previewUrlService.getParameterNames();

    String transformed = source;
    for (String parameterName : parameterNames) {
      transformed = appendParameter(transformed, parameterName, bean, view, request, response, forRedirect);
    }

    return transformed;
  }

  private static String appendParameter(String source, String parameterName, Object bean, String view,
                                        @NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        boolean forRedirect) {
    ParameterAppendingLinkTransformer parameterAppendingLinkTransformer = new ParameterAppendingLinkTransformer();
    parameterAppendingLinkTransformer.setParameterName(parameterName);
    return parameterAppendingLinkTransformer.transform(source, bean, view, request, response, forRedirect);
  }
}
