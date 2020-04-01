package com.coremedia.blueprint.cae.richtext.filter;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

class SaxElementData {
  private final String namespaceUri;
  private final String localName;
  private final String qName;
  private final Attributes atts;

  SaxElementData(String namespaceUri, String localName, String qName, Attributes atts) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.qName = qName;
    this.atts = new AttributesImpl(atts);
  }

  String getNamespaceUri() {
    return namespaceUri;
  }

  String getLocalName() {
    return localName;
  }

  String getqName() {
    return qName;
  }

  public Attributes getAtts() {
    return atts;
  }

  boolean isA(String tagName) {
    return tagName.equalsIgnoreCase(asTag());
  }

  String asTag() {
    return "".equals(namespaceUri) ? qName : localName;
  }
}
