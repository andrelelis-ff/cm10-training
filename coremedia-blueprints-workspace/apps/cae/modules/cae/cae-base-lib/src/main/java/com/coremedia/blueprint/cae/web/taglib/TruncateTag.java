package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.objectserver.view.ViewException;
import com.coremedia.common.util.WordAbbreviator;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Helper to truncate a {@link com.coremedia.xml.Markup} or {@link java.lang.String} to a certain amount of characters.
 * For JSP Taglibs.
 * For Freemarker use {@link BlueprintFreemarkerFacade} instead.
 */
public class TruncateTag extends TagSupport {

  private static final long serialVersionUID = 2047875060529051489L;
  private static final Logger LOG = LoggerFactory.getLogger(TruncateTag.class);
  private static final int DEFAULT_LENGTH = 30;

  private Object value;
  private int maxLength = DEFAULT_LENGTH;
  private String var = null;

  /**
   * Sets the {@link com.coremedia.xml.Markup} or {@link java.lang.String} which will be truncated
   *
   * @param value the {@link com.coremedia.xml.Markup} or {@link java.lang.String} which will be truncated
   */
  public void setValue(Object value) {
    this.value = value;
  }

  public void setVar(String var) {
    this.var = var;
  }

  /**
   * Sets the amount of characters
   *
   * @param maxLength the amount of characters
   */
  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public int doEndTag() throws JspException {
    if (value != null) {
      String result;
      if (value instanceof String) {
        result = abbreviateString((String) value);
      } else if (value instanceof Markup) {
        result = abbreviateMarkup((Markup) value);
      } else {
        // should not happen
        LOG.error("Unknown object-type for abbreviation: " + value);
        throw new UnsupportedOperationException("Cannot abbreviate value " + value + " of Type" + value.getClass().toString());
      }
      writeResult(result);
    }
    return EVAL_PAGE;
  }

  private String abbreviateMarkup(Markup markup) {
    return abbreviateString(MarkupUtil.asPlainText(markup).trim());
  }

  private String abbreviateString(String value) {
    WordAbbreviator abbreviator = new WordAbbreviator();
    return abbreviator.abbreviateString(value, maxLength);
  }

  protected void writeResult(String result) {
    try {
      if (var == null) {
        pageContext.getOut().write(result);
      } else {
        pageContext.setAttribute(var, result);
      }
    } catch (IOException e) {
      throw new ViewException(e.getMessage(), e);
    }
  }
}
