package com.coremedia.blueprint.boot.studio.packagesproxy;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class StudioClientResourcesProxyServlet extends ProxyServlet {

  // Patched Method that accounts for the possibility that the servletRequest's
  // InputStream was already read.
  protected HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri, HttpServletRequest servletRequest) throws IOException {
    // Try  to obtain request body as parameters
    Map<String, String[]> params = servletRequest.getParameterMap();
    if (params.isEmpty()) {
      // If empty, use parent method (which uses the ServletRequest's stream)
      return super.newProxyRequestWithEntity(method, proxyRequestUri, servletRequest);
    }

    String paramString = params.entrySet()
            .stream()
            .map(entry -> {
              String values = String.join(",", entry.getValue());
              return entry.getKey() + "=" + values;
            })
            .collect(Collectors.joining("&"));

    HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
    eProxyRequest.setEntity(new StringEntity(paramString));

    return eProxyRequest;
  }
}
