package com.coremedia.blueprint.cae.search;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

/**
 * A value in a {@link Condition}. Allows to wrap multiple values connected with AND or OR in one object.
 *
 * <p>Note that the underlying string values must be correctly escaped for the used search engine so that they can
 * be used directly in the search engine query string.
 */
public class Value {

  public enum Operators {
    AND, OR
  }

  private Collection<String> values;
  private Operators op;

  protected Value(Collection<String> values, Operators op) {
    this.values = ImmutableList.copyOf(values);
    this.op = op;
  }

  public Collection<String> getValue() {
    return values;
  }

  public void setValue(Collection<String> values) {
    this.values = values;
  }

  public Operators getOp() {
    return op;
  }

  public void setOp(Operators op) {
    this.op = op;
  }

  /**
   * Creates a new Value instance that matches the given string.
   *
   * <p>Note that the given string must already be correctly escaped for the used search engine so that it can
   * be used directly in the search engine query string.
   *
   * <p>Note also that a {@link Condition} that checks if a field matches the returned value may be fulfilled even
   * if the indexed value is not exactly the same. Matching depends on the search engine and the type of the
   * index field.
   *
   * @param s string value
   * @return new Value
   */
  public static Value exactly(String s) {
    return new Value(ImmutableList.of(s), Operators.AND);
  }

  /**
   * Creates a new Value instance that matches any of the given strings.
   *
   * <p>Note that the given strings must already be correctly escaped for the used search engine so that they can
   * be used directly in the search engine query string.
   *
   * <p>Note also that a {@link Condition} that checks if a field matches the returned value may be fulfilled even
   * if the indexed value is not exactly one the given values. Matching depends on the search engine and the type of
   * the index field.
   *
   * @param c string values
   * @return new Value
   */
  public static Value anyOf(Collection<String> c) {
    return new Value(c, Operators.OR);
  }

  /**
   * Creates a new Value instance that matches values containing all of the given strings.
   *
   * <p>Note that the given strings must already be correctly escaped for the used search engine so that they can
   * be used directly in the search engine query string.
   *
   * <p>Note also that a {@link Condition} that checks if a field matches the returned value may be fulfilled even
   * if the indexed value does not contain exactly the same given values. Matching depends on the search engine and
   * the type of the index field.
   *
   * @param c string values
   * @return new Value
   */
  public static Value allOf(Collection<String> c) {
    return new Value(c, Operators.AND);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Value)) {
      return false;
    }

    Value value = (Value) o;

    if (op != value.op) {
      return false;
    }
    if (!values.equals(value.values)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = values.hashCode();
    result = 31 * result + op.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Value{" +
            "values=" + values +
            ", op=" + op +
            '}';
  }
}
