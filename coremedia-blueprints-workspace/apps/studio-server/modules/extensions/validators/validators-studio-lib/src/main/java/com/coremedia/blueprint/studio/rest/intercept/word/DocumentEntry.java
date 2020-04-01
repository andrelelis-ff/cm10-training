package com.coremedia.blueprint.studio.rest.intercept.word;

/**
 * Pojo to extract images from the Word document.
 */
public class DocumentEntry {
  private String key;
  private Object value;
  private String name;


  public DocumentEntry(String key, Object value) {
    this(key, value, null);
  }


  public DocumentEntry(String key, Object value, String name) {
    this.key = key;
    this.value = value;
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public String getName() {
    return name;
  }
}
