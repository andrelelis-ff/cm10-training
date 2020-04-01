package com.coremedia.livecontext.ecommerce.ibm.common;

import com.google.common.base.Splitter;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is meant to simplify access to values deep inside nested lists
 * and maps without having to worry about {@code NullPointerException}s while
 * providing automatic type conversion.
 */
@DefaultAnnotation(NonNull.class)
public class DataMapHelper {

  private static final Splitter KEY_PATH_SPLITTER = Splitter.on('.');
  private static final Pattern KEY_INDEX_PATTERN = Pattern.compile("(.+)\\[(\\d+)\\]");

  private static final ConversionService DEFAULT_CONVERSION_SERVICE = new DefaultConversionService();

  private DataMapHelper() {
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public static Object getValueForPath(Map<String, Object> map, String path) {
    Object value = null;

    Map<String, Object> myMap = map;

    List<String> keys = KEY_PATH_SPLITTER.splitToList(path);

    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get(i);
      if (i < keys.size() - 1 && !matchIndexPattern(key).matches()) {
        value = getValueForKey(myMap, key);
        if (value instanceof Map) {
          myMap = (Map<String, Object>) value;
        } else {
          return null;
        }
      } else {
        Matcher matcher = matchIndexPattern(key);
        if (matcher.matches()) { // a list entry is expected
          String keyWithoutIndex = matcher.group(1);
          int index = Integer.parseInt(matcher.group(2));

          List tmpList = getList(myMap, keyWithoutIndex);
          if (tmpList.size() > index) {
            value = tmpList.get(index);
            if (value instanceof Map) {
              myMap = (Map<String, Object>) value;
            }
          } else {
            return null;
          }
        } else {
          // for the last key ...
          value = myMap.get(key);
        }
      }
    }

    return value;
  }

  private static Matcher matchIndexPattern(CharSequence key) {
    return KEY_INDEX_PATTERN.matcher(key);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public static Object getValueForKey(Map<String, Object> map, String key) {
    Object value = map.get(key);

    if (value instanceof List) {
      List<Map<String, Object>> list = (List<Map<String, Object>>) value;
      if (!list.isEmpty()) {
        return list.get(0);
      }
    }

    return value;
  }

  public static <T> Optional<T> findValue(Map<String, Object> map, String key, Class<T> type) {
    Object value = getValueForPath(map, key);

    return Optional.ofNullable(value)
            .map(v -> DEFAULT_CONVERSION_SERVICE.convert(v, type));
  }

  /**
   * Return the string at that key, or nothing if the key is not found or the value is null.
   */
  public static Optional<String> findString(Map<String, Object> map, String key) {
    return findValue(map, key, String.class);
  }

  /**
   * Return the list at that key, or an empty list if the key is not found or the value is null.
   */
  public static List getList(Map<String, Object> map, String key) {
    return findValue(map, key, List.class)
            .orElseGet(Collections::emptyList);
  }

  /**
   * Return the map at that key, or an empty map if the key is not found or the value is null.
   */
  public static Map getMap(Map<String, Object> map, String key) {
    return findValue(map, key, Map.class)
            .orElseGet(Collections::emptyMap);
  }
}
