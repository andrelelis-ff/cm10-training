package com.coremedia.blueprint.studio.rest.intercept.word;

import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class DocContentHandler extends ContentHandlerDecorator implements IDocumentEntryResolver {
  public static final String CLASS_ATTRIBUTE = "class";

  private static final String RICHTEXT_START = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
  private static final String RICHTEXT_END = "</div>";

  private StringBuilder markup = new StringBuilder();
  private Stack<String> openedTags = new Stack<>();
  private Map<String, String> xhtmlToRichtext = new HashMap<>();

  private List<String> ignoreTags = Arrays.asList("html", "head", "meta", "title", "body", "img", "s");

  public DocContentHandler() {
    // mapping of xhtml elements to Richtext elements
    // values are stored comma separated in the format: "tag-name,class-attribute"
    xhtmlToRichtext.put("b", "strong");
    xhtmlToRichtext.put("i", "em");
    xhtmlToRichtext.put("h1", "p,p--heading-1");
    xhtmlToRichtext.put("h1-title", "p,p--heading-1");
    xhtmlToRichtext.put("h1-titel", "p,p--heading-1");
    xhtmlToRichtext.put("h2", "p,p--heading-2");
    xhtmlToRichtext.put("h2-subtitle", "p,p--heading-2");
    xhtmlToRichtext.put("h3", "p,p--heading-3");
    xhtmlToRichtext.put("h4", "p,p--heading-4");
    xhtmlToRichtext.put("h5", "p,p--heading-5");
    xhtmlToRichtext.put("h6", "p,p--heading-6");
    xhtmlToRichtext.put("p", "p");
    xhtmlToRichtext.put("table", "table");
    xhtmlToRichtext.put("tbody", "tbody");
    xhtmlToRichtext.put("tr", "tr");
    xhtmlToRichtext.put("td", "td");
    xhtmlToRichtext.put("a", "p");
    xhtmlToRichtext.put("li", "li");
    xhtmlToRichtext.put("div", "p");
    xhtmlToRichtext.put("p-titel", "p");
    xhtmlToRichtext.put("p-title", "p");
    xhtmlToRichtext.put("p-header", "p");
    xhtmlToRichtext.put("p-untertitel", "p");
    xhtmlToRichtext.put("p-standard", "p");
    xhtmlToRichtext.put("p-list_paragraph", "p");
  }

  @Override
  public DocumentEntry getDocumentEntry() {
    return new DocumentEntry("detailText", getMarkup());
  }

  private Markup getMarkup() {
    return MarkupFactory.fromString(markup.toString());
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    String tag = localName.toLowerCase();
    if(ignoreTags.contains(tag)) {
      return;
    }

    String lastStyle = (atts.getValue(CLASS_ATTRIBUTE) != null) ? "-" + atts.getValue(CLASS_ATTRIBUTE) : "";
    String tagAndStyle = (tag + lastStyle).toLowerCase();

    //paragraph fixing: remove the class information if this has not been mapped and reduce it to the tag name
    if(!xhtmlToRichtext.containsKey(tagAndStyle)) {
      tagAndStyle = tag;
    }

    if (xhtmlToRichtext.containsKey(tagAndStyle)) {
      String mappedTag = xhtmlToRichtext.get(tagAndStyle);
      String mappedPlainTag = mappedTag.split(",")[0];

      if(!openedTags.isEmpty()) {
        //check for nested p tags
        String lastTag = openedTags.peek();
        if(lastTag.equals("p") && mappedPlainTag.equals("p")) {
          mappedTag = "";
          mappedPlainTag = "";
        }
        if(lastTag.equals("strong") && mappedPlainTag.equals("p")) {
          mappedTag = "";
          mappedPlainTag = "";
        }
      }

      markup.append(generateRichTextStartTag(mappedTag));
      openedTags.push(mappedPlainTag);
    }
    else {
      //ensure that all elements are mapped
      throw new SAXException("Unmapped document element found '" + tagAndStyle + "'");
    }
  }


  @Override
  public void endElement(String uri, String localName, String name) throws SAXException {
    String tag = localName.toLowerCase();
    if(ignoreTags.contains(tag)) {
      return;
    }

    String lastOpenedTag = openedTags.pop();
    markup.append(generateRichTextEndTag(lastOpenedTag));
  }


  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    String s = new String(ch);

    //ignore text nodes until we start generating richtext elements
    if(!markup.toString().equals(RICHTEXT_START)) {
      String xml = cleanInvalidXmlChars(s, "");
      xml = xml.replaceAll("&", "&amp;");
      xml = xml.replaceAll("<", "&lt;");
      xml = xml.replaceAll(">", "&gt;");
      markup.append(xml);
    }
  }


  @Override
  public void startDocument() throws SAXException {
    markup.append(RICHTEXT_START);
  }


  @Override
  public void endDocument() throws SAXException {
    markup.append(RICHTEXT_END);
  }


  private String generateRichTextStartTag(String mappedTag) {
    if(mappedTag.length() > 0) {
      String[] split = mappedTag.split(",");
      String tag = split[0];

      String classAtrributeString = "";
      if (split.length > 1) {
        classAtrributeString = " class=\"" + split[1] + "\"";
      }

      return "<" + tag + classAtrributeString + ">";
    }
    return "";
  }

  private String generateRichTextEndTag(String tag) {
    if(tag.length() > 0) {
      return "</" + tag + ">";
    }
    return "";
  }

  /**
   * Helper to remove all invalid characters from XML text elemnts
   * @param text the parsed element text
   * @param replacement the string to replace the invalid characters with
   */
  private static String cleanInvalidXmlChars(String text, String replacement) {
    // XML 1.0
    // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    String xml10pattern = "[^"
            + "\u0009\r\n"
            + "\u0020-\uD7FF"
            + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff"
            + "]";
    text = text.replaceAll("\u000B", "");
    text = text.replaceAll("\\n", "");
    return text.replaceAll(xml10pattern, replacement);
  }

}
