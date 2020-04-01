package com.coremedia.livecontext.handler;

import com.coremedia.livecontext.fragment.links.context.Context;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FragmentCommerceContextInterceptorTimestampParsingTest {

  private static final ZoneId ZONE_ID_BERLIN = ZoneId.of("Europe/Berlin");
  private static final ZoneId ZONE_ID_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private static final ZoneId ZONE_ID_US_PACIFIC = ZoneId.of("US/Pacific");

  private FragmentCommerceContextInterceptor testling;

  @BeforeEach
  void setUp() {
    testling = new FragmentCommerceContextInterceptor();
  }

  @ParameterizedTest
  @MethodSource("provideCreatePreviewDateData")
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void testCreatePreviewDate(@NonNull Context fragmentContext, @NonNull Optional<ZonedDateTime> expected) {
    Optional<ZonedDateTime> actual = testling.createPreviewDate(fragmentContext);
    assertThat(actual).isEqualTo(expected);
  }

  private static Stream<Arguments> provideCreatePreviewDateData() {
    return Stream.of(
            Arguments.of(
                    createContext("bad timestamp", "Europe/Berlin"),
                    Optional.empty()
            ),
            Arguments.of(
                    createContext("2016-10-30 23:11:00.0", "America/Los_Angeles"),
                    Optional.of(zonedDateTime(2016, Month.OCTOBER, 30, 23, 11, 0, ZONE_ID_LOS_ANGELES))
            ),
            Arguments.of(
                    createContext("2014-07-02 17:57:00.0", "US/Pacific"),
                    Optional.of(zonedDateTime(2014, Month.JULY, 2, 17, 57, 0, ZONE_ID_US_PACIFIC))
            ),
            Arguments.of(
                    createContext("2017-03-26 10:54:00.0", "Europe/Berlin"),
                    Optional.of(zonedDateTime(2017, Month.MARCH, 26, 10, 54, 0, ZONE_ID_BERLIN))
            )
    );
  }

  @NonNull
  private static Context createContext(@NonNull String timestamp, @Nullable String timeZone) {
    Context context = mock(Context.class);

    when(context.get("wc.preview.timestamp")).thenReturn(timestamp);
    when(context.get("wc.preview.timezone")).thenReturn(timeZone);

    return context;
  }

  @NonNull
  private static ZonedDateTime zonedDateTime(int year, @NonNull Month month, int dayOfMonth, int hour, int minute,
                                             int second, @NonNull ZoneId zoneId) {
    int nanoOfSecond = 0;
    LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    return ZonedDateTime.of(localDateTime, zoneId);
  }
}
