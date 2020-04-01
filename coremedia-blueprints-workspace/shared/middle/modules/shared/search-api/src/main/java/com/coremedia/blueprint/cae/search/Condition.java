package com.coremedia.blueprint.cae.search;

/**
 * A class that contains triplets of field (search engine index field), {@link Operators}, and {@link Value},
 * allows to create conditions such as "documenttype" "is any of" "(CMArticle, CMVideo)".
 */
public class Condition {

  public enum Operators {
    IS, ISNOT, LOWERTHAN, GREATERTHAN
  }

  private String field;
  private Operators op;
  private Value value;

  protected Condition(String field, Operators op, Value value) {
    this.field = field;
    this.op = op;
    this.value = value;
  }

  protected Condition(SearchConstants.FIELDS field, Operators op, Value value) {
    this(field.toString(), op, value);
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Operators getOp() {
    return op;
  }

  public void setOp(Operators op) {
    this.op = op;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  public static Condition is(SearchConstants.FIELDS field, Value value) {
    return new Condition(field, Operators.IS, value);
  }

  public static Condition is(String field, Value value) {
    return new Condition(field, Operators.IS, value);
  }

  public static Condition isNot(SearchConstants.FIELDS field, Value value) {
    return new Condition(field, Operators.ISNOT, value);
  }

  public static Condition isNot(String field, Value value) {
    return new Condition(field, Operators.ISNOT, value);
  }

  public static Condition lowerThan(SearchConstants.FIELDS field, Value value) {
    return new Condition(field, Operators.LOWERTHAN, value);
  }

  public static Condition lowerThan(String field, Value value) {
    return new Condition(field, Operators.LOWERTHAN, value);
  }

  public static Condition greaterThan(SearchConstants.FIELDS field, Value value) {
    return new Condition(field, Operators.GREATERTHAN, value);
  }

  public static Condition greaterThan(String field, Value value) {
    return new Condition(field, Operators.GREATERTHAN, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Condition)) {
      return false;
    }

    Condition condition = (Condition) o;

    if (!field.equals(condition.field)) {
      return false;
    }
    if (op != condition.op) {
      return false;
    }
    if (!value.equals(condition.value)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = field.hashCode();
    result = 31 * result + op.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Condition{" +
            "field='" + field + '\'' +
            ", op=" + op +
            ", value=" + value +
            '}';
  }
}
