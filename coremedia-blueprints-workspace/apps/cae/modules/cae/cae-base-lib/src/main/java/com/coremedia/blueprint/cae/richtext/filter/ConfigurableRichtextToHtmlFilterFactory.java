package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.objectserver.view.RichtextToHtmlFilterFactory;
import com.coremedia.xml.Markup;
import org.xml.sax.XMLFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configurable richtext to HTML filter factory that can replace the default richtext to HTML filter factory in the
 * richtextMarkupView bean. It inherits all properties of the default richtext to HTML filter factory and in
 * addition, lets you add custom filter factories before and after the default ones. All
 * filter factories in <code>xmlFilters</code> will be executed after the default filters, all filters in
 * <code>xmlFiltersBeforeUriFormatter</code> will be executed before the default filters.
 */
public class ConfigurableRichtextToHtmlFilterFactory extends RichtextToHtmlFilterFactory {

  private List<FilterFactory> xmlFilters = Collections.emptyList();
  private List<FilterFactory> xmlFiltersBeforeUriFormatter = Collections.emptyList();


  // --- features ---------------------------------------------------

  @Override
  public List<XMLFilter> createFilters(HttpServletRequest request, HttpServletResponse response, Markup markup, String s) {
    List<XMLFilter> newXmlFilters = new ArrayList<>();

    // Add before URI Formatter
    for (FilterFactory f : xmlFiltersBeforeUriFormatter) {
      newXmlFilters.add(f.getInstance(request, response));
    }

    // Add super and replace UriFormatter with our own class
    newXmlFilters.addAll(super.createFilters(request, response, markup, s));

    // Add after
    for (FilterFactory f : xmlFilters) {
      newXmlFilters.add(f.getInstance(request, response));
    }

    return newXmlFilters;
  }


  // --- configuration ----------------------------------------------

  public void setXmlFilters(List<FilterFactory> xmlFilters) {
    this.xmlFilters = xmlFilters;
  }

  // @VisibleForPropertyCustomizers
  public List<FilterFactory> getXmlFilters() {
    return xmlFilters;
  }

  public void setXmlFiltersBeforeUriFormatter(List<FilterFactory> xmlFiltersBeforeUriFormatter) {
    this.xmlFiltersBeforeUriFormatter = xmlFiltersBeforeUriFormatter;
  }

  // @VisibleForPropertyCustomizers
  public List<FilterFactory> getXmlFiltersBeforeUriFormatter() {
    return xmlFiltersBeforeUriFormatter;
  }
}

