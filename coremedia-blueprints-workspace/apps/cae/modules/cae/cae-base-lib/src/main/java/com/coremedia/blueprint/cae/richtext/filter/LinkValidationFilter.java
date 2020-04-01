package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.id.IdProvider;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.xml.Filter;
import com.coremedia.xml.Xlink;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A {@link Filter} that suppresses links to invalid content.
 *
 * <p>Content validity is checked with {@link ValidationService#validate}.
 */
public class LinkValidationFilter extends Filter implements FilterFactory {
  private IdProvider idProvider;
  private ValidationService<Object> validationService;
  private DataViewFactory dataViewFactory;
  private boolean renderLinkText = true;

  private boolean omittingA;


  // --- configure --------------------------------------------------

  @Required
  public void setIdProvider(IdProvider idProvider) {
    this.idProvider = idProvider;
  }

  @Required
  public void setValidationService(ValidationService<Object> validationService) {
    this.validationService = validationService;
  }

  /**
   * configuration switch if the link text to an filtered out content shall be shown
   * @param renderLinkText (set true if link text shall be shown, default is true)
   */
  public void setRenderLinkText(boolean renderLinkText) {
    this.renderLinkText = renderLinkText;
  }

  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  protected IdProvider getIdProvider() {
    return idProvider;
  }

  protected ValidationService<Object> getValidationService() {
    return validationService;
  }

  protected DataViewFactory getDataViewFactory() {
    return dataViewFactory;
  }

  // --- FilterFactory ----------------------------------------------

  @Override
  public Filter getInstance(HttpServletRequest request, HttpServletResponse response) {
    LinkValidationFilter instance = new LinkValidationFilter();
    instance.setDataViewFactory(dataViewFactory);
    instance.setIdProvider(idProvider);
    instance.setValidationService(validationService);
    instance.setRenderLinkText(renderLinkText);
    return instance;
  }


  // --- Filter -----------------------------------------------------

  @Override
  public void startDocument() throws SAXException {
    omittingA = false;
    super.startDocument();
  }

  @Override
  public void endDocument() throws SAXException {
    if (omittingA) {
      // Kind of cannot happen, probably indicates a bug in this class or
      // in a preceding filter.
      throw new SAXException("Mismatching <a> tags");
    }
    super.endDocument();
  }

  @Override
  public void startElement(String namespaceUri, String localName, String qName, Attributes atts) throws SAXException {
    if (isA(namespaceUri, localName, qName, "a")) {
      if (isValid(atts)) {
        super.startElement(namespaceUri, localName, qName, atts);
      } else {
        omittingA = true;
      }
    } else {
      super.startElement(namespaceUri, localName, qName, atts);
    }
  }

  @Override
  public void endElement(String namespaceUri, String localName, String qName) throws SAXException {
    if (isA(namespaceUri, localName, qName, "a")) {
      if (!omittingA) {
        super.endElement(namespaceUri, localName, qName);
      } else {
        omittingA = false;
      }
    } else {
      super.endElement(namespaceUri, localName, qName);
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (!omittingA || renderLinkText) {
      super.characters(ch, start, length);
    }
  }

  // --- internal ---------------------------------------------------

  protected boolean isValid(Attributes atts) {
    Object bean = fetchBean(atts);
    return bean==null || validationService.validate(bean);
  }

  protected Object fetchBean(Attributes atts) {
    String id = atts.getValue(Xlink.NAMESPACE_URI, Xlink.HREF);
    Object bean = idProvider.parseId(id);
    if (bean instanceof IdProvider.UnknownId) {
      // Probably an external link, not our business
      return null;
    }
    return dataViewFactory==null ? bean : dataViewFactory.loadCached(bean, null);
  }

  /**
   * Convenient check whether the tag matches the localName or qName of a Sax event.
   */
  private static boolean isA(String uri, String localName, String qName, String tag) {
    return "".equals(uri) ? tag.equalsIgnoreCase(qName) : tag.equalsIgnoreCase(localName);
  }

}
