package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.id.IdProvider;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.xml.Filter;
import com.coremedia.xml.Xlink;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal links that have the show attribute set to 'embed' will be rendered
 * through the ViewDispatcher, with the view 'asRichtextEmbed'.
 * <p>
 * If you use this class, make sure that the
 * {@link #hasBlockLevel(String tag, org.xml.sax.Attributes)} method matches your
 * project templates.
 */
public class LinkEmbedFilter extends PDivUntanglingFilter {
  private static final Logger LOG = LoggerFactory.getLogger(LinkEmbedFilter.class);

  private static final String LINK_EMBED_ROLE = "linkEmbedRole";
  private static final String LINK_EMBED_CLASS_NAMES = "linkEmbedClassNames";

  private static final String A_ELEMENT_NAME = "a";

  private IdProvider idProvider;
  private DataViewFactory dataViewFactory;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private String defaultViewName = "asRichtextEmbed";

  /**
   * Maps Xlink:show values to view names that are used for the embedded view
   */
  private Map<String, String> mappings;


  // --- PDivUntanglingFilter ---------------------------------------

  @Override
  protected boolean mustEmbed(String tag, Attributes atts) {
    return A_ELEMENT_NAME.equalsIgnoreCase(tag) && mustEmbedLink(atts);
  }

  /**
   * This implementation matches the Blueprint's default asRichtextEmbed
   * templates.  If you change the asRichtextEmbed template family, you must
   * adjust this method accordingly.
   */
  @Override
  protected boolean hasBlockLevel(String tag, Attributes atts) {
    return true;
  }

  @Override
  protected void renderEmbeddedData(String tag, Attributes atts) {
    String role = atts.getValue(Xlink.NAMESPACE_URI, Xlink.ROLE);
    String classNames = atts.getValue("class");
    Object bean = getBean(atts.getValue(Xlink.NAMESPACE_URI, Xlink.HREF));
    String view = mappings.get(atts.getValue(Xlink.NAMESPACE_URI, Xlink.SHOW));

    setTemporaryRequestAttribute(LINK_EMBED_ROLE, role);
    setTemporaryRequestAttribute(LINK_EMBED_CLASS_NAMES, classNames);
    ViewUtils.render(bean, view, this, request, response);
    removeTempraryRequestAttribute(LINK_EMBED_ROLE, role);
    removeTempraryRequestAttribute(LINK_EMBED_CLASS_NAMES, classNames);
  }


  // --- Factory ----------------------------------------------------

  /**
   * Factory Method
   *
   * @return initialized instance of {@link LinkEmbedFilter}
   */
  @Override
  public Filter getInstance(HttpServletRequest request, HttpServletResponse response) {
    LinkEmbedFilter lef = new LinkEmbedFilter();
    lef.setRequest(request);
    lef.setResponse(response);
    lef.setIdProvider(idProvider);
    lef.setDataViewFactory(dataViewFactory);

    //if no mappings are found, initialize with default.
    if (mappings == null) {
      mappings = new HashMap<>();
      mappings.put(Xlink.SHOW_EMBED, defaultViewName);
    }
    lef.setMappings(mappings);

    return lef;
  }


  // --- Configuration ----------------------------------------------

  @Required
  public void setIdProvider(IdProvider idProvider) {
    this.idProvider = idProvider;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  public void setDefaultViewName(String defaultViewName) {
    this.defaultViewName = defaultViewName;
  }

  public void setMappings(Map<String, String> mappings) {
    this.mappings = mappings;
  }


  // --- internal ---------------------------------------------------

  private void setTemporaryRequestAttribute(String key, String value) {
    if (!StringUtils.isBlank(value)) {
      request.setAttribute(key, value);
    }
  }

  private void removeTempraryRequestAttribute(String key, String value) {
    if (!StringUtils.isBlank(value)) {
      request.removeAttribute(key);
    }
  }

  /**
   * Retrieves the bean from the given id.
   *
   * @param id - the bean's identifier
   * @return the bean or <code>null</code> if no bean with the given id could be found
   */
  private Object getBean(String id) {
    Object bean = idProvider.parseId(id);
    if (bean instanceof IdProvider.UnknownId) {
      // Regular case for external links, id is the foreign URL.
      // Should not happen for internal links,
      // indicates invalid content, misconfigured id schemes, ... .
      LOG.debug("There is no bean with the id {}", id);
      return null;
    }
    return dataViewFactory.loadCached(bean, null);
  }

  private boolean mustEmbedLink(Attributes atts) {
    String show = atts.getValue(Xlink.NAMESPACE_URI, Xlink.SHOW);
    if (!mappings.containsKey(show)) {
      return false;
    }
    String href = atts.getValue(Xlink.NAMESPACE_URI, Xlink.HREF);
    Object bean = getBean(href);
    if (bean!=null) {
      return true;
    } else {
      LOG.warn("{} does not denote a CMS bean, so it cannot be rendered embedded.", href);
      return false;
    }
  }
}

