package com.coremedia.blueprint.cae.action.webflow;

import org.springframework.util.StringUtils;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A custom {@link org.springframework.webflow.context.servlet.FlowUrlHandler} that generates URIs based on the setting
 * of {@link #prependBaseUri}
 * It will prepend /servlet/context/ if {@link #prependBaseUri} is set to true
 */
public class BlueprintFlowUrlHandler extends DefaultFlowUrlHandler {

  public static final String FLOW_EXECUTION_KEY_PARAMETER = "execution";
  private boolean prependBaseUri = true;


  @Override
  public String createFlowExecutionUrl(String flowId, String flowExecutionKey, HttpServletRequest request) {
    StringBuffer url = new StringBuffer();
    if (prependBaseUri) {
      url.append(request.getRequestURI());
    } else {
      url.append(request.getPathInfo());
    }

    url.append('?');
    appendQueryParameter(url, FLOW_EXECUTION_KEY_PARAMETER, flowExecutionKey, getEncodingScheme(request));

    return url.toString();
  }

  @Override
  public String createFlowDefinitionUrl(String flwId, AttributeMap input, HttpServletRequest request) {
    //re-request the flowId here since we want the URL and not the id of the flow that may have executed a redirect
    String flowId = getFlowId(request);
    StringBuilder url = new StringBuilder();
    if (request.getPathInfo() != null && prependBaseUri) {
      url.append(request.getContextPath());
      url.append(request.getServletPath());
      url.append('/');
      url.append(flowId);
    } else {
      String servletPath = request.getServletPath();
      if (StringUtils.hasText(servletPath) && prependBaseUri) {
        url.append(request.getContextPath());
        url.append('/');
        url.append(flowId);
        int dotIndex = servletPath.lastIndexOf('.');
        if (dotIndex != -1) {
          url.append(servletPath.substring(dotIndex));
        }
      } else {
        url.append('/');
        url.append(flowId);
      }
    }
    if (input != null && !input.isEmpty()) {
      url.append('?');
      appendQueryParameters(url, input.asMap(), getEncodingScheme(request));
    }
    return url.toString();
  }

  public void setPrependBaseUri(boolean prependBaseUri) {
    this.prependBaseUri = prependBaseUri;
  }


  //====================================================================================================================
  // internal helpers

  private void appendQueryParameter(StringBuffer url, Object key, Object value, String encodingScheme) {
    String encodedKey = encode(key, encodingScheme);
    String encodedValue = encode(value, encodingScheme);
    url.append(encodedKey).append('=').append(encodedValue);
  }

  private String encode(Object value, String encodingScheme) {
    return value != null ? urlEncode(value.toString(), encodingScheme) : "";
  }

  private String urlEncode(String value, String encodingScheme) {
    try {
      return URLEncoder.encode(value, encodingScheme);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Cannot url encode " + value, e);
    }
  }

}
