package com.coremedia.ecommerce.studio.rest;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.NonNull;

public class Facet {

  private final String id;
  private final String value;

  Facet(@NonNull String id, @NonNull String value) {
    this.id = id;
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Facet facet = (Facet) o;

    //noinspection SimplifiableIfStatement
    if (!id.equals(facet.id)) {
      return false;
    }
    return value.equals(facet.value);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("value", value)
            .toString();
  }
}
