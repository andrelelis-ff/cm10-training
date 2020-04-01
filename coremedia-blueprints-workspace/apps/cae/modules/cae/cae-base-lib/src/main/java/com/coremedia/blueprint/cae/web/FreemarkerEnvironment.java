package com.coremedia.blueprint.cae.web;

import freemarker.core.Environment;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides servlet request and response from the current Freemarker {@link Environment}.
 *
 * <p>Methods of this class must be called from Freemarker web template processing and will
 * throw {@link IllegalStateException} otherwise.
 */
public class FreemarkerEnvironment {

  private FreemarkerEnvironment() {
  }

  public static HttpServletRequest getCurrentRequest() {
    return getHttpRequestHashModel().getRequest();
  }

  public static HttpServletResponse getCurrentResponse() {
    return getHttpRequestHashModel().getResponse();
  }

  // ----------------------------------------------------------------------

  private static HttpRequestHashModel getHttpRequestHashModel() {
    Environment currentEnvironment = Environment.getCurrentEnvironment();
    if (currentEnvironment != null) {
      try {
        TemplateModel model = currentEnvironment.getDataModel().get(FreemarkerServlet.KEY_REQUEST);
        if (model instanceof HttpRequestHashModel) {
          return ((HttpRequestHashModel) model);
        }
      } catch (TemplateModelException e) {
        throw new IllegalStateException("Freemarker HTTP request model not found. " +
                                        "Method must be called from FreeMarker web template.", e);
      }
    }

    throw new IllegalStateException("Freemarker HTTP request model not found. " +
                                    "Method must be called from FreeMarker web template.");
  }

}
