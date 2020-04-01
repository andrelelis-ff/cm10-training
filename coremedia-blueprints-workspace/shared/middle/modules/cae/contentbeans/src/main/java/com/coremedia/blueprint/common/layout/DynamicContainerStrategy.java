package com.coremedia.blueprint.common.layout;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;

public interface DynamicContainerStrategy {

  /**
   * Determines if a items list contains elements that should be rendered dynamically.
   *
   * @param items the items list
   * @return true if the rendering should be dynamic
   */
  boolean isDynamic(@NonNull List items);

  /**
   * Check if the strategy is enabled for the given bean
   */
  default boolean isEnabled(@NonNull Object bean) {
    return true;
  }
}
