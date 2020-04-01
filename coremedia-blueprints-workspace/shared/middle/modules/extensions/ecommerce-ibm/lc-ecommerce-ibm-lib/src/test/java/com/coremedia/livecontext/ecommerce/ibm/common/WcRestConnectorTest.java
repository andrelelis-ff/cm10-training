package com.coremedia.livecontext.ecommerce.ibm.common;

import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WcRestConnectorTest {

  @Test
  public void testParseFromJson() {
    // some test json that has different structures, submap, list, direct values, as plain numbers and as strings
    // we need all values be parsed into Strings as some IBM WCS systems will return numbers, some return numbers as strings
    String json = "{\"submap\": {" +
            "\"numberAsString\":\"12345678901234567890\"," +
            "\"numberAsNumber\":12345678901234567890," +
            "\"smallNegativeNumberAsString\":\"-1\"," +
            "\"smallNegativeNumberAsNumber\":-1" +
            "}," +
            "\"numberAsString\":\"12345678901234567890\"," +
            "\"numberAsNumber\":12345678901234567890," +
            "\"smallNegativeNumberAsString\":\"-1\"," +
            "\"smallNegativeNumberAsNumber\":-1," +
            "\"numbersAsList\":[0,-1,\"2\"]," +
            "\"someText\":\"foo bar\"," +
            "\"someBoolean\":true" +
            "}";

    Reader reader = new StringReader(json);

    Map map = WcRestConnector.parseFromJson(reader, Map.class).orElse(null);
    assertThat(map).hasSize(8);
    assertThat(map.get("numberAsString")).isEqualTo("12345678901234567890");
    assertThat(map.get("numberAsNumber")).isEqualTo("12345678901234567890");
    assertThat(map.get("smallNegativeNumberAsString")).isEqualTo("-1");
    assertThat(map.get("smallNegativeNumberAsNumber")).isEqualTo("-1");
    assertThat(map.get("someText")).isEqualTo("foo bar");
    assertThat(map.get("someBoolean")).isEqualTo("true");

    Map subMap = (Map) map.get("submap");
    assertThat(subMap).isNotNull();
    assertThat(subMap.get("numberAsString")).isEqualTo("12345678901234567890");
    assertThat(subMap.get("numberAsNumber")).isEqualTo("12345678901234567890");
    assertThat(subMap.get("smallNegativeNumberAsString")).isEqualTo("-1");
    assertThat(subMap.get("smallNegativeNumberAsNumber")).isEqualTo("-1");

    List list = (List) map.get("numbersAsList");
    assertThat(list).containsExactly("0", "-1", "2");
  }
}
