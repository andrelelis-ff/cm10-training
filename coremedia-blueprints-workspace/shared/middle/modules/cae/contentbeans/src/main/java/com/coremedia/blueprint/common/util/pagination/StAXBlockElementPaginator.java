package com.coremedia.blueprint.common.util.pagination;

import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.coremedia.blueprint.common.util.pagination.StAXUtil.elementIsA;
import static com.coremedia.blueprint.common.util.pagination.StAXUtil.elementName;
import static com.coremedia.blueprint.common.util.pagination.StAXUtil.getAttribute;
import static com.coremedia.blueprint.common.util.pagination.StAXUtil.isClosingDiv;
import static com.coremedia.blueprint.common.util.pagination.StAXUtil.isOpeningDiv;
import static com.coremedia.blueprint.common.util.pagination.StAXUtil.isWhitespace;

/**
 * StAX based Paginator. Empty blocks are ignored in block count but included in
 * Markup.
 */
class StAXBlockElementPaginator extends AbstractPaginator { // NOSONAR  cyclomatic complexity
  private static final Logger LOG = LoggerFactory.getLogger(StAXBlockElementPaginator.class);

  private DelimitingPagingRule delimitingPagingRule;

  private StringBuilder charactersBuffer = new StringBuilder();
  private XMLEvent divStart;

  @Override
  public int getCharacterCounter() {
    return charactersBuffer.length();
  }

  @Override
  public void setPagingRule(PagingRule pagingRule) {
    super.setPagingRule(pagingRule);
    if (pagingRule instanceof DelimitingPagingRule) {
      setDelimitingPagingRule((DelimitingPagingRule) pagingRule);
    }
  }

  public void setDelimitingPagingRule(DelimitingPagingRule rule) {
    delimitingPagingRule = rule;
  }

  @Override
  protected List<Markup> splitInternally(XMLEventReader xmlReader, Markup markup) throws XMLStreamException {
    List<Markup> result = new ArrayList<>();
    List<XMLEvent> blockElements = new ArrayList<>();
    int level = 0;
    XMLEvent currentEvent = null;
    XMLEvent previousEvent = null;

    while (xmlReader.hasNext()) {
      if (currentEvent != null && !isIgnorableEvent(currentEvent)) {
        previousEvent = currentEvent;
      }
      currentEvent = xmlReader.nextEvent();
      if (currentEvent.isStartDocument() ||
              currentEvent.isEndDocument() ||
              isWhitespace(currentEvent) ||
              isClosingDiv(currentEvent)) {
        continue;
      }
      if (isOpeningDiv(currentEvent)) {
        divStart = currentEvent;
        continue;
      }
      String currentBlockName = elementName(currentEvent);
      level += depth(currentEvent);

      if (level == 0 &&
              null != delimitingPagingRule &&
              null != currentBlockName &&
              !blockElements.isEmpty() &&
              delimitingPagingRule.matchesDelimiterTags(currentBlockName) &&
              currentEvent.isStartElement() &&
              delimitingPagingRule.matchesDelimiter(currentBlockName, getAttribute(currentEvent, "class"))) {
          addMarkupToResult(markup, blockElements, result);
      }

      if (currentEvent.isCharacters()) {
        charactersBuffer.append(currentEvent.asCharacters().getData());
      }

      blockElements.add(currentEvent);
      // check if we should count block
      if (shouldCountBlock(level, currentBlockName, previousEvent)) {
        setBlockCounter(getBlockCounter() + 1);
      }
      if (currentBlockName != null && getPagingRule().match(currentBlockName) && level == 0) {
        addMarkupToResult(markup, blockElements, result);
      }
    }
    if (containsNonEmptyBlock(blockElements) && !blockElements.isEmpty()) {
      addMarkupToResult(markup, blockElements, result);
    }
    return result;
  }

  private static int depth(XMLEvent event) {
    return event.isStartElement() ? 1 : event.isEndElement() ? -1 : 0;
  }

  private void addMarkupToResult(Markup markup, List<XMLEvent> blockElements, List<Markup> result) {
    Markup extractedMarkup = buildMarkUp(blockElements, markup.getGrammar());
    if (extractedMarkup != null) {
      result.add(extractedMarkup);
    }
  }

  private static boolean isIgnorableEvent(XMLEvent event) {
    return isWhitespace(event);
  }

  private static boolean containsNonEmptyBlock(List<XMLEvent> blockElements) {
    XMLEvent currentEvent = null;
    XMLEvent previousEvent = null;
    for (XMLEvent blockElement : blockElements) {
      previousEvent = currentEvent;
      currentEvent = blockElement;
      String currentBlockName = null;
      if (currentEvent.isEndElement()) {
        currentBlockName = currentEvent.asEndElement().getName().getLocalPart();
      }
      if (previousEvent == null) {
        continue;
      }
      if (previousEvent.isStartElement() && !elementIsA(previousEvent, currentBlockName)) {
        return true;
      }
    }
    return false;
  }

  // only count non-empty top level blocks
  private static boolean shouldCountBlock(int level, String currentBlockName, XMLEvent previousEvent) {
    if (level!=0 || previousEvent==null) {
      return false;
    }
    boolean isEmptyBlock = previousEvent.isStartElement() && elementIsA(previousEvent, currentBlockName);
    return !isEmptyBlock;
  }

  private Markup buildMarkUp(List<XMLEvent> blockElements, String grammar) {
    ByteArrayInputStream bytesInputStream = null;
    XMLEventWriter streamWriter = null;
    try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
      streamWriter = xmlOutputFactory.get().createXMLEventWriter(byteArrayStream, "UTF-8");
      streamWriter.add(divStart);
      for (XMLEvent xmlEvent : blockElements) {
        streamWriter.add(xmlEvent);
      }
      streamWriter.add(xmlEventFactory.get().createEndElement(divStart.asStartElement().getName(), divStart
              .asStartElement().getNamespaces()));
      streamWriter.flush();
      byteArrayStream.flush();
      blockElements.clear();
      setBlockCounter(0);
      charactersBuffer.delete(0, charactersBuffer.length());
      bytesInputStream = new ByteArrayInputStream(byteArrayStream.toByteArray());
      Markup result = MarkupFactory.fromInputStream(bytesInputStream);
      result = result.withGrammar(grammar);
      return result;
    } catch (IOException e) {
      LOG.error("Error flushing  streamwriter", e);
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
