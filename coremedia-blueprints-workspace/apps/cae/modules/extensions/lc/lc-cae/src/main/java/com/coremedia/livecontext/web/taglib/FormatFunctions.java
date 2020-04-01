package com.coremedia.livecontext.web.taglib;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * This class contains static helper functions used in livecontext templates to format various information.
 */
public class FormatFunctions {

  public FormatFunctions() {}

  /**
   * Formats a given price
   *
   * @param amount The numeric part of the price
   * @param currency The currency of the price
   * @param locale The locale to be used
   * @return formatted price
   */
  public static String formatPrice(Object amount, Currency currency, Locale locale) {
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
    currencyFormatter.setCurrency(currency);
    return currencyFormatter.format(amount);
  }
}
