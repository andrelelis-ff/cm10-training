package com.coremedia.blueprint.cae.richtext.filter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Stack;

/**
 * <p/>
 * This SAX filter replaces link text embedded in anchor or image tags with the
 * value of the a-tag's href-attribute. Use this filter if you have created
 * scripts (e.g. CSS, JavaScript) with your CMS and want managed links to be
 * properly rendered
 * </p>
 */
public class ScriptFilter extends XMLFilterImpl {

  protected static final String ANCHOR_TAG = "a";
  protected static final String IMAGE_TAG = "img";

  private Stack uriStack = new Stack();


  /**
   * Called by the SAX parser.
   *
   * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String qName,
                           Attributes atts) throws SAXException {

    String value = null;

    if (ANCHOR_TAG.equalsIgnoreCase(localName)) {
      value = atts.getValue("href");
    } else if (IMAGE_TAG.equalsIgnoreCase(localName)) {
      value = atts.getValue("src");
    }
    if (value != null) {
      uriStack.push(value);
    }

    super.startElement(uri, localName, qName, atts);
  }


  @Override
  public void characters(char[] ch, int start, int length)
          throws SAXException {

    if (this.uriStack.empty()) {
      super.characters(ch, start, length);
    }
  }


  /**
   * Called by the SAX parser.
   *
   * @see org.xml.sax.ContentHandler#endElement(String, String, String)
   */
  @Override
  public void endElement(String uri, String localName, String qName)
          throws SAXException {

    if (ANCHOR_TAG.equalsIgnoreCase(localName)
            || IMAGE_TAG.equalsIgnoreCase(localName)) {

      String theUri = (String) uriStack.pop();
      this.characters(theUri.toCharArray(), 0, theUri.length());
    }

    super.endElement(uri, localName, qName);
  }
}
