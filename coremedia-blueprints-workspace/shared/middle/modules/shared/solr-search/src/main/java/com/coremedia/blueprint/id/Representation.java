package com.coremedia.blueprint.id;

public interface Representation<T> {
  String toID(T bean);

  T fromID(String id);

  boolean isValid(T bean);
}
