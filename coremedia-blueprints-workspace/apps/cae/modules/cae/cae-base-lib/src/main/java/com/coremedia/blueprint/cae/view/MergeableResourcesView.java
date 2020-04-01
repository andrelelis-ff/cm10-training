package com.coremedia.blueprint.cae.view;

import com.coremedia.cap.util.PairCacheKey;
import com.coremedia.blueprint.cae.richtext.filter.ScriptFilter;
import com.coremedia.blueprint.cae.richtext.filter.ScriptSerializer;
import com.coremedia.blueprint.cae.view.processing.Minifier;
import com.coremedia.blueprint.common.contentbeans.CMAbstractCode;
import com.coremedia.blueprint.common.contentbeans.MergeableResources;
import com.coremedia.cache.Cache;
import com.coremedia.objectserver.view.ServletView;
import com.coremedia.objectserver.view.XmlFilterFactory;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.XMLFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A programmed view to retrieve merged CSS/JS-Code from a page.
 */
public class MergeableResourcesView implements ServletView {

  private static final Logger LOG = LoggerFactory.getLogger(MergeableResourcesView.class);

  private Minifier minifier;
  private XmlFilterFactory xmlFilterFactory;
  private Cache cache;
  private String contentType;

  /**
   * Set the xmlFilterFactory from which the main filters can be retrieved.
   *
   * @param xmlFilterFactory the filter factory
   */
  @Required
  public void setXmlFilterFactory(XmlFilterFactory xmlFilterFactory) {
    this.xmlFilterFactory = xmlFilterFactory;
  }

  /**
   * If you set a cache, minified scripts will be cached.
   * <p>
   * If your minifier's results are not cacheable, do not set a cache.
   */
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Required
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setMinifier(Minifier minifier) {
    this.minifier = minifier;
  }

  /**
   * Renders {@link CMAbstractCode#getCode() resources } attached to a page into one merged file.
   * <br/>
   * Before merging, code will be preprocessed.
   * This is necessary because code is stored in a Richtext property, and the Richtext specific XML has to be
   * removed before writing the contents.
   *
   * @param bean     the page containing the resources.
   * @param view     the view
   * @param request  the request
   * @param response the response
   */
  @Override
  public void render(Object bean, String view, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    if (!(bean instanceof MergeableResources)) {
      throw new IllegalArgumentException(bean + " is no MergeableResources");
    }

    try {
      MergeableResources codeResources = (MergeableResources) bean;
      PrintWriter writer = response.getWriter();
      renderResources(request, response, codeResources, writer);
      writer.flush();
    } catch (IOException e) {
      LOG.error("Error retrieving writer from HttpServletResponse.", e);
    }
  }

  //====================================================================================================================

  /**
   * Merge and render CMS-managed resources. Will also handle device-specific excludes.
   *
   * @param request    the request
   * @param response   the response
   * @param codeResources the codeResources element containing the resources
   * @param out        the writer to render to
   */
  private void renderResources(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                               MergeableResources codeResources, Writer out) {
    List<CMAbstractCode> codes = codeResources.getMergeableResources();

    //set correct contentType
    response.setContentType(contentType);

    for (CMAbstractCode code : codes) {
      renderResource(request, response, code, out);
    }

  }

  /**
   * Render the given {@link CMAbstractCode resource} with all it's {@link CMAbstractCode#getInclude() includes}.
   *
   * @param request  the request
   * @param response the response
   * @param code     the resource
   * @param out      the writer to render to
   */
  private void renderResource(HttpServletRequest request, HttpServletResponse response, CMAbstractCode code, Writer out) {
    String script = filterScriptMarkup(request, response, code);
    if (minifier != null && !code.isCompressionDisabled()) {
      String name = code.getContent().getName();
      script = cache != null ? cache.get(new MinifierCacheKey(script, name)) : minify(script, name);
    }

    try {
      out.write(script);
      out.append('\n');

    } catch (IOException e) {
      LOG.error("Unable to write Script to response.", e);
    }
  }

  /**
   * Removes the CoreMedia markup from the script code by applying all configured filters.
   */
  private String filterScriptMarkup(HttpServletRequest request, HttpServletResponse response, CMAbstractCode code) {
    //construct xmlFilters to strip RichText from <div> and <p> tags
    Markup unfilteredCode = code.getCode();
    List<XMLFilter> filters = new ArrayList<>();
    filters.addAll(xmlFilterFactory.createFilters(request, response, unfilteredCode, "script"));
    filters.add(new ScriptFilter());

    //strip <div> and <p> from markup
    StringWriter writer = new StringWriter();
    ScriptSerializer handler = new ScriptSerializer(writer);
    unfilteredCode.writeOn(filters, handler);
    return writer.getBuffer().toString();
  }

  private String minify(String script, String name)  {
    try {
      StringWriter resultStringWriter = new StringWriter();
      minifier.minify(resultStringWriter, new StringReader(script), name);
      return resultStringWriter.getBuffer().toString();
    } catch (Exception e) {
      LOG.info("Could not minify file {}. Will write unminified version. Cause: {}", name, e.getMessage());
      return script;
    }
  }

  private class MinifierCacheKey extends PairCacheKey<String, String, String> {
    MinifierCacheKey(String script, String name) {
      super(script, name);
    }

    @Override
    protected String evaluate(Cache cache, String script, String name) {
      return minify(script, name);
    }
  }
}
