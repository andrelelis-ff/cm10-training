package com.coremedia.blueprint.studio.rest.intercept.word;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;


/**
 * ContentHandlerDecorator for extracting the content title property from a paragraph.
 */
public class DocTitleHandler extends ContentHandlerDecorator implements IDocumentEntryResolver {

  public static final String CLASS_ATTRIBUTE = "class";

  private boolean titleMode = false;
  private String title = null;
  private String defaultTitle = null;
  private List<String> titleTags = new ArrayList<>();

  public DocTitleHandler(String defaultTitle) {
    this.defaultTitle = defaultTitle;

    titleTags.add("p-titel");
    titleTags.add("p-title");
    titleTags.add("p-header");
    titleTags.add("h1-titel");
    titleTags.add("h1-title");
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    String lastStyle = (atts.getValue(CLASS_ATTRIBUTE) != null) ? "-" + atts.getValue(CLASS_ATTRIBUTE) : "";
    String tagAndStyle = (localName + lastStyle).toLowerCase();

    if (titleTags.contains(tagAndStyle)) {
      titleMode = true;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    String s = new String(ch);

    if (StringUtils.isEmpty(title) && titleMode && !StringUtils.isEmpty(s)) {
      title = s;
    }
  }

  @Override
  public void endElement(String uri, String localName, String name) throws SAXException {
    this.titleMode = false;
  }


  @Override
  public void startDocument() throws SAXException {
  }


  @Override
  public void endDocument() throws SAXException {
  }

  @Override
  public DocumentEntry getDocumentEntry() {
    return new DocumentEntry("title", getTitle());
  }

  private String getTitle() {
    if(StringUtils.isEmpty(title)) {
      return defaultTitle;
    }
    return title;
  }
}
