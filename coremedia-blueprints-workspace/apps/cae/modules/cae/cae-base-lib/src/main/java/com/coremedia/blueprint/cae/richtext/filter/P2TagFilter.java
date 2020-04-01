package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Filter;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Maps paragraphs with specified css classes to accordant html elements, e.g. h1, h2 etc.
 * <p/>
 * Mapping defines a mapping 'from css class of p tag' -> 'to tag', e.g.
 * <p/>
 * www-p--h1 -> h3
 * www-p--h2 -> h4
 * <p/>
 * This class is XHTML compliant. It will render tags like <p/> instead of <p></p>
 */
public class P2TagFilter extends Filter implements FilterFactory {
  private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

  private int count = 0;
  private Map<String, String> mapping;
  private String currentTag;

  @Override
  public P2TagFilter getInstance(HttpServletRequest request, HttpServletResponse response) {
    P2TagFilter f = new P2TagFilter();
    f.setMapping(mapping);
    return f;
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
    // Only do something if qName is a paragraph
    if (StringUtils.isNotEmpty(qName) && qName.equals("p")) {
      // If a count is set, that means, we're currently rendering a heading
      // and we'll not allow further paragraphs in it
      if (count > 0) {
        count++;
        return;
      }

      // Check if attributes are set and define the style of a heading
      if (atts.getLength() > 0) {
        String style = atts.getValue("class");
        if (style != null) {
          // Check if a heading is set
          for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String hClass = entry.getKey();
            if (style.contains(hClass)) {
              count++;
              currentTag = hClass;

              //set additional class if alignment was set
              String[] styles = style.split(" ");
              if(styles.length > 1) {
                AttributesImpl attributes = new AttributesImpl();
                String styleString = StringUtils.remove(style, hClass);
                attributes.addAttribute("", "", "class", "CDATA", styleString.trim());
                super.startElement(uri, localName, entry.getValue(), attributes);
              }
              else {
                super.startElement(uri, localName, entry.getValue(), EMPTY_ATTRIBUTES);
              }

              return;
            }
          }
        }
      }
    }

    // If no paragraph is found, do default
    super.startElement(uri, localName, qName, atts);
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
    // Only do something if qName is a paragraph
    if (StringUtils.isNotEmpty(qName) && qName.equals("p")) {
      // We're not currently rendering a heading, so we do the default
      if (count == 0) {
        super.endElement(uri, localName, qName);
        return;
      }
      // We're at the first paragraph, so let's close the heading
      if (count == 1) {
        super.endElement(uri, localName, mapping.get(currentTag));
        count = 0;
        currentTag = null;
        return;
      } else {
        // Ignore end element, but decrease count
        count--;
        return;
      }
    }
    super.endElement(uri, localName, qName);
  }

  public void setMapping(Map<String, String> mapping) {
    this.mapping = mapping;
  }
}

