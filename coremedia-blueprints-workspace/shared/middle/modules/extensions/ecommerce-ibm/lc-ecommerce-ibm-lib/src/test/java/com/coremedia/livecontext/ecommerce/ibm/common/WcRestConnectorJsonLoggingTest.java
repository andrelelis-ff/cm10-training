package com.coremedia.livecontext.ecommerce.ibm.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WcRestConnectorJsonLoggingTest {

  @ParameterizedTest
  @MethodSource("formatJsonForLoggingProvider")
  void formatJsonForLogging(String input, String expected) {
    String actual = WcRestConnector.formatJsonForLogging(input);
    assertThat(actual).isEqualTo(expected);
  }

  private static Stream<Arguments> formatJsonForLoggingProvider() {
    return Stream.of(
            Arguments.of(
                    "{\"logonId\":\"coremedia\",\"logonPassword\":\"thepassword\"}",
                    "{\"logonId\":\"coremedia\",\"logonPassword\":\"***\"}"
            ),
            Arguments.of(
                    // whitespace before and after delimiter
                    "{\"logonId\":\"coremedia\",\"logonPassword\" : \"thepassword\"}",
                    "{\"logonId\":\"coremedia\",\"logonPassword\":\"***\"}"
            ),
            Arguments.of(
                    // newlines before and after delimiter
                    "{\"logonId\":\"coremedia\",\"logonPassword\" : \"thepassword\"}",
                    "{\"logonId\":\"coremedia\",\"logonPassword\":\"***\"}"
            ),
            Arguments.of(
                    // non-greediness with more JSON elements after the password
                    "{\"logonId\":\"coremedia\",\"logonPassword\":\"thepassword\"},\"foo\":\"bar\"}",
                    "{\"logonId\":\"coremedia\",\"logonPassword\":\"***\"},\"foo\":\"bar\"}"
            )
    );
  }
}
