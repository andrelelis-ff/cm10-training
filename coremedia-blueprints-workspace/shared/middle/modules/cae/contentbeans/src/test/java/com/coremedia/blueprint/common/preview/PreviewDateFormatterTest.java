package com.coremedia.blueprint.common.preview;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DefaultAnnotation(NonNull.class)
class PreviewDateFormatterTest {

  private static final ZoneId ZONE_ID_BERLIN = ZoneId.of("Europe/Berlin");
  private static final ZoneId ZONE_ID_LOS_ANGELES = ZoneId.of("America/Los_Angeles");

  @ParameterizedTest
  @MethodSource("provideFormatData")
  void format(ZonedDateTime input, String expected) {
    assertThat(PreviewDateFormatter.format(input)).isEqualTo(expected);
  }

  private static Stream<Arguments> provideFormatData() {
    return Stream.of(
            Arguments.of(
                    zonedDateTime(2017, Month.MARCH, 26, 10, 57, 0, ZONE_ID_BERLIN),
                    "26-03-2017 10:57 Europe/Berlin"
            ),
            Arguments.of(
                    zonedDateTime(2016, Month.OCTOBER, 30, 23, 11, 0, ZONE_ID_LOS_ANGELES),
                    "30-10-2016 23:11 America/Los_Angeles"
            )
    );
  }

  @ParameterizedTest
  @MethodSource("provideParseData")
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void parse(String input, Optional<ZonedDateTime> expected) {
    assertThat(PreviewDateFormatter.parse(input)).isEqualTo(expected);
  }

  private static Stream<Arguments> provideParseData() {
    return Stream.of(
            Arguments.of(
                    "26-03-2017 10:57 Europe/Berlin",
                    Optional.of(zonedDateTime(2017, Month.MARCH, 26, 10, 57, 0, ZONE_ID_BERLIN))
            ),
            Arguments.of(
                    "30-10-2016 23:11 America/Los_Angeles",
                    Optional.of(zonedDateTime(2016, Month.OCTOBER, 30, 23, 11, 0, ZONE_ID_LOS_ANGELES))
            ),
            Arguments.of(
                    "",
                    Optional.empty()
            ),
            Arguments.of(
                    "10-30-2016 23:11 America/Los_Angeles", // Day and month switched, month `30` is invalid.
                    Optional.empty()
            ),
            Arguments.of(
                    "30-10-2016 23:11:99 America/Los_Angeles", // Contains seconds, thus invalid.
                    Optional.empty()
            )
    );
  }

  @Test
  void formatParseEquality() {
    String text = "26-03-2017 10:57 Europe/Berlin";
    ZonedDateTime zonedDateTime = PreviewDateFormatter.parse(text).get();
    assertThat(PreviewDateFormatter.format(zonedDateTime)).isEqualTo(text);
  }

  private static ZonedDateTime zonedDateTime(int year, Month month, int dayOfMonth, int hour, int minute, int second,
                                             ZoneId zoneId) {
    int nanoOfSecond = 0;
    LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    return ZonedDateTime.of(localDateTime, zoneId);
  }
}
