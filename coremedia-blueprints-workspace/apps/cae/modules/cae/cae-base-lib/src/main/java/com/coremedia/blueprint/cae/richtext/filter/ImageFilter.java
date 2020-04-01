package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.xml.Filter;
import com.coremedia.xml.Xlink;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Delegates the rendering of embedded pictures to a JSP template
 * <p>
 * In case of an IMG tag, resolve the content reference and render the image.
 */
public class ImageFilter extends PDivUntanglingFilter implements FilterFactory {
  private static final String CLASS = "class";
  private static final String HEIGHT = "height";
  private static final String WIDTH = "width";
  private static final String ALT = "alt";

  private static final String ATT_ALT = "att_alt";
  private static final String ATT_CLASS = "att_class";
  private static final String ATT_HEIGHT = "att_height";
  private static final String ATT_WIDTH = "att_width";
  private static final String ATT_ROLE = "att_role";
  private static final String ATT_TITLE = "att_title";

  private static final String VIEW_NAME = "asRichtextEmbed";

  private static final String IMG_ELEMENT_NAME = "img";

  private DataViewFactory dataViewFactory;
  private ContentRepository contentRepository;
  private ContentBeanFactory contentBeanFactory;
  private ValidationService<Object> validationService;

  private HttpServletRequest request;
  private HttpServletResponse response;


  // --- FilterFactory ----------------------------------------------

  @Override
  public Filter getInstance(HttpServletRequest request, HttpServletResponse response) {
    ImageFilter i = new ImageFilter();
    i.setRequest(request);
    i.setResponse(response);
    i.setDataViewFactory(dataViewFactory);
    i.setContentRepository(contentRepository);
    i.setContentBeanFactory(contentBeanFactory);
    i.setValidationService(validationService);
    return i;
  }


  // --- PDivUntanglingFilter ---------------------------------------

  @Override
  protected boolean mustEmbed(String tag, Attributes atts) {
    return IMG_ELEMENT_NAME.equalsIgnoreCase(tag);
  }

  @Override
  protected boolean hasBlockLevel(String tag, Attributes atts) {
    return true;
  }

  @Override
  protected void renderEmbeddedData(String tag, Attributes atts) {
    processImage(atts, request, response);
  }


  // --- internal ---------------------------------------------------

  private void processImage(Attributes atts, HttpServletRequest request, HttpServletResponse response) {
    String contentId = atts.getValue(Xlink.NAMESPACE_URI, Xlink.HREF);
    Content content = contentRepository.getContent(IdHelper.parseContentIdFromBlobId(contentId));
    ContentBean picture = contentBeanFactory.createBeanFor(content, ContentBean.class);
    picture = dataViewFactory.loadCached(picture, null);
    if (validationService==null || validationService.validate(picture)) {
      renderPicture(picture, atts, request, response);
    }
  }

  private void renderPicture(ContentBean picture, Attributes atts, HttpServletRequest request, HttpServletResponse response) {
    // Transfer attributes to request
    String title = Xlink.getTitle(atts);
    String role = Xlink.getRole(atts);
    String alt = atts.getValue(ALT);
    String sClass = atts.getValue(CLASS);

    Integer height = null;
    if (StringUtils.isNumeric(atts.getValue(HEIGHT))) {
      height = Integer.decode(atts.getValue(HEIGHT));
    }

    Integer width = null;
    if (StringUtils.isNumeric(atts.getValue(WIDTH))) {
      width = Integer.decode(atts.getValue(WIDTH));
    }

    Map<String, Object> toBeRestoredAttributes = new HashMap<>();
    try {
      saveAttribute(ATT_TITLE, title, request, toBeRestoredAttributes);
      saveAttribute(ATT_ROLE, role, request, toBeRestoredAttributes);
      saveAttribute(ATT_ALT, alt, request, toBeRestoredAttributes);
      saveAttribute(ATT_CLASS, sClass, request, toBeRestoredAttributes);
      saveAttribute(ATT_HEIGHT, height, request, toBeRestoredAttributes);
      saveAttribute(ATT_WIDTH, width, request, toBeRestoredAttributes);

      ViewUtils.render(picture, VIEW_NAME, this, request, response);
    } finally {
      restoreAttributes(request, toBeRestoredAttributes);
    }
  }

  private static void saveAttribute(String key, Object value, HttpServletRequest request, Map<String, Object> toBeRestored) {
    toBeRestored.put(key, request.getAttribute(key));
    request.setAttribute(key, value);
  }

  private static void restoreAttributes(HttpServletRequest request, Map<String, Object> toBeRestored) {
    for (Map.Entry<String, Object> attribute : toBeRestored.entrySet()) {
      request.setAttribute(attribute.getKey(), attribute.getValue());
    }
  }


  // --- configuration ----------------------------------------------

  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  public void setValidationService(ValidationService<Object> validationService) {
    this.validationService = validationService;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }
}

