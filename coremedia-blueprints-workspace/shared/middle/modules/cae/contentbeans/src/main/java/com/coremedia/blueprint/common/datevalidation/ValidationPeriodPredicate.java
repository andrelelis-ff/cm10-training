package com.coremedia.blueprint.common.datevalidation;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Calendar;
import java.util.function.Predicate;

/**
 * This predicate is used to filter contents by validFrom and validTo fields.
 */
public class ValidationPeriodPredicate implements Predicate<ValidityPeriod> {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationPeriodPredicate.class);

  /**
   * This will contain the date and time to compare with.
   */
  private final Calendar now;

  /**
   * Constructor.
   *
   * @param date the date to use for comparison
   */
  public ValidationPeriodPredicate(@NonNull Calendar date) {
    now = date;
  }

  /**
   * Returns true if the input object matches this predicate.
   *
   * @param validityPeriod The object to check
   * @return <tt>true</tt> if the input object matches this predicate.
   */
  @Override
  public boolean test(@NonNull ValidityPeriod validityPeriod) {
    Calendar validFrom = validityPeriod.getValidFrom();
    Calendar validTo = validityPeriod.getValidTo();

    boolean result = (validFrom == null || validFrom.compareTo(now) <= 0)
            && (validTo == null || validTo.compareTo(now) > 0);

    logDetails(validFrom, validTo, result);

    return result;
  }

  private void logDetails(@Nullable Calendar validFrom, @Nullable Calendar validTo, boolean result) {
    if (LOG.isDebugEnabled()) {
      FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss.SSS");

      String validFromStr = (validFrom == null) ? "*not set*" : dateFormat.format(validFrom.getTime());
      String validToStr = (validTo == null) ? "*not set*" : dateFormat.format(validTo.getTime());
      String nowStr = dateFormat.format(now);
      String isNotValid = result ? "" : "not ";

      LOG.debug("Object with validity dates 'validFrom: {}' and 'validTo: {}' is {} valid at {}",
              validFromStr, validToStr, isNotValid, nowStr);
    }
  }
}
