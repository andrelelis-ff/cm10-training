package com.coremedia.blueprint.common.util.pagination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

class StAXUtil {
  private static final Logger LOG = LoggerFactory.getLogger(StAXUtil.class);

  private static final String DIV = "div";

  // static utility class
  private StAXUtil() {}

  public static String elementName(XMLEvent event) {
    if (event.isStartElement()) {
      return event.asStartElement().getName().getLocalPart();
    }
    if (event.isEndElement()) {
      return event.asEndElement().getName().getLocalPart();
    }
    return null;
  }

  public static boolean elementIsA(XMLEvent event, String name) {
    return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(name) ||
            event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(name);
  }

  public static boolean isOpeningDiv(XMLEvent event) {
    return event.isStartElement() && DIV.equals(event.asStartElement().getName().getLocalPart());
  }

  public static boolean isClosingDiv(XMLEvent event) {
    return event.isEndElement() && DIV.equals(event.asEndElement().getName().getLocalPart());
  }

  public static boolean isWhitespace(XMLEvent event) {
    return event.isCharacters() && event.asCharacters().isWhiteSpace();
  }

  public static String getAttribute(XMLEvent event, String attributeName) {
    if (!event.isStartElement()) {
      throw new IllegalArgumentException("Not a startElement: " + event);
    }
    Attribute attribute = event.asStartElement().getAttributeByName(new QName(attributeName));
    return attribute==null ? null : attribute.getValue();
  }

  public static void closeQuietly(XMLEventWriter streamWriter) {
    if (streamWriter != null) {
      try {
        streamWriter.close();
      } catch (Exception e) {
        LOG.error("Closing stream " + streamWriter + " has failed!", e);
      }
    }
  }
}
