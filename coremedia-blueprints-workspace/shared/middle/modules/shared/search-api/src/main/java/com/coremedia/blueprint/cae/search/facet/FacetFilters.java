package com.coremedia.blueprint.cae.search.facet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Static methods to format and parse filter strings for facet values.
 *
 * <p>Example filter string: "{@code <facetA>:<value1>,<value2>;<facetB>:<value3>}"
 *
 * @since 1810
 */
@DefaultAnnotation(NonNull.class)
public class FacetFilters {

  private static final char FACETS_SEPARATOR = ';';
  private static final char FACET_VALUES_SEPARATOR = ':';
  private static final char VALUES_SEPARATOR = ',';
  private static final char ESCAPE_CHAR = '\\';

  private static final String SPECIAL_CHARS = String.valueOf(new char[] {
    ESCAPE_CHAR, FACETS_SEPARATOR, FACET_VALUES_SEPARATOR, VALUES_SEPARATOR
  });

  private static final Pattern ESCAPE_PATTERN = Pattern.compile("([\\Q" + SPECIAL_CHARS + "\\E])");
  private static final String ESCAPE_REPLACEMENT = "\\" + ESCAPE_CHAR + "$1";

  private FacetFilters() {
  }

  /**
   * Creates a filter string for enabling filters for the given facet values.
   *
   * @param filters map of facets to facet values to filter search results
   * @return filter string
   */
  public static String format(Map<String, ? extends Collection<? extends FacetValue>> filters) {
    return filters.entrySet().stream()
      .filter(e -> !e.getValue().isEmpty())
      .map(e -> format(e.getKey(), e.getValue()))
      .collect(Collectors.joining(String.valueOf(FACETS_SEPARATOR)));
  }

  private static String format(String facetName, Collection<? extends FacetValue> facetFilters) {
    return facetFilters.stream()
      .map(FacetValue::getValue)
      .map(FacetFilters::escape)
      .collect(Collectors.joining(String.valueOf(VALUES_SEPARATOR), escape(facetName) + FACET_VALUES_SEPARATOR, ""));
  }

  private static String escape(String s) {
    return ESCAPE_PATTERN.matcher(s).replaceAll(ESCAPE_REPLACEMENT);
  }

  /**
   * Parses a string created by {@link #format(Map)} to facet filters.
   *
   * @param facetFilters filter string
   * @return map of facets to filters of facet values, empty map if given string is null
   */
  public static Map<String, List<String>> parse(@Nullable String facetFilters) {
    if (facetFilters == null) {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, List<String>> result = ImmutableMap.builder();
    for (String facetWithValues : split(facetFilters, FACETS_SEPARATOR)) {
      List<String> split = split(facetWithValues, FACET_VALUES_SEPARATOR, 2);
      if (split.size() == 2) {
        String facetPart = split.get(0);
        String valuePart = split.get(1);
        List<String> values = split(valuePart, VALUES_SEPARATOR).stream()
          .filter(s -> !s.isEmpty())
          .map(FacetFilters::unescape)
          .collect(Collectors.toList());
        if (!values.isEmpty()) {
          result.put(unescape(facetPart), ImmutableList.copyOf(values));
        }
      }
    }
    return result.build();
  }

  static List<String> split(String s, char separator) {
    return split(s, separator, Integer.MAX_VALUE);
  }
  static List<String> split(String s, char separator, int limit) {
    if (separator == ESCAPE_CHAR) {
      throw new IllegalArgumentException("must not use escape character " + ESCAPE_CHAR + " as separator");
    }
    ImmutableList.Builder<String> result = ImmutableList.builder();
    int start = 0;
    boolean escaped = false;
    int size = 1;
    for (int i = 0; i < s.length() && size < limit; i++) {
      char c = s.charAt(i);
      if (escaped) {
        escaped = false;
      } else if (c == ESCAPE_CHAR) {
        escaped = true;
      } else if (c == separator) {
        result.add(s.substring(start, i));
        start = i + 1;
        size++;
      }
    }
    if (start < s.length()) {
      result.add(s.substring(start));
    }
    return result.build();
  }

  private static String unescape(String s) {
    if (s.indexOf(ESCAPE_CHAR) < 0) {
      return s;
    }
    StringBuilder sb = new StringBuilder();
    boolean escaped = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!escaped && c == ESCAPE_CHAR) {
        escaped = true;
      } else {
        sb.append(c);
        escaped = false;
      }
    }
    return sb.toString();
  }

}
