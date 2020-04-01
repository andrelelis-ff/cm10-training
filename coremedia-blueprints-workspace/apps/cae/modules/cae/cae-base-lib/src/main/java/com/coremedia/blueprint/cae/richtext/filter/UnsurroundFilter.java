package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Filter;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Removes surrounding divs from coremedia-richtext-1.0.
 */


public class UnsurroundFilter extends Filter implements FilterFactory {
  private int divCount = 0;

  @Override
  public UnsurroundFilter getInstance(HttpServletRequest request, HttpServletResponse response) {
    return new UnsurroundFilter();
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
    // Ignore first div
    if (!isDiv(qName) || divCount++ > 0) {
      super.startElement(uri, localName, qName, atts);
    }
  }

  /**
   * Filter an end element event.
   *
   * @param uri       The element's Namespace URI, or the empty string.
   * @param localName The element's local name, or the empty string.
   * @param qName     The element's qualified (prefixed) name, or the empty
   *                  string.
   * @throws org.xml.sax.SAXException The client may throw
   *                                  an exception during processing.
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // Ignore first div
    if (!isDiv(qName) || --divCount > 0) {
      super.endElement(uri, localName, qName);
    }
  }


  // --- internal ---------------------------------------------------

  private static boolean isDiv(String qName) {
    return StringUtils.isNotEmpty(qName) && qName.equals("div");
  }
}

