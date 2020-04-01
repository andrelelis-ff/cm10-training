package com.coremedia.blueprint.taxonomies.semantic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

public class SemanticContext {

  private String id;
  private Multimap<String, SemanticEntity> properties;

  public SemanticContext(String id, Multimap<String, SemanticEntity> properties) {
    this.id = id;
    this.properties = properties;
  }

  public String getId() {
    return id;
  }

  public Map<String, Collection<SemanticEntity>> getEntities() {
    return ImmutableMap.copyOf(properties.asMap());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SemanticContext that = (SemanticContext) o;

    return !(id != null ? !id.equals(that.id) : that.id != null) && !(properties != null ? !properties.equals(that.properties) : that.properties != null);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    return result;
  }
}