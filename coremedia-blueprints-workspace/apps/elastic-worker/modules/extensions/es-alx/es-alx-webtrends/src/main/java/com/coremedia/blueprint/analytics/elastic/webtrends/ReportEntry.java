package com.coremedia.blueprint.analytics.elastic.webtrends;

/**
 * Helper class to sort Webtrends report entries.
 */
final class ReportEntry implements Comparable<ReportEntry> {

  private final String value;
  private final double measure;
  private static final double EPSILON = 0.00001d;
  private static final double NEGATIVE_EPSILON = -1.0d * EPSILON;
  private static final int HASHCODE_SHIFT_OFFSET = 32;

  ReportEntry(String value, double measure) {
    this.value = value;
    this.measure = measure;
  }

  public String getValue() {
    return value;
  }

  public double getMeasure() {
    return measure;
  }

  @Override
  public int compareTo(ReportEntry o) {
    return Double.compare(measure, o.measure);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReportEntry that = (ReportEntry) o;
    return Double.compare(that.measure, measure) == 0;
  }

  @Override
  public int hashCode() {
    long temp = measureIsNotZero() ? Double.doubleToLongBits(measure) : 0L;
    return (int) (temp ^ (temp >>> HASHCODE_SHIFT_OFFSET));
  }

  private boolean measureIsNotZero() {
    return (measure < NEGATIVE_EPSILON) || (measure > EPSILON);
  }

  @Override
  public String toString() {
    return "ReportEntry{" +
            "value='" + value + '\'' +
            ", measure=" + measure +
            '}';
  }
}
