package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.PathHelper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsJsonToMapAdapter {

  private static final String LINK_VALUE_PROPERTY_NAME = "$Link";

  private static final String CALENDAR_VALUE_PROPERTY_NAME = "$Date";

  private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
  private static final String[] PARSE_PATTERNS = {
          DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.getPattern(),
          DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern(),
          DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern()
  };

  private final ContentRepository contentRepository;

  public SettingsJsonToMapAdapter(@NonNull ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  /**
   * Get a map containing the parsed JSON.
   *
   * - any link is transformed into {@link com.coremedia.cap.content.Content} if the content is found
   * - any date is transformed into {@link java.util.Calendar} if the date is valid
   * - any number is transformed into {@link Integer}
   *
   * @param settingsJson The serialized JSON to parse.
   * @param basePath A base path to resolve relative links against. If null links will be resolved against root.
   * @return see description
   */
  @NonNull
  public Map<String, Object> getMap(@Nullable String settingsJson, String basePath) {
    Gson gson = new Gson();
    Map<String, Object> plainMap;
    try {
      plainMap = gson.fromJson(settingsJson, MAP_TYPE);
    } catch (JsonSyntaxException e) {
      plainMap = null;
    }
    if (plainMap == null) {
      return Collections.emptyMap();
    }
    Object substitute = substituteMap(plainMap, basePath);
    //noinspection unchecked
    return substitute instanceof Map ? (Map<String, Object>) substitute : Collections.emptyMap();
  }

  /**
   * Convenience method for {@link #getMap(String, String)} providing no base path.
   */
  @NonNull
  public Map<String, Object> getMap(@Nullable String settingsJson) {
    return getMap(settingsJson, null);
  }

  @Nullable
  private Object substitute(@NonNull Object value, String basePath) {
    if (value instanceof Map) {
      //noinspection unchecked
      value = substituteMap((Map<String, Object>)value, basePath);
    }
    if (value instanceof List) {
      value = ((List<?>)value).stream().map(item -> substitute(item, basePath)).collect(Collectors.toList());
    }
    if (value instanceof Number) {
      value = ((Number) value).intValue();
    }
    return value;
  }

  @Nullable
  private Object substituteMap(@NonNull Map<String, Object> map, String basePath) {
    if (map.containsKey(CALENDAR_VALUE_PROPERTY_NAME)) {
      Object value = map.get(CALENDAR_VALUE_PROPERTY_NAME);
      if (value instanceof String) {
        String date = (String) value;
        try {
          return DateUtils.toCalendar(DateUtils.parseDate(date, PARSE_PATTERNS));
        } catch (ParseException e) {
          // invalid date, remove value
          return null;
        }
      }
    }

    if (map.containsKey(LINK_VALUE_PROPERTY_NAME)) {
      Object value = map.get(LINK_VALUE_PROPERTY_NAME);
      if (value instanceof String) {
        String link = (String) value;
        if (!PathHelper.isAbsolute(link) && basePath != null) {
          link = PathHelper.join(basePath, link);
        }
        return contentRepository.getChild(link);
      }
    }

    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      result.put(entry.getKey(), substitute(entry.getValue(), basePath));
    }
    return result;
  }
}
