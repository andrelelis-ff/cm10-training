package com.coremedia.livecontext.assets;

import com.coremedia.livecontext.asset.ProductAssetsHandler;
import com.coremedia.livecontext.ecommerce.catalog.AxisFilter;
import com.coremedia.livecontext.ecommerce.catalog.VariantFilter;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAssetsHandlerSSLTest {

  private final static List<VariantFilter> ONE_FILTER = ImmutableList.of(
          AxisFilter.on("a", "1"));

  private final static List<VariantFilter> THREE_FILTERS = ImmutableList.of(
          AxisFilter.on("a", "1"),
          AxisFilter.on("b", "2"),
          AxisFilter.on("c", "3"));

  @ParameterizedTest
  @MethodSource("provideTestData")
  void testParseAttributes(String given, List<VariantFilter> exspected) {
    List<VariantFilter> filters = ProductAssetsHandler.parseAttributesFromSSL(given);
    assertThat(filters).isEqualTo(exspected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"a;", "a;;b;;c;", "", "a", "a;;b;;c;;"})
  void testParseInvalidAttributes(String attributes) {
    List<VariantFilter> filters = ProductAssetsHandler.parseAttributesFromSSL(attributes);
    assertThat(filters).isEmpty();
  }

  private static Stream<Arguments> provideTestData() {
    return Stream.of(
            Arguments.of("a;1;b;2;c;3", THREE_FILTERS),
            Arguments.of("a;1", ONE_FILTER),
            Arguments.of("a;1;b;2;c;3;", THREE_FILTERS)
    );
  }
}