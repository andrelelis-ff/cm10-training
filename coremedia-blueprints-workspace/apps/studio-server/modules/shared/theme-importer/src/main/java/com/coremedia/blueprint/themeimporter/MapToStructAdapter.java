package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapToStructAdapter {

  private final StructService structService;
  private final ContentRepository contentRepository;

  private StructBuilder structBuilder;

  public MapToStructAdapter(StructService structService, ContentRepository contentRepository) {
    this.structService = structService;
    this.contentRepository = contentRepository;
  }

  public Struct getStruct(@NonNull Map<String, ?> json) {
    structBuilder = structService.createStructBuilder();
    parseObject(json);
    return structBuilder.build();
  }

  private ObjectType getObjectType(@NonNull Object object) {
    if (object instanceof String) {
      return ObjectType.STRING;
    } else if (object instanceof Integer) {
      return ObjectType.INTEGER;
    } else if (object instanceof Boolean) {
      return ObjectType.BOOLEAN;
    } else if (object instanceof List) {
      return ObjectType.ARRAY;
    } else if (object instanceof Content) {
      return ObjectType.LINK;
    } else if (object instanceof Calendar) {
      return ObjectType.DATE;
    } else if (object instanceof Map) {
      return ObjectType.OBJECT;
    }
    return null;
  }

  private ObjectType getListItemType(@NonNull List<?> list) {
    Set<ObjectType> itemTypes = list.stream().map(this::getObjectType).collect(Collectors.toSet());
    return itemTypes.size() == 1 ? itemTypes.iterator().next() : null;
  }

  private void parseObject(@NonNull Map<String, ?> properties) {
    properties.forEach(this::parseProperty);
  }

  private void parseList(@NonNull String key, List<?> items) {
    ObjectType listItemType = getListItemType(items);
    if (listItemType != null) {
      switch (listItemType) {
        case STRING:
          structBuilder.declareStrings(key, Integer.MAX_VALUE, Collections.emptyList());
          break;
        case INTEGER:
          structBuilder.declareIntegers(key, Collections.emptyList());
          break;
        case BOOLEAN:
          structBuilder.declareBooleans(key, Collections.emptyList());
          break;
        case LINK:
          structBuilder.declareLinks(key, contentRepository.getContentContentType(), Collections.emptyList());
          break;
        case DATE:
          structBuilder.declareDates(key, Collections.emptyList());
          break;
        case OBJECT:
        default:
          structBuilder.declareStructs(key, Collections.emptyList());
      }

      if (listItemType != ObjectType.OBJECT) {
        items.forEach(item -> structBuilder.add(key, item));
      } else {
        int position = 0;
        for (Object item : items) {
          @SuppressWarnings({"unchecked"}) Map<String, ?> mapItem = (Map<String, ?>) item;
          structBuilder.enter(key, position);
          parseObject(mapItem);
          structBuilder.up();
          position++;
        }
      }
    }
  }

  private void parseProperty(@NonNull String key, @NonNull Object value) {
    ObjectType objectType = getObjectType(value);
    if (objectType != null) {
      switch (objectType) {
        case STRING:
          String stringValue = (String) value;
          structBuilder.declareString(key, Integer.MAX_VALUE, stringValue);
          break;
        case INTEGER:
          Integer integerValue = (Integer) value;
          structBuilder.declareInteger(key, integerValue);
          break;
        case BOOLEAN:
          Boolean booleanvalue = (Boolean) value;
          structBuilder.declareBoolean(key, booleanvalue);
          break;
        case LINK:
          Content linkValue = (Content) value;
          structBuilder.declareLink(key, contentRepository.getContentContentType(), linkValue);
          break;
        case DATE:
          Calendar dateValue = (Calendar) value;
          structBuilder.declareDate(key, dateValue);
          break;
        case ARRAY:
          List<?> listValue = (List<?>) value;
          parseList(key, listValue);
          break;
        case OBJECT:
        default:
          @SuppressWarnings({"unchecked"}) Map<String, ?> mapValue = (Map<String, ?>) value;
          structBuilder.enter(key);
          parseObject(mapValue);
          structBuilder.up();
      }
    }
  }

  private enum ObjectType {
    STRING, INTEGER, BOOLEAN, LINK, DATE, ARRAY, OBJECT
  }
}
