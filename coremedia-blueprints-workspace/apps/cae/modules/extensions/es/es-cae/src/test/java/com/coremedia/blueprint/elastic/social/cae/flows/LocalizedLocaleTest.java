package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalizedLocaleTest {

  @Test
  public void testDisplayLanguage() {
    String displayLanguage = "Englisch";
    LocalizedLocale localizedLocale = new LocalizedLocale();
    assertNull(localizedLocale.getDisplayLanguage());
    localizedLocale.setDisplayLanguage(displayLanguage);
    assertEquals(displayLanguage, localizedLocale.getDisplayLanguage());
  }

  @Test
  public void testLanguageCode() {
    String languageCode = "en";
    LocalizedLocale localizedLocale = new LocalizedLocale();
    assertNull(localizedLocale.getDisplayLanguage());
    localizedLocale.setDisplayLanguage(languageCode);
    assertEquals(languageCode, localizedLocale.getDisplayLanguage());
  }

  @Test
  public void testConversion() {
    String displayLanguage = "Englisch";
    Locale locale = Locale.ENGLISH;
    LocalizedLocale localizedLocale = new LocalizedLocale(locale, displayLanguage);
    String localizedLocaleAsString = localizedLocale.toString();
    LocalizedLocale newLocalizedLocale = new LocalizedLocale(localizedLocaleAsString);
    assertEquals(displayLanguage, localizedLocale.getDisplayLanguage());
    assertEquals(locale, localizedLocale.getLocale());
    assertEquals(displayLanguage, newLocalizedLocale.getDisplayLanguage());
    assertEquals(locale, newLocalizedLocale.getLocale());
  }

  @Test
  public void testCountryConversion() {
    String displayLanguage = "Englisch";
    Locale locale = Locale.US;
    LocalizedLocale localizedLocale = new LocalizedLocale(locale, displayLanguage);
    String localizedLocaleAsString = localizedLocale.toString();
    LocalizedLocale newLocalizedLocale = new LocalizedLocale(localizedLocaleAsString);
    assertEquals(displayLanguage, localizedLocale.getDisplayLanguage());
    assertEquals(locale, localizedLocale.getLocale());
    assertEquals(displayLanguage, newLocalizedLocale.getDisplayLanguage());
    assertEquals(locale, newLocalizedLocale.getLocale());
  }

}
