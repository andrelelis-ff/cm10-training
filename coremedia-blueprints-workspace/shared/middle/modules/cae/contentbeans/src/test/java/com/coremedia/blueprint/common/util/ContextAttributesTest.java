package com.coremedia.blueprint.common.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContextAttributesTest {

  @Test
  void testTypedSame() {
    String expected = "foo";
    Optional<String> actual = ContextAttributes.typed(expected, String.class);
    assertThat(actual).contains(expected);
  }

  @Test
  void testTypedSuper() {
    String expected = "foo";
    Optional<Object> actual = ContextAttributes.typed(expected, Object.class);
    assertThat(actual).contains(expected);
  }

  @Test
  void testTypedMismatch() {
    String unexpected = "foo";
    Optional<Integer> actual = ContextAttributes.typed(unexpected, Integer.class);
    assertThat(actual).isNotPresent();
  }
}
