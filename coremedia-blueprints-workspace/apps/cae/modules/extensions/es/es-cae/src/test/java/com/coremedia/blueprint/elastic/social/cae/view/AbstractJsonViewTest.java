package com.coremedia.blueprint.elastic.social.cae.view;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static org.junit.Assert.assertTrue;

public abstract class AbstractJsonViewTest {
  private Gson gson = new Gson();

  protected void assertJsonEquals(String expected, String actual) {
    JsonObject expectedJson = gson.fromJson(expected, JsonElement.class).getAsJsonObject();
    JsonObject actualJson = gson.fromJson(actual, JsonElement.class).getAsJsonObject();

    assertTrue("Expected <" + expectedJson + ">, but was <" + actualJson + ">", jsonEquals(expectedJson, actualJson));
  }

  private boolean jsonEquals(JsonObject expectedJson, JsonObject actualJson) {
    return actualJson.equals(expectedJson);
  }

  private boolean jsonCompatibleEquals(Object expectedValue, Object actualValue) {
    boolean isEqual;
    if (expectedValue instanceof JsonObject && actualValue instanceof JsonObject) {
      isEqual = jsonEquals((JsonObject) expectedValue, (JsonObject) actualValue);
    } else if (expectedValue instanceof JsonArray && actualValue instanceof JsonArray) {
      isEqual = jsonEquals((JsonArray) expectedValue, (JsonArray) actualValue);
    } else if (expectedValue != null && actualValue != null) {
      isEqual = expectedValue.equals(actualValue);
    } else {
      isEqual = expectedValue == actualValue;
    }
    return isEqual;
  }

  private boolean jsonEquals(JsonArray expectedJson, JsonArray actualJson) {
    boolean isEqual = expectedJson.size() == actualJson.size();

    if (isEqual) {
      for (int i = 0; i < expectedJson.size() && isEqual; i++) {
        Object expectedValue = expectedJson.get(i);
        Object actualValue = actualJson.get(i);

        isEqual = jsonCompatibleEquals(expectedValue, actualValue);
      }
    }
    return isEqual;
  }
}
