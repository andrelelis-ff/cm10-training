package com.coremedia.blueprint.cae.web;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * A model that represents a HTTP HEAD response.
 * <p>
 * Use cautiously!  For details see
 * {@link com.coremedia.blueprint.cae.handlers.PageHandlerBase#optimizeForHeadRequest}
 */
public class HttpHead {
  private String charSet = "UTF-8";
  private String contentType = "text/html";

  public HttpHead() {
  }

  public HttpHead(String charSet, String contentType) {
    this.charSet = charSet;
    this.contentType = contentType;
  }

  public String getCharSet() {
    return charSet;
  }

  public String getContentType() {
    return contentType;
  }

  public Map<String, String> getHeaders() {
    // Be aware that at least with Tomcat's implementation of
    // HttpServletResponse the Content-Length header has no effect!
    return ImmutableMap.of("Content-Type", contentType+";charset="+charSet, "Content-Length", "");
  }
}
