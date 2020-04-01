package com.coremedia.blueprint.common.datevalidation;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidityPeriodValidatorIsValidityPeriodUsedTest {

  private ValidityPeriodValidator testling;

  @BeforeEach
  void setUp() {
    testling = new ValidityPeriodValidator();
  }

  @ParameterizedTest
  @MethodSource("provideIsValidityPeriodUsedData")
  void testIsValidityPeriodUsed(@NonNull List<CMLinkable> linkables, boolean expected) {
    assertThat(testling.isValidityPeriodUsed(linkables)).isEqualTo(expected);
  }

  private static Stream<Arguments> provideIsValidityPeriodUsedData() {
    return Stream.of(
            Arguments.of(
                    newArrayList(),
                    false
            ),
            Arguments.of(
                    newArrayList(new Object()),
                    false
            ),
            Arguments.of(
                    newArrayList(createLinkable(false, false)),
                    false
            ),
            Arguments.of(
                    newArrayList(createLinkable(true, false)),
                    true
            ),
            Arguments.of(
                    newArrayList(createLinkable(false, true)),
                    true
            ),
            Arguments.of(
                    newArrayList(createLinkable(true, true)),
                    true
            )
    );
  }

  @NonNull
  private static CMLinkable createLinkable(boolean hasValidFrom, boolean hasValidTo) {
    CMLinkable linkable = mock(CMLinkable.class);

    if (hasValidFrom) {
      when(linkable.getValidFrom()).thenReturn(Calendar.getInstance());
    }

    if (hasValidTo) {
      when(linkable.getValidTo()).thenReturn(Calendar.getInstance());
    }

    return linkable;
  }
}
