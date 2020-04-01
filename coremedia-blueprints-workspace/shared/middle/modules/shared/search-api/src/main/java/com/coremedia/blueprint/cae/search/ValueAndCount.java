package com.coremedia.blueprint.cae.search;

/**
 * A string value with a count.
 *
 * @cm.template.api
 */
public class ValueAndCount {

  private final String value;
  private final long count;

  public ValueAndCount(String name, long count) {
    this.value = name;
    this.count = count;
  }

  /**
   * Returns the value.
   *
   * @return value
   * @cm.template.api
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the count.
   *
   * @return count
   * @cm.template.api
   */
  public long getCount() {
    return count;
  }

  @Override
  public String toString() {
    return "ValueAndCount[value='" + value + "', count=" + count + ']';
  }
}
