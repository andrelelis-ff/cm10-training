package com.coremedia.blueprint.elastic.social.contentbeans;

public class TemplateError {

  private final String titleKey;

  private final String messageKey;

  private final Object[] params;

  public TemplateError(String titleKey, String messageKey, Object... params) {
    this.messageKey = messageKey;
    this.titleKey = titleKey;
    this.params = params;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public Object[] getParams() {
    return params;
  }

  public String getTitleKey() {
    return titleKey;
  }
}
