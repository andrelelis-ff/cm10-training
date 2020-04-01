package com.coremedia.blueprint.common.util;

import com.coremedia.blueprint.common.layout.Container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class ContainerFlattener {
  // static utility class
  private ContainerFlattener() {}


  // --- features ---------------------------------------------------

  /**
   * Flattens nested containers.
   * <p>
   * Breaks cycles, drops duplicates, drops wrong-typed entries.
   */
  public static <T> List<T> flatten(Container container, Class<T> expectedType) {
    ArrayList<T> result = new ArrayList<>();
    recFlatten(container, result, new HashSet<>(), expectedType, false, false);
    return result;
  }


  // --- internal ---------------------------------------------------

  private static <T> void recFlatten(Container container,
                                     Collection<T> result,
                                     Collection<Container> visited,
                                     Class<T> expectedType,
                                     boolean allowDuplicates,
                                     boolean failOnError) {
    if (!visited.contains(container)) {
      visited.add(container);
      List<?> items = container.getItems();
      for (Object item : items) {
        //Only flatten item where the marker interface Flatless has not been set
        if (item instanceof Container && !(item instanceof Flatless)) {
          recFlatten(((Container)item), result, visited, expectedType, allowDuplicates, failOnError);
        } else {
          T typedItem = expectedType.isAssignableFrom(item.getClass()) ? expectedType.cast(item) : null;
          if (typedItem!=null) {
            if (allowDuplicates || !result.contains(typedItem)) {
              result.add(typedItem);
            }
          } else {
            if (failOnError) {
              throw new IllegalArgumentException(item.getClass().getName() + " is not a " + expectedType.getName());
            }
          }
        }
      }
    }
  }
}
