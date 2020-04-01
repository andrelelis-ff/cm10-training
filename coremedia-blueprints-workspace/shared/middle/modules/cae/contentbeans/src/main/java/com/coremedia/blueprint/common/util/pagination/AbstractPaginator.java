package com.coremedia.blueprint.common.util.pagination;

import com.coremedia.xml.Markup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

abstract class AbstractPaginator implements Paginator {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPaginator.class);

  protected ThreadLocal<XMLInputFactory> xmlInputFactory = new ThreadLocal<XMLInputFactory>() {
    @Override
    protected XMLInputFactory initialValue() {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      return factory;
    }
  };
  protected ThreadLocal<XMLOutputFactory> xmlOutputFactory = new ThreadLocal<XMLOutputFactory>() {
    @Override
    protected XMLOutputFactory initialValue() {
      return XMLOutputFactory.newInstance();
    }
  };
  protected ThreadLocal<XMLEventFactory> xmlEventFactory = new ThreadLocal<XMLEventFactory>() {
    @Override
    protected XMLEventFactory initialValue() {
      return XMLEventFactory.newInstance();
    }
  };


  private int blockCounter = 0;
  private PagingRule pagingRule;

  @Override
  public int getBlockCounter() {
    return blockCounter;
  }

  public void setBlockCounter(int blockCounter) {
    this.blockCounter = blockCounter;
  }

  @Override
  public void setPagingRule(PagingRule pagingRule) {
    this.pagingRule = pagingRule;
    this.pagingRule.setPaginator(this);
  }

  @Override
  public PagingRule getPagingRule() {
    return pagingRule;
  }

  @Override
  public List<Markup> split(Markup markup) {
    if (markup == null) {
      return Collections.emptyList();
    }
    XMLEventReader xmlReader = null;
    try (InputStream inputStream = new ByteArrayInputStream(markup.asXml().getBytes(StandardCharsets.UTF_8))) {
      xmlReader = createXmlEventReader(inputStream);
      return splitInternally(xmlReader, markup);
    } catch (Exception e) {
      LOG.error("Error streaming xml", e);
      return Collections.emptyList();
    } finally {
      closeQuietly(xmlReader);
    }
  }

  @SuppressWarnings("findsecbugs:XXE_XMLSTREAMREADER") // XMLInputFactory is created without external entities support
  private XMLEventReader createXmlEventReader(InputStream inputStream) throws XMLStreamException {
    return xmlInputFactory.get().createXMLEventReader(inputStream);
  }

  private static void closeQuietly(XMLEventReader xmlReader) {
    if (xmlReader != null) {
      try {
        xmlReader.close();
      } catch (Exception e) {
        LOG.error("Cannot close XMLEventReader, this may indicate a resource leak.", e);
      }
    }
  }

  protected abstract List<Markup> splitInternally(XMLEventReader xmlReader, Markup markup) throws XMLStreamException;
}
