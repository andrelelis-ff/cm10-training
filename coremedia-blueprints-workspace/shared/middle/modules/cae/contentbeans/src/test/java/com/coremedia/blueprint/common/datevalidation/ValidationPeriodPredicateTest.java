package com.coremedia.blueprint.common.datevalidation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationPeriodPredicateTest {

  private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

  @ParameterizedTest
  @MethodSource("providePredicateData")
  void testPredicate(@NonNull Calendar now, @Nullable ValidityPeriod validityPeriod, boolean expected) {
    Predicate<ValidityPeriod> predicate = new ValidationPeriodPredicate(now);

    boolean actual = predicate.test(validityPeriod);

    assertThat(actual).isEqualTo(expected);
  }

  private static Stream<Arguments> providePredicateData() {
    return Stream.of(
            // Neither period start nor end are defined.

            // Now is valid because there is no limitation.
            Arguments.of(
                    createCalendar("2018-03-25 12:00:00"),
                    createValidityPeriodEmpty(),
                    true
            ),

            // Period start, but not end, is defined.

            // Now is *before* period.
            Arguments.of(
                    createCalendar("2018-03-25 11:59:59"),
                    createValidityPeriodFrom("2018-03-25 12:00:00"),
                    false
            ),
            // Now is *in* period.
            Arguments.of(
                    createCalendar("2018-03-25 12:00:00"),
                    createValidityPeriodFrom("2018-03-25 12:00:00"),
                    true
            ),

            // Period end, but not start, is defined.

            // Now is *in* period.
            Arguments.of(
                    createCalendar("2018-03-25 11:59:59"),
                    createValidityPeriodTo("2018-03-25 12:00:00"),
                    true
            ),
            // Now is *after* period.
            Arguments.of(
                    createCalendar("2018-03-25 12:00:00"),
                    createValidityPeriodTo("2018-03-25 12:00:00"),
                    false
            ),

            // Period start and end are defined.

            // Now is *before* period.
            Arguments.of(
                    createCalendar("2018-03-25 11:59:59"),
                    createValidityPeriodFromTo("2018-03-25 12:00:00", "2018-03-26 12:00:00"),
                    false
            ),
            // Now is *in* period (at the very beginning).
            Arguments.of(
                    createCalendar("2018-03-25 12:00:00"),
                    createValidityPeriodFromTo("2018-03-25 12:00:00", "2018-03-26 12:00:00"),
                    true
            ),
            // Now is *in* period (at the very end).
            Arguments.of(
                    createCalendar("2018-03-26 11:59:59"),
                    createValidityPeriodFromTo("2018-03-25 12:00:00", "2018-03-26 12:00:00"),
                    true
            ),
            // Now is *after* period.
            Arguments.of(
                    createCalendar("2018-03-26 12:00:00"),
                    createValidityPeriodFromTo("2018-03-25 12:00:00", "2018-03-26 12:00:00"),
                    false
            )
    );
  }

  @NonNull
  private static Calendar createCalendar(@NonNull String text) {
    String isoText = text.replace(' ', 'T');
    LocalDateTime localDateTime = LocalDateTime.parse(isoText, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZONE);

    return GregorianCalendar.from(zonedDateTime);
  }

  @NonNull
  private static ValidityPeriod createValidityPeriodEmpty() {
    return createValidityPeriod(
            null,
            null
    );
  }

  @NonNull
  private static ValidityPeriod createValidityPeriodFrom(@NonNull String validFromText) {
    return createValidityPeriod(
            createCalendar(validFromText),
            null
    );
  }

  @NonNull
  private static ValidityPeriod createValidityPeriodFromTo(@NonNull String validFromText, @NonNull String validToText) {
    return createValidityPeriod(
            createCalendar(validFromText),
            createCalendar(validToText)
    );
  }

  @NonNull
  private static ValidityPeriod createValidityPeriodTo(@NonNull String validToText) {
    return createValidityPeriod(
            null,
            createCalendar(validToText)
    );
  }

  @NonNull
  private static ValidityPeriod createValidityPeriod(@Nullable Calendar validFrom, @Nullable Calendar validTo) {
    ValidityPeriod validityPeriod = mock(ValidityPeriod.class);

    when(validityPeriod.getValidFrom()).thenReturn(validFrom);
    when(validityPeriod.getValidTo()).thenReturn(validTo);

    return validityPeriod;
  }
}
