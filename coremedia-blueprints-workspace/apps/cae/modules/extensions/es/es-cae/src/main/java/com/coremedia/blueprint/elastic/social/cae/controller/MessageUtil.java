package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.Page;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;

@Named
public class MessageUtil {

  @Inject
  private SettingsService settingsService;

  public String getErrorMessage(String key, Page page) {
    return settingsService.setting(key, String.class, page);
  }

  public String getErrorMessage(String key, Page page, Object... args) {
    String message = getErrorMessage(key, page);
    MessageFormat messageFormat = new MessageFormat(message);
    return messageFormat.format(args);
  }
}
