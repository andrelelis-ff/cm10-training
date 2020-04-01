package com.coremedia.blueprint.common.util.pagination;

import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.coremedia.blueprint.common.util.pagination.StAXUtil.isOpeningDiv;
import static com.coremedia.blueprint.common.util.pagination.StAXUtil.isWhitespace;

class StAXPlainTextPaginator extends AbstractPaginator {
  private static final Logger LOG = LoggerFactory.getLogger(StAXPlainTextPaginator.class);

  private StringBuilder charactersBuffer = new StringBuilder();
  private XMLEvent divStart;

  @Override
  public int getCharacterCounter() {
    return charactersBuffer.length();
  }

  @Override
  protected List<Markup> splitInternally(XMLEventReader xmlReader, Markup markup) throws XMLStreamException {
    List<Markup> result = new ArrayList<>();
    while (xmlReader.hasNext()) {
      XMLEvent event = xmlReader.nextEvent();
      if (isOpeningDiv(event)) {
        divStart = event;
      } else if (event.isCharacters() && !isWhitespace(event)) {
        charactersBuffer.append(event.asCharacters().getData().trim()).append(" ");
      }
    }
    while (charactersBuffer.length() > 0) {
      Markup markupNode = buildMarkup();
      if (markupNode != null) {
        result.add(markupNode);
      }
    }
    return result;
  }

  private Markup buildMarkup() {
    int index = charactersBuffer.indexOf(" ", getPagingRule().getPagingUnitsNumber());
    if (index == -1) {
      index = charactersBuffer.length();
    }
    String writeCharacters = charactersBuffer.substring(0, index);

    charactersBuffer.delete(0, writeCharacters.length());
    String[] v = writeCharacters.split("\n");
    StringBuilder buffer = new StringBuilder();
    for (String aV : v) {
      buffer.append(aV.trim());
    }
    return buildMarkup(buffer.toString());
  }

  private Markup buildMarkup(String characters) {
    XMLEventWriter streamWriter = null;
    ByteArrayInputStream bytesInputStream = null;

    try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
      streamWriter = xmlOutputFactory.get().createXMLEventWriter(byteArrayStream);
      streamWriter.add(divStart);
      XMLEventFactory eventFactory = xmlEventFactory.get();
      streamWriter.add(eventFactory.createStartElement("", "", "p"));
      streamWriter.add(eventFactory.createCharacters(characters));
      streamWriter.add(eventFactory.createEndElement("", "", "p"));
      StartElement startElement = divStart.asStartElement();
      streamWriter.add(eventFactory.createEndElement(startElement.getName(), startElement.getNamespaces()));
      streamWriter.flush();
      byteArrayStream.flush();
      setBlockCounter(0);
      bytesInputStream = new ByteArrayInputStream(byteArrayStream.toByteArray());
      return MarkupFactory.fromInputStream(bytesInputStream);
    } catch (IOException e) {
      LOG.error("Error flushing writer or stream", e);
      return null;
    } catch (XMLStreamException e) {
      LOG.error("Error streaming xml", e);
      return null;
    } finally {
      IOUtils.closeQuietly(bytesInputStream);
      StAXUtil.closeQuietly(streamWriter);
    }
  }
}
