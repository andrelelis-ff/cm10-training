package com.coremedia.blueprint.cae.view;

import com.coremedia.objectserver.view.ServletView;
import com.coremedia.blueprint.cae.web.HttpHead;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * A View for {@link HttpHead} beans.
 * <p>
 * Use cautiously!  For details see
 * {@link com.coremedia.blueprint.cae.handlers.PageHandlerBase#optimizeForHeadRequest}
 */
public class HttpHeadView implements ServletView<HttpHead> {

  @Override
  public void render(HttpHead self, String viewName, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    String contentType = self.getContentType();
    if (contentType!=null) {
      response.setContentType(contentType);
    }
    String charSet = self.getCharSet();
    if (charSet!=null) {
      response.setCharacterEncoding(charSet);
    }
    for (Map.Entry<String, String> header : self.getHeaders().entrySet()) {
      response.setHeader(header.getKey(), header.getValue());
    }
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
