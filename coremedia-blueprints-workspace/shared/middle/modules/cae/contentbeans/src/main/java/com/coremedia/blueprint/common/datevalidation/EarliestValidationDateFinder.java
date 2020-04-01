package com.coremedia.blueprint.common.datevalidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

/**
 * Find the earliest date after a reference date from the {@code validTo} and {@code validFrom} properties of a content.
 */
class EarliestValidationDateFinder {

  private static final Logger LOG = LoggerFactory.getLogger(EarliestValidationDateFinder.class);

  private EarliestValidationDateFinder() {
  }

  /**
   * Return the earliest date (`validFrom` or `validTo`) defined by the validity period after `now`.
   *
   * @return a {@link java.util.Calendar} object, or nothing if no valid date could be determined
   */
  @NonNull
  static Optional<Calendar> findNextDate(@NonNull Calendar now, @NonNull ValidityPeriod validityPeriod) {
    Calendar chosenDate = calculateDateForPeriod(now, validityPeriod.getValidFrom(), validityPeriod.getValidTo());
    logChosenDate(chosenDate);
    return Optional.ofNullable(chosenDate);
  }

  private static void logChosenDate(@Nullable Calendar chosenDate) {
    if (!LOG.isDebugEnabled()) {
      return;
    }

    String chosenDateStr = (chosenDate != null)
            ? new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").format(chosenDate.getTime())
            : "*null*";

    LOG.debug("ComponentToEarliestValidationDateTransformer.chosenDate: {}", chosenDateStr);
  }

  @Nullable
  private static Calendar calculateDateForPeriod(@NonNull Calendar now, @Nullable Calendar validFrom,
                                                 @Nullable Calendar validTo) {
    // now compare the two dates with each other and the almighty comparison date "now"
    if ((validFrom != null) && (validTo == null) && validFrom.after(now)) {
      return clone(validFrom);
    } else if ((validFrom == null) && (validTo != null) && validTo.after(now)) {
      return clone(validTo);
    } else if ((validFrom != null) && (validTo != null)) {
      return retrieveDate(now, validFrom, validTo);
    } else {
      return null;
    }
  }

  @Nullable
  private static Calendar retrieveDate(@NonNull Calendar now, @NonNull Calendar validFrom, @NonNull Calendar validTo) {
    if (validFrom.after(now) && validTo.after(now)) {
      return validFrom.before(validTo) ? clone(validFrom) : clone(validTo);
    } else if (validFrom.after(now) && !validTo.after(now)) {
      return clone(validFrom);
    } else if (!validFrom.after(now) && validTo.after(now)) {
      return clone(validTo);
    } else {
      return null;
    }
  }

  @NonNull
  private static Calendar clone(@NonNull Calendar calendar) {
    return (Calendar) calendar.clone();
  }
}
