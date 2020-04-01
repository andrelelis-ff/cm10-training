package com.coremedia.blueprint.common.layout;

import java.util.List;

/**
 * @cm.template.api
 */
public interface Container<T> {
  /**
   * Retrieves the items of the implementing class.
   * <p>
   * It is assumed that implementations of this method take care of proper cache dependency handling.
   *
   * @return a list of items computed for the backing content 'proxy' object
   * @cm.template.api
   */
  List<? extends T> getItems();

  /**
   * Returns the items, transitively flattening inner containers.
   *
   * @cm.template.api
   */
  List<?> getFlattenedItems();


  // --- Pagination -------------------------------------------------

  /**
   * Check whether the container supports pagination
   *
   * @return true if pagination is supported, false otherwise
   * @since 1901
   * @cm.template.api
   */
  default boolean isPaginated() {
    return false;
  }

  /**
   * Returns a {@link Pagination} with page number 0 which is backed by this
   * container.
   * <p>
   * If {@link #isPaginated()} is false, the result is unspecified.
   *
   * @since 1901
   * @cm.template.api
   */
  default Pagination asPagination() {
    return asPagination(0);
  }

  /**
   * Returns a {@link Pagination} with the given page number which is backed
   * by this container.
   * <p>
   * If {@link #isPaginated()} is false, the result is unspecified.
   *
   * @since 1901
   * @cm.template.api
   */
  default Pagination asPagination(int pageNum) {
    throw new UnsupportedOperationException("Pagination is not supported.");
  }
}
