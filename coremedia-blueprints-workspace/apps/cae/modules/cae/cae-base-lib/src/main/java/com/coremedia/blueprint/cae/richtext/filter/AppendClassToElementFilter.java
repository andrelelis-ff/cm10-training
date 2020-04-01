package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Filter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * A Filter class to append a specific CSS class to an HTML element.
 */
public class AppendClassToElementFilter extends Filter implements FilterFactory {
  public static final String ATTRIBUTE_CLASS = "class";
  private Map<String, String> elementList = new HashMap<>();

  /**
   * Always create a new Object
   *
   * @param request  HttpServletRequest
   * @param response HttpServletResponse
   * @return a new instance of {@link AppendClassToElementFilter}
   */
  @Override
  public AppendClassToElementFilter getInstance(HttpServletRequest request, HttpServletResponse response) {
    AppendClassToElementFilter appendClassToElementFilter = new AppendClassToElementFilter();
    appendClassToElementFilter.setElementList(elementList);
    return appendClassToElementFilter;
  }

  /**
   * Filter a start element event.
   *
   * @param uri       The element's Namespace URI, or the empty string.
   * @param localName The element's local name, or the empty string.
   * @param qName     The element's qualified (prefixed) name, or the empty
   *                  string.
   * @param atts      The element's attributes.
   * @throws org.xml.sax.SAXException The client may throw
   *                                  an exception during processing.
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    AttributesImpl attributes = new AttributesImpl(atts);
    if (StringUtils.isNotEmpty(qName) && elementList.containsKey(qName)) {
      modifyAttributes(qName, attributes);
    }
    super.startElement(uri, localName, qName, attributes);
  }

  void modifyAttributes(String qName, AttributesImpl attributes) {
    StringBuilder className = new StringBuilder();
    int index = attributes.getIndex(ATTRIBUTE_CLASS);
    if (index > -1) {
      className.append(attributes.getValue(index)).append(" ").append(elementList.get(qName));
      attributes.removeAttribute(index);
    } else {
      className.append(elementList.get(qName));
    }
    attributes.addAttribute("", "", ATTRIBUTE_CLASS, "CDATA", className.toString());
  }

  public Map<String, String> getElementList() {
    return elementList;
  }

  @Required
  public void setElementList(Map<String, String> elementList) {
    this.elementList = elementList;
  }
}
