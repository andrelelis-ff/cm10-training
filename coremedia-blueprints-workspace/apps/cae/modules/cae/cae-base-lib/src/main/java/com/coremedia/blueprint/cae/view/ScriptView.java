package com.coremedia.blueprint.cae.view;

import com.coremedia.blueprint.cae.richtext.filter.ScriptFilter;
import com.coremedia.blueprint.cae.richtext.filter.ScriptSerializer;
import com.coremedia.objectserver.view.TextView;
import com.coremedia.objectserver.view.XmlFilterFactory;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.XMLFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.ArrayList;

/**
 * A view to properly serialize client-code (JS, CSS or HTML) stored in CoreMedia richtext.
 *
 * @see com.coremedia.blueprint.common.contentbeans.CMAbstractCode#getCode()
 */
public class ScriptView implements TextView {

  private XmlFilterFactory xmlFilterFactory;

  @Override
  public void render(Object bean, String view, Writer writer, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    if (!(bean instanceof Markup)) {
      throw new IllegalArgumentException(bean + " is no " + Markup.class);
    }

    Markup markup = (Markup) bean;
    ArrayList<XMLFilter> filters = new ArrayList<>();
    filters.addAll(xmlFilterFactory.createFilters(request, response, markup, "script"));
    filters.add(new ScriptFilter());
    // create serializer instance for scripts
    ScriptSerializer handler = new ScriptSerializer(writer);
    // transform and flush markup
    markup.writeOn(filters, handler);
  }

  /**
   * Set the xmlFilterFactory from which the main filters can be retrieved.
   *
   * @param xmlFilterFactory the filter factory
   */
  @Required
  public void setXmlFilterFactory(XmlFilterFactory xmlFilterFactory) {
    this.xmlFilterFactory = xmlFilterFactory;
  }
}