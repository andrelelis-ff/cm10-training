package com.coremedia.blueprint.taxonomies.semantic;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A POJO wrapping semantic entity info from external source
 */
public class SemanticEntity {
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String TYPE = "type";
  private Map<String, String> delegate = Maps.newHashMap();

  @JsonIgnore
  public String getId() {
    return get(ID);
  }

  public void setId(final String id) {
    set(ID, id);
  }

  @JsonIgnore
  public String getName() {
    return get(NAME);
  }

  public void setName(final String name) {
    set(NAME, name);
  }

  @JsonIgnore
  public String getType() {
    return get(TYPE);
  }

  public void setType(final String type) {
    set(TYPE, type);
  }

  public String get(final String property) {
    return delegate.get(property);
  }

  public void set(final String property, final String value) {
    delegate.put(property, value);
  }

  public void set(final Map<String, String> map) {
    delegate.putAll(map);
  }

  @JsonAnyGetter
  public Map<String, String> getProperties() {
    return delegate;
  }

  public static SemanticEntity populate(final Map<String, String> map) {
    SemanticEntity semanticEntity = new SemanticEntity();
    semanticEntity.set(map);
    return semanticEntity;
  }
}
