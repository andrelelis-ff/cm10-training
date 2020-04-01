package com.coremedia.blueprint.common.datevalidation;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidityPeriodValidatorFindNearestDateTest {

  private ValidityPeriodValidator testling;

  @BeforeEach
  void setUp() {
    testling = new ValidityPeriodValidator();
  }

  @ParameterizedTest
  @MethodSource("provideFindNearestDateData")
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void testFindNearestDate(@NonNull List<CMLinkable> linkables, @NonNull Calendar validTime,
                           @NonNull Optional<Calendar> expected) {
    Optional<Calendar> actual = testling.findNearestDate(linkables, validTime);
    assertThat(actual).isEqualTo(expected);
  }

  private static Stream<Arguments> provideFindNearestDateData() {
    return Stream.of(
            Arguments.of(
                    newArrayList(),
                    createCalendar(2018, 3, 24),
                    Optional.empty()
            ),
            Arguments.of(
                    newArrayList(new Object()),
                    createCalendar(2018, 3, 24),
                    Optional.empty()
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.empty()
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(null, createCalendar(2018, 3, 25)),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.of(createCalendar(2018, 3, 25))
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(createCalendar(2018, 3, 25), null),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.of(createCalendar(2018, 3, 25))
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(createCalendar(2018, 3, 27), null),
                            createLinkable(createCalendar(2018, 3, 26), null),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.of(createCalendar(2018, 3, 26))
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(null, createCalendar(2018, 3, 27)),
                            createLinkable(null, createCalendar(2018, 3, 26)),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.of(createCalendar(2018, 3, 26))
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(createCalendar(2018, 3, 27), null),
                            createLinkable(null, createCalendar(2018, 3, 26)),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.of(createCalendar(2018, 3, 26))
            ),
            Arguments.of(
                    newArrayList(
                            createLinkable(null, null),
                            createLinkable(null, createCalendar(2018, 3, 27)),
                            createLinkable(createCalendar(2018, 3, 26), null),
                            createLinkable(null, null)
                    ),
                    createCalendar(2018, 3, 24),
                    Optional.of(createCalendar(2018, 3, 26))
            )
    );
  }

  @SuppressWarnings("SameParameterValue")
  @NonNull
  private static Calendar createCalendar(int year, int month, int dayOfMonth) {
    return new Calendar.Builder()
            .setDate(year, month, dayOfMonth)
            .build();
  }

  @NonNull
  private static CMLinkable createLinkable(@Nullable Calendar validFrom, @Nullable Calendar validTo) {
    CMLinkable linkable = mock(CMLinkable.class);

    if (validFrom != null) {
      when(linkable.getValidFrom()).thenReturn(validFrom);
    }

    if (validTo != null) {
      when(linkable.getValidTo()).thenReturn(validTo);
    }

    return linkable;
  }
}
