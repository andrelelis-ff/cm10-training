package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.elastic.core.api.templates.TemplateException;
import com.coremedia.elastic.core.api.templates.TemplateService;
import com.coremedia.elastic.core.api.templates.TemplateTokenException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;

/**
 * Generated extension class for beans of document type "CMMail".
 */
public class CMMailImpl extends CMMailBase implements CMMail {
  public static final String MAIL_TEMPLATE_TOKENS_STRUCT = "mailTemplate_tokens";
  public static final String PREFIX = "mailTemplate_token_";
  public static final String LOCALE_DELIMITER = "_";
  private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

  private TemplateService templateService;

  public void setTemplateService(TemplateService templateService) {
    this.templateService = templateService;
  }

  @Override
  public String getContentType() {
    String contentType = getContent().getString("contentType");
    if (StringUtils.isBlank(contentType)) {
      return CONTENT_TYPE_TEXT_PLAIN;
    }
    return contentType;
  }

  @Override
  public String getName() {
    String name = getContent().getName();
    int pos = name.indexOf(LOCALE_DELIMITER);
    return pos < 0 ? name : name.substring(0, pos);
  }

  @Override
  public String getExampleText() {
    CMContext context = getCurrentContextService().getContext();
    Map<String, Object> knownTokens = getSettingsService().settingAsMap(MAIL_TEMPLATE_TOKENS_STRUCT, String.class, Object.class, context);
    Map<String, Object> filteredTokens = new HashMap<>();

    for (Map.Entry<String, Object> entry : knownTokens.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith(PREFIX)) {
        filteredTokens.put(key.substring(PREFIX.length()), entry.getValue());
      }
    }
    return escapeXml(templateService.renderTemplate(getText(), filteredTokens)).replace("\n", "<br>");
  }

  @Override
  public String getErrorToken() {
    try {
      getExampleText();
    } catch (TemplateTokenException e) {
      return e.getToken();
    }
    return null;
  }

  public TemplateError getTemplateError() {
    TemplateError templateError = null;
    try {
      getExampleText();
    } catch (TemplateTokenException e) {
      templateError = new TemplateError("mailTemplate.tokenError.title", "mailTemplate.tokenError", e.getToken());
    } catch (TemplateException e) {
      templateError = new TemplateError("mailTemplate.syntaxError.title", "mailTemplate.syntaxError", e.getMessage());
    }
    return templateError;
  }
}
