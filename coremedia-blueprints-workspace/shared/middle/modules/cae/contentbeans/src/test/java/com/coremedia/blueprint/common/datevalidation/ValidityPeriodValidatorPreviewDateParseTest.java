package com.coremedia.blueprint.common.datevalidation;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ValidityPeriodValidatorPreviewDateParseTest {

  private static final ZoneId ZONE_ID_BERLIN = ZoneId.of("Europe/Berlin");
  private static final ZoneId ZONE_ID_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private static final ZoneId ZONE_ID_US_PACIFIC = ZoneId.of("US/Pacific");

  @ParameterizedTest
  @MethodSource("provideParsePreviewDateFromRequestParameterData")
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void parsePreviewDateFromRequestParameter(@NonNull String previewDateText,
                                            @NonNull Optional<ZonedDateTime> expectedZonedDateTime) {
    Optional<Calendar> actual = ValidityPeriodValidator.parsePreviewDateFromRequestParameter(previewDateText);
    Optional<ZonedDateTime> actualZonedDateTime = actual
            .map(ValidityPeriodValidatorPreviewDateParseTest::toZonedDateTime);
    assertThat(actualZonedDateTime).isEqualTo(expectedZonedDateTime);
  }

  private static Stream<Arguments> provideParsePreviewDateFromRequestParameterData() {
    return Stream.of(
            Arguments.of(
                    "bad timestamp",
                    Optional.empty()
            ),
            Arguments.of(
                    "30-10-2016 23:11 America/Los_Angeles",
                    Optional.of(zonedDateTime(2016, Month.OCTOBER, 30, 23, 11, ZONE_ID_LOS_ANGELES))
            ),
            Arguments.of(
                    "02-07-2014 17:57 US/Pacific",
                    Optional.of(zonedDateTime(2014, Month.JULY, 2, 17, 57, ZONE_ID_US_PACIFIC))
            ),
            Arguments.of(
                    "26-03-2017 10:54 Europe/Berlin",
                    Optional.of(zonedDateTime(2017, Month.MARCH, 26, 10, 54, ZONE_ID_BERLIN))
            )
    );
  }

  @NonNull
  private static ZonedDateTime zonedDateTime(int year, @NonNull Month month, int dayOfMonth, int hour, int minute,
                                             @NonNull ZoneId zoneId) {
    int second = 0;
    int nanoOfSecond = 0;

    LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);

    return ZonedDateTime.of(localDateTime, zoneId);
  }

  @NonNull
  private static ZonedDateTime toZonedDateTime(@NonNull Calendar calendar) {
    return ((GregorianCalendar) calendar).toZonedDateTime();
  }
}
