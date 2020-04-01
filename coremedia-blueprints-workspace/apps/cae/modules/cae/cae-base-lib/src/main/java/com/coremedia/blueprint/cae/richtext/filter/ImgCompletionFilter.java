package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.id.IdProvider;
import com.coremedia.id.IdScheme;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.xml.Filter;
import com.coremedia.xml.Xlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImgCompletionFilter extends Filter implements FilterFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ImgCompletionFilter.class);

  private static final String IMG_ATTR_ALT = "alt";
  private static final String IMG_TAG = "img";

  private IdProvider idProvider;
  private ContentBeanFactory contentBeanFactory;


  // --- configuration ----------------------------------------------

  @Required
  public void setIdProvider(IdProvider idProvider) {
    this.idProvider = idProvider;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }


  // --- FilterFactory ----------------------------------------------

  @Override
  public Filter getInstance(HttpServletRequest request, HttpServletResponse response) {
    ImgCompletionFilter instance = new ImgCompletionFilter();
    instance.setIdProvider(idProvider);
    instance.setContentBeanFactory(contentBeanFactory);
    return instance;
  }


  // --- Filter -----------------------------------------------------

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (isA(uri, localName, qName, IMG_TAG)) {
      atts = addAlt(atts);
    }
    super.startElement(uri, localName, qName, atts);
  }


  // --- internal ---------------------------------------------------

  /**
   * Add missing alt attribute.
   */
  private Attributes addAlt(Attributes atts) {
    String alt = atts.getValue(IMG_ATTR_ALT);
    if (alt!=null && !alt.isEmpty()) {
      // There is an alt value already, do not change.
      return atts;
    }

    String blobId = atts.getValue(Xlink.NAMESPACE_URI, Xlink.HREF);
    alt = altValue(blobId);
    if (alt==null || alt.isEmpty()) {
      // Cannot derive an alt value from the target bean, do nothing.
      return atts;
    }

    AttributesImpl newAtts = new AttributesImpl(atts);
    // Attributes behaviour: addAttribute does not overwrite but append.
    // Must explicitely delete an existing entry.
    int altIndex = newAtts.getIndex(IMG_ATTR_ALT);
    if (altIndex>=0) {
      newAtts.removeAttribute(altIndex);
    }
    newAtts.addAttribute("", IMG_ATTR_ALT, IMG_ATTR_ALT, "CDATA", alt);
    return newAtts;
  }

  /**
   * Find a suitable alt value for the blob.
   */
  private String altValue(String blobId) {
    if (blobId == null || blobId.isEmpty()) {
      // Blob is empty, don't know how to get the alt value.
      return null;
    }

    if (!CMMedia.DATA.equals(IdHelper.parsePropertyFromBlobId(blobId))) {
      // Blob is no CMMedia.data, don't know how to get the alt value.
      return null;
    }
    String contentId = IdHelper.parseContentIdFromBlobId(blobId);
    Object content = getBean(contentId);
    if (!(content instanceof Content)) {
      LOG.error("Unexpected bean " + content + " for id " + contentId + ", expected a content.");
      // Clean code would throw a runtime exception here, we rather keep robust, though.
      return null;
    }
    ContentBean contentBean = contentBeanFactory.createBeanFor((Content) content, ContentBean.class);
    if (contentBean instanceof CMMedia) {
      return ((CMMedia)contentBean).getAlt();
    }
    // Blob is no CMMedia don't know how to get the alt value.
    return null;
  }

  private Object getBean(String id) {
    Object bean = idProvider.parseId(id);
    if (IdScheme.CANNOT_HANDLE.equals(bean) && IdScheme.DOES_NOT_EXIST.equals(bean)) {
      // should not happen since the editor should ensure valid xlinks
      throw new IllegalStateException("There is no bean with the id: " + id);
    }
    return bean;
  }

  /**
   * Convenient check whether the tag matches the localName or qName of a Sax event.
   */
  private static boolean isA(String uri, String localName, String qName, String tag) {
    return "".equals(uri) ? tag.equalsIgnoreCase(qName) : tag.equalsIgnoreCase(localName);
  }

}
