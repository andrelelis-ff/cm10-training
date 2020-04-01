package com.coremedia.blueprint.cae.web.i18n;

import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A message source that is based on a fixed {@link ResourceBundle} instance. This implementation
 * does not provide internal caching.
 */
class FixedResourceBundleMessageSource extends AbstractMessageSource {

  private final ResourceBundle bundle;

  public FixedResourceBundleMessageSource(ResourceBundle bundle) {
    this.bundle = bundle;
  }


  @Override
  protected MessageFormat resolveCode(String code, Locale locale) {

    try {
      String msg = bundle.getString(code);
      return createMessageFormat(msg, locale);
    }
    catch (MissingResourceException e) {
      // not found
      return null;
    }
  }
}
