package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.elastic.core.api.counters.AverageCounter;
import com.coremedia.elastic.core.api.counters.Counter;

/**
 * Helper class for {@link Counter} results.
 *
 * @cm.template.api
 */
public final class Count {
  private final String name;
  private final Object target;
  private final long sum;
  private final long quantity;
  private final long value;

  public Count(AverageCounter averageCounter) {
    name = averageCounter.getName();
    target = averageCounter.getTarget();
    sum = averageCounter.getSum();
    quantity = averageCounter.getQuantity();
    value = 0L;
  }

  public Count(Counter counter) {
    name = counter.getName();
    target = counter.getTarget();
    value = counter.getValue();
    sum = counter.getValue();
    quantity = counter.getValue();
  }

  public Count(Count count, Object newTarget) {
    name = count.getName();
    value = count.getValue();
    sum = count.getSum();
    quantity = count.getQuantity();
    target = newTarget;
  }

  public String getName() {
    return name;
  }

  /**
   * @cm.template.api
   */
  public Object getTarget() {
    return target;
  }

  public long getSum() {
    return sum;
  }

  public long getQuantity() {
    return quantity;
  }

  public long getValue() {
    return value;
  }

  public double getAverage() {
    return quantity == 0L ? 0.0 : (double) sum / quantity;
  }
}
