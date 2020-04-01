package com.coremedia.blueprint.cae.web.taglib;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Helper Class do create unique ids for one request. Uses the current request to store the latest id in.
 * For JSP Taglibs.
 * For Freemarker use {@link BlueprintFreemarkerFacade} instead.
 */
public final class GenerateUniqueId extends TagSupport {

  public static final String ATTR_NAME_PREFIX = GenerateUniqueId.class.getName() + '.';
  public static final String ATTR_NAME_GENERATE_UNIQUE_ID = ATTR_NAME_PREFIX + "uniqueid";
  private static final long serialVersionUID = 8665946671472213187L;

  // static class
  private GenerateUniqueId() {
  }

  /**
   * Creates a unique id and stores the id as request attribute
   *
   * @param pageContext the current JSP page context
   * @return a unique id
   */
  public static String generateId(PageContext pageContext) {
    return generateId("", pageContext);
  }

  /**
   * Creates a unique id with the given Prefix and stores the id as request attribute
   *
   * @param prefix      the prefix to add
   * @param pageContext the current JSP page context
   * @return a unique id with an prefix
   */
  public static String generateId(String prefix, PageContext pageContext) {
    ServletRequest request = pageContext.getRequest();
    Integer id = (Integer) request.getAttribute(ATTR_NAME_GENERATE_UNIQUE_ID);
    id = (id == null) ? 1 : id + 1;
    request.setAttribute(ATTR_NAME_GENERATE_UNIQUE_ID, id);
    return prefix + id;
  }
}
