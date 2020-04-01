package com.coremedia.blueprint.elastic.social.cae.flows;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.Serializable;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class LocalizedLocale implements Serializable {
  private static final long serialVersionUID = 42L;
  private String displayLanguage;
  private Locale locale;

  public LocalizedLocale() {
    // empty constructor
  }

  public LocalizedLocale(String localizedLocale) {
    if (isNotBlank(localizedLocale)) {
      String[] localeItems = StringUtils.split(localizedLocale, '_');
      if (localeItems.length > 0) {
        displayLanguage = localeItems[0];
        StringBuilder builder = new StringBuilder();
        if (localeItems.length > 1) {
          builder.append(localeItems[1]);
        }
        if (localeItems.length > 2) {
          builder.append("-").append(localeItems[2]);
        }
        if (localeItems.length > 3) {
          builder.append("-").append(localeItems[3]);
        }
        locale = Locale.forLanguageTag(builder.toString());
      }
    }
  }

  public LocalizedLocale(Locale locale, String displayLanguage) {
    this.locale = locale;
    this.displayLanguage = displayLanguage;
  }

  public String getDisplayLanguage() {
    return displayLanguage;
  }

  public void setDisplayLanguage(String displayLanguage) {
    this.displayLanguage = displayLanguage;
  }


  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(displayLanguage)) {
      builder.append(displayLanguage);
    }
    if (this.locale != null) {
      appendNonBlank(builder, locale.getLanguage(), locale.getCountry(), locale.getVariant());
    }
    return builder.toString();
  }

  public Locale getLocale() {
    return locale;
  }

  private static void appendNonBlank(@NonNull StringBuilder builder, @NonNull String... values) {
    for (String value: values) {
      if (StringUtils.isNotBlank(value)) {
        builder.append("_").append(value);
      }
    }
  }
}
