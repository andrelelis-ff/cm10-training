package com.coremedia.blueprint.taxonomies.semantic;

import com.coremedia.xml.MarkupUtil;
import com.coremedia.cap.common.NoSuchPropertyDescriptorException;
import com.coremedia.cap.content.Content;
import com.coremedia.xml.Markup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class ContentSerializer {

  /**
   * Hide Utility Class Constructor
   */
  private ContentSerializer() {
  }

  private static final Logger LOG = LoggerFactory.getLogger(ContentSerializer.class);

  public static String serialize(Content content, List<String> documentProperties) {
    StringBuilder stringBuilder = new StringBuilder();

    for (String documentProperty : documentProperties) {
      try {
        Object propertyValue = content.get(documentProperty);
        if (propertyValue instanceof Markup) {
          propertyValue = MarkupUtil.asPlainText((Markup) propertyValue);
        }
        if (isNotBlank((String) propertyValue)) {
          stringBuilder.append(propertyValue);
          stringBuilder.append(" ");
        }
      } catch (NoSuchPropertyDescriptorException nspe) {
        LOG.debug(format("Could not extract document property %s because it does not exist.", documentProperty));
      }
    }
    return stringBuilder.toString();
  }

  public static List<String> tokenizeContent(String data, int tokenSize) {
    String dataString = data;
    List<String> tokenized = new ArrayList<>();
    while (dataString.length() > 0) {
      String token = "";
      if (dataString.length() > tokenSize) {
        token = dataString.substring(0, tokenSize);
        if (token.indexOf('.') != -1) {
          token = token.substring(0, token.lastIndexOf('.') + 1);
        } else if (token.indexOf('!') != -1) {
          token = token.substring(0, token.lastIndexOf('!') + 1);
        } else if (token.indexOf('?') != -1) {
          token = token.substring(0, token.lastIndexOf('?') + 1);
        } else if (token.indexOf(' ') != -1) {
          token = token.substring(0, token.lastIndexOf(' ') + 1);
        }
      } else {
        token = dataString;
      }
      dataString = dataString.substring(token.length(), dataString.length());
      tokenized.add(token);
    }
    return tokenized;
  }
}
