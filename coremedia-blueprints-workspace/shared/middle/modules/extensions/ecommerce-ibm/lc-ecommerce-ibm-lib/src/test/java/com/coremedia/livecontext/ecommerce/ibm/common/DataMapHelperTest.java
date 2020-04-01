package com.coremedia.livecontext.ecommerce.ibm.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

class DataMapHelperTest {

  @Test
  void testSimpleValue() {
    Map<String, Object> map = ImmutableMap.<String, Object>builder()
            .put("testEntry", "testValue")
            .build();

    assertEquals("testValue", DataMapHelper.getValueForKey(map, "testEntry"));
  }

  @Test
  void testPathInnerMap() {
    Map<String, Object> innerMap = ImmutableMap.<String, Object>builder()
            .put("targetValue", "42")
            .build();

    Map<String, Object> outerMap = ImmutableMap.<String, Object>builder()
            .put("nestedMap", innerMap)
            .build();

    assertEquals("42", DataMapHelper.getValueForPath(outerMap, "nestedMap.targetValue"), "42");
  }

  @Test
  void testPathInnerList() {
    List<String> innerList = ImmutableList.of("zero", "one", "two");

    Map<String, Object> outerMap = ImmutableMap.<String, Object>builder()
            .put("innerList", innerList)
            .build();

    assertEquals("zero", DataMapHelper.getValueForPath(outerMap, "innerList[0]"));
    assertEquals("one", DataMapHelper.getValueForPath(outerMap, "innerList[1]"));
    assertEquals("two", DataMapHelper.getValueForPath(outerMap, "innerList[2]"));
  }

  @Test
  void testPathWithComplexInnerList() {
    Map<String, Object> mapInList = ImmutableMap.<String, Object>builder()
            .put("key1", "value1")
            .put("key2", "value2")
            .build();

    List<Object> innerList = ImmutableList.of(emptyMap(), mapInList);

    Map<String, Object> outerMap = ImmutableMap.<String, Object>builder()
            .put("aList", innerList)
            .build();

    assertEquals(innerList, DataMapHelper.getValueForPath(outerMap, "aList"));
    assertEquals(emptyMap(), DataMapHelper.getValueForPath(outerMap, "aList[0]"));
    assertEquals(mapInList, DataMapHelper.getValueForPath(outerMap, "aList[1]"));
    assertEquals("value2", DataMapHelper.getValueForPath(outerMap, "aList[1].key2"));
  }

  @Test
  void testNotExistingArray() {
    Map<String, Object> map = emptyMap();

    assertNull(DataMapHelper.getValueForKey(map, "MyList[0]"));
  }

  @Test
  void testArrayIndexOutOfBounds() {
    Map<String, Object> mapInList = ImmutableMap.<String, Object>builder()
            .put("key1", "value1")
            .put("key2", "value2")
            .build();

    List<Object> innerList = ImmutableList.of(emptyMap(), mapInList);

    Map<String, Object> outerMap = ImmutableMap.<String, Object>builder()
            .put("aList", innerList)
            .build();

    assertEquals("value2", DataMapHelper.getValueForPath(outerMap, "aList[1].key2"));
    assertNull(DataMapHelper.getValueForPath(outerMap, "aList[2]"));
    assertNull(DataMapHelper.getValueForPath(outerMap, "aList[2].key2"));
  }
}